/*
 *  Copyright (C) 2011 Zubanov Dmitry
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com Omsk, Russia, created
 * 29.05.2010 (C) Copyright by Zubanov Dmitry
 */
package org.jssdb.filesystem;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Example to watch a directory (or tree) for changes to files.
 */
public abstract class WatchDir {

    private final WatchService watcher;
    private final ConcurrentMap<WatchKey, Path> keys;
    private final boolean recursive;
    private boolean trace = false;
    public static int ADD = 0, MODIFY = 1, DELETE = 2;

    /**
     * Creates a WatchService and registers the given directory
     */
    public WatchDir(String pathToDir) throws IOException {
        Path dir = Paths.get(pathToDir);
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new ConcurrentHashMap<>();
        this.recursive = true;

        if (recursive) {
            System.out.format("Scanning %s ...\n", dir);
            registerAll(dir);
            System.out.println("Done.");
        } else {
            register(dir);
        }

        // enable trace after initial registration
        this.trace = true;
        processEvents();
    }

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Process all events for keys queued to the watcher
     */
    private void processEvents() {
        for (;;) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);
                if (child.endsWith("lastestid")
                        || child.endsWith("lock")
                        || child.endsWith("revision")
                        || child.endsWith("data.idx")
                        || child.endsWith("data.jsdb")) {
                    continue;
                }

                if (child.getName(child.getNameCount() - 1).toString().indexOf("_lock") != -1) {
                    continue;
                }

                if (child.toString().indexOf("ProxyHolder") != -1) {
                    continue;
                }

                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    if (!child.toFile().isDirectory()) {
                        Logger.getGlobal().log(Level.FINE, "{0}, {1}, {2}",
                                new Object[]{"ENTRY_CREATE", child, child.toFile().lastModified()});

                        try {
                            onChange(child.toFile(), WatchDir.ADD);

                        } catch (Exception e) {
                            System.err.println("ENTRY_CREATE " + e);
                            Logger.getGlobal().log(Level.WARNING, child.toString(), e);
                        }
                    }
                }

                if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    if (!child.toFile().isDirectory()) {
                        Logger.getGlobal().log(Level.FINE, "{0}, {1}, {2}",
                                new Object[]{"ENTRY_MODIFY", child, child.toFile().lastModified()});
                        try {
                            onChange(child.toFile(), WatchDir.MODIFY);
                        } catch (Exception e) {
                            System.err.println("ENTRY_MODIFY " + e);
                            Logger.getGlobal().log(Level.WARNING, child.toString(), e);
                        }
                    }
                }

                if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    if (!child.toFile().isDirectory()) {
                        Logger.getGlobal().log(Level.FINE, "{0}, {1}, {2}",
                                new Object[]{"ENTRY_DELETE", child, child.toFile().lastModified()});
                        onChange(child.toFile(), WatchDir.DELETE);
                    }
                }

                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (recursive && (kind == StandardWatchEventKinds.ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                    } catch (IOException x) {
                        // ignore to keep sample readbale
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }

        }
    }

    public abstract void onChange(File file, int action);
}