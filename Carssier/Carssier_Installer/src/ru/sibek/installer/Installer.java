/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.installer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author developer
 */
public abstract class Installer {

    private String rootDir = System.getProperty("user.dir");

    public Installer() {
        System.out.println("rootDir = " + rootDir);
    }

    public abstract void message(String message);

    public abstract void percent(int percent);

    // Installser/data
    // Installer/jre
    // Installer/install
    // Installer/Carssier_Installer.jar
    // data/core.zip
    // data/data.zip
    // data/scripts/...
    public void install(Path installPath) throws IOException {
        // clean core dir
        percent(10);
        Files.walkFileTree(installPath, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                file.toFile().delete();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                dir.toFile().delete();
                return FileVisitResult.CONTINUE;
            }
        });

        installPath.toFile().mkdir();

        // unzip core
        percent(50);
        unzip(Paths.get(rootDir, "data", "core.zip"), installPath);

        // uzip data
        percent(80);

        Path dataPath = Paths.get(System.getProperty("user.home"));
        unzip(Paths.get(rootDir, "data", "data.zip"), dataPath);
        percent(85);

        // modify env
        Path dbProp = Paths.get(dataPath.toString(), ".saas", "app", "config", "db.properties");
        List<String> list = Files.readAllLines(dbProp, Charset.forName("utf-8"));
        BufferedWriter bw = new BufferedWriter(new FileWriter(dbProp.toFile()));
        for (String line : list) {
            if (line.indexOf("path_to_db=") != -1) {
                line = "path_to_db=" + Paths.get(dataPath.toString(), ".saas", "app", "db") + "/";
            }

            bw.write(line);
            bw.newLine();
        }
        bw.flush();
        bw.close();

        Path dbLogger = Paths.get(dataPath.toString(), ".saas", "app", "config", "logger.properties");
        list = Files.readAllLines(dbLogger, Charset.forName("utf-8"));
        bw = new BufferedWriter(new FileWriter(dbLogger.toFile()));
        for (String line : list) {
            if (line.indexOf("java.util.logging.FileHandler.pattern") != -1) {
                line = "java.util.logging.FileHandler.pattern="
                        + System.getProperty("user.home") + "/.saas/app/logs/carssier%u.log";
            }

            bw.write(line);
            bw.newLine();
        }
        bw.flush();
        bw.close();

        // POS printer 
        Path fopConfig = Paths.get(dataPath.toString(), ".saas", "app", "config", "fop.xconf");
        list = Files.readAllLines(fopConfig, Charset.forName("utf-8"));
        bw = new BufferedWriter(new FileWriter(fopConfig.toFile()));
        for (String line : list) {
            if (line.indexOf("/home/developer/.saas/app/fonts") != -1) {
                line = "<directory recursive=\"true\">"
                        + System.getProperty("user.home")
                        + "/.saas/app/fonts</directory>";
            }
            bw.write(line);
            bw.newLine();
        }
        bw.flush();
        bw.close();

        //.saas/app/print/templates
        Path billPos = Paths.get(dataPath.toString(), ".saas", "app", "print", "templates", "bill_pos.html");
        list = Files.readAllLines(billPos, Charset.forName("utf-8"));
        bw = new BufferedWriter(new FileWriter(billPos.toFile()));
        for (String line : list) {
            if (line.indexOf("/home/developer/.saas/app/tmp/logo.png") != -1) {
                line = "<img src=\""
                        + System.getProperty("user.home")
                        + "/.saas/app/tmp/logo.png\" />";
            }
            bw.write(line);
            bw.newLine();
        }
        bw.flush();
        bw.close();

        Path systemConfig = Paths.get(dataPath.toString(), ".saas", "app", "config", "system.xml");
        list = Files.readAllLines(systemConfig, Charset.forName("utf-8"));
        bw = new BufferedWriter(new FileWriter(systemConfig.toFile()));
        for (String line : list) {
            if (line.indexOf("html:/home/developer/.saas/app/ui/tmp") != -1) {
                line = "<cashmachine config_dir=\"POS\" driver=\"pos\" "
                        + "name=\"Star\" password=\"48\" port=\"html:" 
                        + System.getProperty("user.home") + "/.saas/app/ui/tmp\" "
                        + "speed=\"\" text_field_lenght=\"56\"/>";
            }
            bw.write(line);
            bw.newLine();
        }
        bw.flush();
        bw.close();

        Path binPath = Paths.get(System.getProperty("user.home"), ".saas", "app", "bin");
        Files.setPosixFilePermissions(binPath.resolve("startui.sh"), PosixFilePermissions.fromString("rwxrwxrwx"));
        
        Path pWebcam = binPath.resolve("webcam");
        Files.setPosixFilePermissions(pWebcam.resolve("checker"), PosixFilePermissions.fromString("rwxrwxrwx"));
        Files.setPosixFilePermissions(pWebcam.resolve("converter"), PosixFilePermissions.fromString("rwxrwxrwx"));
        Files.setPosixFilePermissions(pWebcam.resolve("watcher"), PosixFilePermissions.fromString("rwxrwxrwx"));
        Files.setPosixFilePermissions(pWebcam.resolve("webcam.sh"), PosixFilePermissions.fromString("rwxrwxrwx"));
        
        Files.setPosixFilePermissions(installPath.resolve("client.sh"), PosixFilePermissions.fromString("rwxrwxrwx"));
        Files.setPosixFilePermissions(installPath.resolve("server.sh"), PosixFilePermissions.fromString("rwxrwxrwx"));

        // install jre
        String osArch = System.getProperty("os.arch");
        final Path jvmFrom = Paths.get(rootDir, "jvm", osArch);
        final Path jvmTo = Paths.get(installPath.toString(), "jre");

        Files.walkFileTree(jvmFrom, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                message("jre: " + dir.toString());
                Path targetPath = jvmTo.resolve(jvmFrom.relativize(dir));
                if (!Files.exists(targetPath)) {
                    Files.createDirectory(targetPath);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                message("jre: " + file.toString());
                Files.copy(file, jvmTo.resolve(jvmFrom.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });

        message("Installation complete!");
        percent(100);
    }

    private void unzip(Path pathToZip, Path outputDir) {
        try {
            final int BUFFER = 2048;
            BufferedOutputStream dest;
            FileInputStream fis = new FileInputStream(pathToZip.toFile());
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                System.out.println("Extracting: " + entry);
                message("Extracting: " + entry);
                if (entry.isDirectory()) {
                    Paths.get(outputDir.toString(), entry.getName()).toFile().mkdir();
                    continue;
                }

                int count;
                byte data[] = new byte[BUFFER];
                // write the files to the disk
                FileOutputStream fos = new FileOutputStream(
                        Paths.get(outputDir.toString(), entry.getName()).toFile());
                dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
            }
            zis.close();
        } catch (Exception e) {
            System.err.println(e);
            Logger.getGlobal().log(Level.WARNING, rootDir, e);
        }
    }
}
