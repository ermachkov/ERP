/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.business.tests;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class DeleteDir {

    private String undeleteFolderName[];
    
    public DeleteDir(String ... undeleteFolderName){
        this.undeleteFolderName = undeleteFolderName;
    }
    
    public static void deleteDir(Path p, String ... undeleteFolderName) throws IOException {
        DeleteDir deleteDir = new DeleteDir(undeleteFolderName);
        deleteDir.delete(p);
    }

    private void delete(Path p) throws IOException {
        Files.walkFileTree(p, new FileVisitor(p));
    }

    private class FileVisitor extends SimpleFileVisitor<Path> {

        Path p;

        public FileVisitor(Path p) {
            this.p = p;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            String dir = file.getName(file.getNameCount() - 2).toString();
            for(String undeleteFolder : undeleteFolderName){
                if(undeleteFolder.equals(dir)){
                    return FileVisitResult.CONTINUE;
                }
            }
            
            Files.deleteIfExists(file);
            System.out.println("delete: " + file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            
            for(String undeleteDirName : undeleteFolderName){
                if(dir.getName(dir.getNameCount() - 1).toString().equals(undeleteDirName)){
                    return FileVisitResult.CONTINUE;
                }
            }
            
            if (!dir.equals(p)) {
                Files.deleteIfExists(dir);
                System.out.println("delete: " + dir);
            }

            return FileVisitResult.CONTINUE;
        }
    }
}
