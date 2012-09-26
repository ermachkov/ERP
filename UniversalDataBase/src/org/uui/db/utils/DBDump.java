/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.db.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.MultiPartEmail;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class DBDump {
    
    public static final int OVERWRITE_ALL = 0, ADD_NEW = 1;

    public static void exportDB(final String pathToDBFolder, String pathToDumpStore, String dumpFileName) {
        try {
            try (OutputStream os = Files.newOutputStream(
                            Paths.get(pathToDumpStore, dumpFileName),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE)) {

                try (ZipOutputStream zos = new ZipOutputStream(os)) {
                    
                    zos.setLevel(9);

                    Files.walkFileTree(Paths.get(pathToDBFolder), new SimpleFileVisitor<Path>() {

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            System.out.println("add file = "
                                    + Paths.get(pathToDBFolder).relativize(file));
                            ZipEntry ze = new ZipEntry(Paths.get(pathToDBFolder).relativize(file).toString());
                            ze.setMethod(ZipEntry.DEFLATED);
                            zos.putNextEntry(ze);
                            zos.write(Files.readAllBytes(file));
                            return FileVisitResult.CONTINUE;
                        }
                    });

                    zos.flush();
                }
                os.flush();
            }

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    public static void importDB(String fullPathToDump, String pathToDBFolder) {
        try {
            Path pTmp = Paths.get(pathToDBFolder);
            pTmp = Paths.get(pTmp.getRoot().toString(), pTmp.subpath(0, pTmp.getNameCount() - 1).toString());
            File fSnapshot = pTmp.resolve(".snapshot").toFile();
            if(fSnapshot.exists()){
                fSnapshot.delete();
            }
            
            ZipFile zipFile = new ZipFile(Paths.get(fullPathToDump).toFile());
            Enumeration<ZipEntry> enumZipEntry = (Enumeration<ZipEntry>) zipFile.entries();
            while (enumZipEntry.hasMoreElements()) {
                ZipEntry zipEntry = enumZipEntry.nextElement();
                Path p = Paths.get(pathToDBFolder).resolve(zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    p.toFile().mkdir();

                } else {
                    System.out.println("extract file = " + zipEntry.getName());
                    System.out.println(p);
                    byte[] bytes;
                    int offset;
                    try (InputStream is = zipFile.getInputStream(zipEntry)) {
                        int len = is.available();
                        bytes = new byte[len];
                        offset = 0;
                        int numRead = 0;
                        while (offset < bytes.length
                                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                            offset += numRead;
                        }
                    }

                    Path entryPath = Paths.get(p.getRoot().toString(),
                            p.subpath(0, p.getNameCount() - 1).toString());
                    if (!Files.exists(entryPath, LinkOption.NOFOLLOW_LINKS)) {
                        System.out.println(entryPath + " not exist, try make dirs...");
                        boolean isSuccess = entryPath.toFile().mkdirs();
                        System.out.println(entryPath + " dirs was created " + isSuccess);
                        if (!isSuccess) {
                            continue;
                        }
                    }

                    if (offset < bytes.length) {
                        throw new IOException("Could not completely read entry " + zipFile.getName());
                    }

                    Files.write(p, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                }

            }
        } catch (Exception ex) {
            System.err.println(ex);
            Logger.getGlobal().log(Level.SEVERE, fullPathToDump, ex);
        }
    }

    public static void sendDB(String mailServerHost, String emailTo, String subject,
            String message, String login, String password, String pathToDBFolder) {
        MultiPartEmail email = new MultiPartEmail();
        email.setHostName(mailServerHost);
        email.setAuthentication(login, password);

        exportDB(pathToDBFolder, System.getProperty("user.home"), "dbdump_jssdb.zip");

        EmailAttachment attachment = new EmailAttachment();
        attachment.setPath(Paths.get(System.getProperty("user.home"), "dbdump_jssdb.zip").toString());
        attachment.setDisposition(EmailAttachment.ATTACHMENT);
        attachment.setName("dbdump_jssdb.zip");
        try {
            email.attach(attachment);
            email.addTo(emailTo, "");
            email.setFrom(emailTo, "org.uui.db.utils");
            email.setSubject(subject);
            email.setMsg(message);
            email.send();
            System.out.println("Email with db dump sended to " + emailTo);
 
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }
}
