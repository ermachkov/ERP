/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.videoserver;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author developer
 */
public class VideoOutputServlet extends HttpServlet {

    private String wwwRootPath, url;
    private Path rootPath;

    public VideoOutputServlet(String wwwRootPath, String url) {
        this.wwwRootPath = wwwRootPath;
        rootPath = Paths.get(wwwRootPath);
        this.url = url;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

        String action = request.getParameter("action");
        if (action == null) {
            return;
        }

        String user = request.getParameter("user");
        if (user == null) {
            return;
        }

        Path pCheck = Paths.get(System.getProperty("user.dir"), "app", "frames", user);
        if (!pCheck.toFile().exists()) {
            return;
        }

        try {
            setFlag(user);
            
            switch (action) {
                case "view":
                    {   
                        Path p = Paths.get("frames", user, "frame.jpg");
                        byte[] data = Files.readAllBytes(rootPath.resolve(p));
                        response.setContentType("image/jpeg");
                        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
                        response.setHeader("Cache-directive", "no-cache"); // HTTP 1.0.
                        response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
                        response.setHeader("Pragma-directive", "no-cache"); // HTTP 1.0.
                        response.setDateHeader("Expires", 0); // Proxies.
                        response.setContentLength(data.length);
                        ServletOutputStream out = response.getOutputStream();
                        out.write(data);
                        out.close();
                        out.flush();
                        break;
                    }
                    
                case "page":
                    {
                        String page = "";
                        Path p = Paths.get(System.getProperty("user.dir"), "app", "index.html");
                        List<String> lines = Files.readAllLines(p, Charset.defaultCharset());
                        for(String s : lines){
                            s = s.replaceAll("\\{url\\}", url);
                            s = s.replaceAll("\\{user\\}", user);
                            page += s + "\n";
                        }
                        
                        response.setContentType("text/html");
                        response.setContentLength(page.getBytes().length);
                        ServletOutputStream out = response.getOutputStream();
                        out.write(page.getBytes());
                        out.close();
                        out.flush();
                        break;
                    }
            }

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }
    
    private void setFlag(String user) throws IOException{
        Path p = Paths.get("frames", user, "request");
        rootPath.resolve(p).toFile().createNewFile();
    }
}
