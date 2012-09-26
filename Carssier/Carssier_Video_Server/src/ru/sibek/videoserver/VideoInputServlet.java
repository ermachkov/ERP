/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.videoserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author developer
 */
public class VideoInputServlet extends HttpServlet{
    
    private String wwwRootPath;
    private Path rootPath;
    
    public VideoInputServlet(String wwwRootPath){
        this.wwwRootPath = wwwRootPath;
        rootPath = Paths.get(wwwRootPath);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        try (ServletOutputStream out = response.getOutputStream()) {
            String user = request.getParameter("user");
            Path p = Paths.get("frames", user, "request");
            Path pRequest = rootPath.resolve(p);
            if(pRequest.toFile().exists()){
                out.print("true");
            } else {
                out.print("false");
            }
            
            out.flush();
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletInputStream is = request.getInputStream();
        int len;
        byte b[] = new byte[8096];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while((len = is.read(b)) > 0){
            baos.write(Arrays.copyOf(b, len));
        }
        
        StringTokenizer tokenizer = new StringTokenizer(new String(baos.toByteArray()), "\r\n");
        String user = "", marker = "";
        int index = 0;
        while(tokenizer.hasMoreTokens()){
            String s = tokenizer.nextToken();
            if(s.indexOf("name=\"frame_") != -1){
                user = s.split(";")[1];
                user = user.split("=")[1];
                user = user.replaceAll("\"", "");
                user = user.split("_")[1];
                break;
            }
            
            if(index == 0){
                marker = s;
            }
        }
        
        Path pCheck = Paths.get(System.getProperty("user.dir"), "app", "frames", user);
        if(!pCheck.toFile().exists()){
            return;
        }
        
        byte[] data = baos.toByteArray();
        byte[] image = null;
        for(int i = 0; i < data.length - 4; i++){
            if(data[i] == 13 && data[i + 1] == 10 && data[i + 2] == 13 && data[i + 3] == 10){
                image = Arrays.copyOfRange(data, i + 4, data.length - (marker.getBytes().length + 6));
                break;
            }
        }
        
        Path p = Paths.get("frames", user, "frame.jpg");
        Files.write(rootPath.resolve(p), image);
        Logger.getGlobal().log(Level.INFO, "Write frame {0}", p);
    }
    
}
