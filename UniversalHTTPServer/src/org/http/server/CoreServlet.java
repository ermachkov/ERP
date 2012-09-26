/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.http.server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public abstract class CoreServlet extends HttpServlet {

    private String indexPage = "index.html", wwwRootPath;
    final static long serialVersionUID = 1L;

    public CoreServlet(String wwwRootPath, String indexPage) {
        this.wwwRootPath = wwwRootPath;
        this.indexPage = indexPage;
    }
    
    public abstract void registeredNewSession(String sessionId);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(Sessions.getInstance().addSession(request.getSession().getId())){
            registeredNewSession(request.getSession().getId());
        }
        
        System.out.println("request.getRequestURI() " + request.getRequestURI());
        String realPath = getServletContext().getRealPath(request.getRequestURI());
        System.out.println("realPath " + realPath);
        
        Path pFile = Paths.get(realPath);
        
        if(pFile.toFile().isDirectory()){
            pFile = pFile.resolve(indexPage);
            System.out.println("pFile " + pFile);
        }
        
        if (pFile.toFile().exists()) {
            response.setStatus(HttpServletResponse.SC_OK);
            
            String contentType = "application/octet-stream";
            if (pFile.toString().toLowerCase(Locale.getDefault()).endsWith(".png")) {
                contentType = "image/png";

            } else if (pFile.toString().toLowerCase(Locale.getDefault()).endsWith(".gif")) {
                contentType = "image/gif";

            } else if (pFile.toString().toLowerCase(Locale.getDefault()).endsWith(".jpg")) {
                contentType = "image/jpeg";

            } else if (pFile.toString().toLowerCase(Locale.getDefault()).endsWith(".html") || pFile.toString().endsWith(".htm")) {
                contentType = "text/html";

            } else if (pFile.toString().toLowerCase(Locale.getDefault()).endsWith(".css")) {
                contentType = "text/css";

            } else if (pFile.toString().toLowerCase(Locale.getDefault()).endsWith(".js")) {
                contentType = "text/javascript";
            }
            
            response.setContentType(contentType);
            try (OutputStream output = response.getOutputStream()) {
                
                if(contentType.equals("text/html")){
                    String s = new String(Files.readAllBytes(pFile), Charset.defaultCharset());
                    s = s.replaceAll("\\{sess\\}", request.getSession().getId());
                    output.write(s.getBytes(Charset.defaultCharset()));
                    
                } else {
                    output.write(Files.readAllBytes(pFile));
                }
                
                output.flush();
            }

        } else {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Error");
        }
    }
}
