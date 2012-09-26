/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.carssier.www;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ubo.document.Order;
import org.ubo.document.ServiceCommandment;
import org.ubo.goods.Goods;
import org.ubo.service.Service;
import org.ubo.tree.TreeBasic;
import org.ubo.tree.TreeFolderBasic;
import org.ubo.tree.TreeLeafBasic;
import org.ubo.utils.Result;
import org.ubo.www.Mediator;
import org.ubo.www.Page;
import org.ubo.www.PriceACL;

/**
 *
 * @author developer
 */
public class ExchangePoint extends HttpServlet {

    private Core core = Core.getInstance();

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

        response.setContentType("application/octet-stream");
        OutputStream out = response.getOutputStream();

        ByteArrayOutputStream baos;
        try (InputStream is = request.getInputStream()) {
            byte b[] = new byte[8096];
            int len;
            baos = new ByteArrayOutputStream();
            while ((len = is.read(b)) != -1) {
                baos.write(Arrays.copyOf(b, len));
            }
        }

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            Mediator mediator = (Mediator) ois.readObject();
            if (mediator != null) {
                out.write(mediatorHandler(mediator).getSerailData());
            } else {
                out.write(Mediator.getMediatorError(-100, "Income Mediator is null").getSerailData());
            }

            out.flush();
            out.close();

        } catch (IOException | ClassNotFoundException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            if (out != null) {
                out.write(Mediator.getMediatorError(-200, e.getMessage()).getSerailData());
                out.close();
            }
        }
    }

    private Mediator mediatorHandler(Mediator mediator) {
        Path rootPath = Paths.get(getServletContext().getRealPath("sites"));
        Map<String, Object> map = mediator.getAttributes();
        if (!map.containsKey("action")) {
            return Mediator.getMediatorError(-1000, "Has not action");

        }
        System.out.println("mediatorHandler started, rootPath " + rootPath);
        Logger.getGlobal().log(Level.INFO, "mediatorHandler started, rootPath {0}", rootPath);

        Mediator mResponse = Mediator.getMediatorOk();

        Map<String, String> accounts = new HashMap<>();
        for (File dir : rootPath.toFile().listFiles()) {
            if (dir.isFile()) {
                continue;
            }

            try {
                String password = new String(Files.readAllBytes(dir.toPath().resolve("config/password")));
                accounts.put(dir.getName(), password);

            } catch (Exception e) {
                Logger.getGlobal().log(Level.SEVERE, null, e);
            }
        }

        switch ("" + map.get("action")) {
            case "getSetOrders":
                if (accounts.containsKey("" + map.get("login"))) {
                    core.setOrdersStatus(rootPath, "" + map.get("login"), 
                            core.getOrdersByRemoteId(rootPath, "" + map.get("login"), 
                            (ArrayList<Order>) map.get("OrdersConfirmed")), 
                            Order.SYNC_CONFIRMED);
                    
                    Result r = core.getOrders(rootPath, "" + map.get("login"), Order.SYNC_NOT_SENDED);
                    if(r.isError()){
                        return Mediator.getMediatorError(10, r.getReason());
                    }
                    
                    Result rUpdate = core.setOrdersStatus(rootPath, "" + map.get("login"), 
                            (ArrayList<Order>)r.getObject(), Order.SYNC_SENDED);
                    if(rUpdate.isError()){
                        Logger.getGlobal().log(Level.WARNING, r.getReason());
                    }
                    
                    Map<String, Object> mapResponse = new HashMap<>();
                    mapResponse.put("notSyncOrders", (ArrayList<Order>)r.getObject());
                    mResponse = Mediator.getMediator(0, "Send not sended Orders", mapResponse);
                }
                
                break;

            case "fullSynchronize":
                // PriceACL, TreeGoodsAndService, TreeLeafBasic, TreeFolderBasic, 
                // WWWPages, login, password
                // sync all elements
                if (accounts.containsKey("" + map.get("login"))) {
                    ArrayList<PriceACL> list = (ArrayList<PriceACL>) map.get("PriceACL");
                    core.syncPageACL(rootPath, "" + map.get("login"), list);

                    TreeBasic treeBasic = (TreeBasic) map.get("TreeGoodsAndService");
                    core.syncTreeBasic(rootPath, "" + map.get("login"), treeBasic);

                    ArrayList<TreeFolderBasic> treeFoldersBasic = (ArrayList<TreeFolderBasic>) map.get("TreeFolderBasic");
                    core.syncTreeFolderBasic(rootPath, "" + map.get("login"), treeFoldersBasic);

                    ArrayList<Page> pages = (ArrayList<Page>) map.get("WWWPages");
                    core.syncWWWPages(rootPath, "" + map.get("login"), pages);

                    ArrayList<TreeLeafBasic> treeLeafBasic = (ArrayList<TreeLeafBasic>) map.get("TreeLeafBasic");
                    core.syncTreeLeafBasic(rootPath, "" + map.get("login"), treeLeafBasic);

                    ArrayList<Goods> goodses = (ArrayList<Goods>) map.get("Goods");
                    core.syncGoods(rootPath, "" + map.get("login"), goodses);

                    ArrayList<Service> services = (ArrayList<Service>) map.get("Service");
                    core.syncService(rootPath, "" + map.get("login"), services);

                    ArrayList<ServiceCommandment> serviceCommandments = (ArrayList<ServiceCommandment>) map.get("ServiceCommandment");
                    core.syncServiceCommandment(rootPath, "" + map.get("login"), serviceCommandments);

                    mResponse = Mediator.getMediatorOk();
                }

                break;
                
            case "pushImages":
                Iterator<String> it = map.keySet().iterator();
                while(it.hasNext()){
                    String key = it.next();
                    if(key.indexOf("image:") != -1){
                        String arr[] = key.split(":");
                        Result rImageSync = core.syncImage(rootPath, 
                                "" + map.get("login"), arr[1].trim(), 
                                (byte[]) map.get(key));
                        
                        if(rImageSync.isError()){
                            return Mediator.getMediatorError(1, rImageSync.getReason());
                        }
                    }
                }
                mResponse = Mediator.getMediatorOk();
                
                break;
        }

        return mResponse;
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
}
