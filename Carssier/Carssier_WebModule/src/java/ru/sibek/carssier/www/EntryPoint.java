/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.carssier.www;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.comet.CometEvent;
import org.apache.catalina.comet.CometProcessor;
import org.ubo.json.JSONObject;
import org.uui.webkit.WebKitEventBridge;

/**
 *
 * @author developer
 */
public class EntryPoint extends HttpServlet implements CometProcessor {

    private ConcurrentHashMap<String, MainFrame> instanceMap = new ConcurrentHashMap<>();

    public EntryPoint() {
        //
    }

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {

            Path p = Paths.get(request.getRequestURI());
            String login = p.getName(p.getNameCount() - 1).toString();

            if (!instanceMap.containsKey(request.getRequestedSessionId() + "-" + login)) {
                MainFrame core = new MainFrame(
                        request.getSession(),
                        request.getRequestedSessionId(),
                        getServletContext().getRealPath("sites"),
                        request.getRequestURI());
                instanceMap.put(request.getRequestedSessionId() + "-" + login, core);
                out.write(core.getModel());

            } else {
                if (request.getParameter("action") == null) {
                    MainFrame mainFrame = instanceMap.get(request.getRequestedSessionId() + "-" + login);
                    if (mainFrame != null) {
                        out.write(mainFrame.getModel());
                    }

                } else {
                    if (request.getParameter("action").equals("event")) {
                        try {
                            JSONObject jsonObject = new JSONObject(request.getParameter("json"));
                            WebKitEventBridge.getInstance().pushEventToComponent(
                                    jsonObject.getString("sess"),
                                    jsonObject.getString("identificator"),
                                    request.getParameter("json"));

                        } catch (Exception e) {
                            Logger.getGlobal().log(Level.WARNING, null, e);
                        }

                    }
                }
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    @Override
    public void event(CometEvent ce) throws IOException, ServletException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
