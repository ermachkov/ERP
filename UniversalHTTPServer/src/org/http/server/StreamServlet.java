/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.http.server;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class StreamServlet extends HttpServlet {

    private String wwwRootPath;
    private Path lastFrame;

    public StreamServlet(String wwwRootPath) {
        this.wwwRootPath = wwwRootPath;
        lastFrame = Paths.get(wwwRootPath).resolve(Paths.get("img", "stream", "last", "frame.jpg"));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("image/jpeg");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        response.setHeader("Cache-directive", "no-cache"); // HTTP 1.0.
        
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
        response.setHeader("Pragma-directive", "no-cache"); // HTTP 1.0.
        response.setDateHeader("Expires", 0); // Proxies.

        byte[] b = Files.readAllBytes(lastFrame);
        try (OutputStream output = response.getOutputStream()) {
            output.write(b);
            output.flush();
        }

    }
}
