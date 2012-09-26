/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.business.report;

import org.ubo.report.ReportException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.datetime.DateTime;
import org.ubo.document.Order;
import org.ubo.document.OrderRow;
import org.ubo.document.SalesItem;
import org.ubo.goods.Goods;
import org.ubo.money.Money;
import org.ubo.print.MediaFormat;
import org.ubo.print.PDFMaker;
import org.ubo.quantity.Quantity;
import org.ubo.report.ReportShift;
import org.ubo.report.ReportHandler;
import org.ubo.service.Service;
import org.ubo.tree.TreeLeaf;
import org.ubo.tree.Trees;
import org.ubo.utils.XPathUtil;
import org.uui.db.DataBase;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class ReportSales implements ReportHandler {

    private DataBase dataBase;
    private ArrayList<ServiceInfo> serviceInfoList = new ArrayList<>();
    private ArrayList<GoodsInfo> goodsInfoList = new ArrayList<>();
    private Path pathToHTML, pathToPDF;
    private ReportShift reportBundle;

    public ReportSales(long reportBundleId) throws ReportException {
        dataBase = CarssierDataBase.getDataBase();

        reportBundle = (ReportShift) dataBase.getObject(ReportShift.class.getName(), reportBundleId);
        if (reportBundle == null) {
            throw new ReportException("ReportBundle with id = "
                    + reportBundleId + " is not present in data base!");
        }

        pathToHTML = Paths.get(System.getProperty("user.home"), ".saas",
                "app", "reports", "html",
                reportBundle.getId() + "_sales_"
                + DateTime.getFormatedDate("yyyy_MM_dd_HH_mm_ss",
                reportBundle.getDate()) + ".html");

        pathToPDF = Paths.get(System.getProperty("user.home"), ".saas",
                "app", "reports", "pdf",
                reportBundle.getId() + "_sales_"
                + DateTime.getFormatedDate("yyyy_MM_dd_HH_mm_ss",
                reportBundle.getDate()) + ".pdf");

        if (!Files.exists(pathToHTML, LinkOption.NOFOLLOW_LINKS)
                || !Files.exists(pathToPDF, LinkOption.NOFOLLOW_LINKS)) {
            generateReport();
        }
    }

    public void regenerateReport() throws ReportException {
        generateReport();
    }

    private void generateReport() throws ReportException {
        for (Order order : reportBundle.getOrderSet()) {
            for (OrderRow orderRow : order.getOrderRows()) {
                SalesItem salesItem = orderRow.getSalesItem(dataBase);
                if (salesItem.getType() == SalesItem.GOODS) {
                    Logger.getGlobal().log(Level.INFO, "add to report goods {0}",
                            salesItem.getShortName());
                    GoodsInfo goodsInfo = new GoodsInfo(salesItem.getId(),
                            orderRow.getCount(), orderRow.getSumWithDiscount());
                    goodsInfo.setPath(goodsInfo.getPath());
                    goodsInfoList.add(goodsInfo);

                } else {
                    Logger.getGlobal().log(Level.INFO, "add to report service {0}",
                            salesItem.getShortName());
                    ServiceInfo serviceInfo = new ServiceInfo(salesItem.getId(),
                            orderRow.getCount(), orderRow.getSumWithDiscount());
                    serviceInfo.setPath(serviceInfo.getPath());
                    serviceInfoList.add(serviceInfo);
                }
            }
        }

        Set<ServiceInfo> setServiceInfo = new HashSet<>();
        for (ServiceInfo serviceInfo : serviceInfoList) {
            if (!setServiceInfo.add(serviceInfo)) {
                serviceUpdateSet(setServiceInfo, serviceInfo);
            }
        }

        ArrayList<ServiceInfo> sortedList = new ArrayList<>();
        for (ServiceInfo si : setServiceInfo) {
            sortedList.add(si);
        }

        Collections.sort(sortedList);

        BigDecimal total = BigDecimal.ZERO;
        for (ServiceInfo si : sortedList) {
            total = Money.ADD(total.toString(),
                    si.getSum().toString());
            System.out.println(si);
        }

        String html = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<!DOCTYPE nbsp [<!ENTITY nbsp \"&#160;\">]>\n"
                + "<html>\n"
                + "<div align=\"center\" style=\"font-family:Arial, Helvetica, "
                + "sans-serif; font-size:11pt; font-weight:bold;\">Отчет</div>\n"
                + "<table cellpadding=\"1\"><tr>"
                + "<td width=\"70%\" style=\"padding:3pt;color:#FFFFFF;"
                + "background-color:#000000\">Товар / Услуга</td>"
                + "<td align=\"center\" style=\"padding:3pt;color:#FFFFFF;"
                + "background-color:#000000\">Кол-во</td>"
                + "<td align=\"center\"  style=\"padding:3pt;color:#FFFFFF;"
                + "background-color:#000000\">Сумма</td></tr>\n";

        String prevPath = "";
        for (ServiceInfo si : sortedList) {
            Path p = Paths.get(si.getPath());
            p = p.subpath(0, p.getNameCount() - 1);

            if (!prevPath.equals(p.toString())) {
                prevPath = p.toString();
                System.out.println(p);
                html += "<tr><td colspan=\"3\" style=\"border-top-style:dashed;"
                        + "border-bottom-style:dashed;border-top-width:.5pt;"
                        + "border-bottom-width:.5pt;padding:5pt;\">" + p + "</td></tr>\n";
                System.out.println(si.getName());

                html += "<tr><td style=\"padding:5pt;\">" + si.getName() + "</td>"
                        + "<td align=\"center\" style=\"padding:5pt;\">"
                        + Quantity.format(si.getQuantity()) + "</td>"
                        + "<td align=\"right\" style=\"padding:5pt;\">"
                        + Money.formatToMoney(si.getSum().doubleValue())
                        + "=</td></tr>\n";

            } else {
                System.out.println(si.getName());
                html += "<tr><td style=\"padding:5pt;\">" + si.getName() + "</td>"
                        + "<td align=\"center\" style=\"padding:5pt;\">"
                        + Quantity.format(si.getQuantity()) + "</td>"
                        + "<td align=\"right\" style=\"padding:5pt;\">"
                        + Money.formatToMoney(si.getSum().doubleValue())
                        + "=</td></tr>\n";
            }
        }
        
        // GOODS
        Set<GoodsInfo> setGoodsInfo = new HashSet<>();
        for (GoodsInfo goodsInfo : goodsInfoList) {
            if (!setGoodsInfo.add(goodsInfo)) {
                goodsUpdateSet(setGoodsInfo, goodsInfo);
            }
        }

        ArrayList<GoodsInfo> sortedGoodsList = new ArrayList<>();
        for (GoodsInfo gi : setGoodsInfo) {
            sortedGoodsList.add(gi);
        }

        Collections.sort(sortedGoodsList);

        for (GoodsInfo gi : sortedGoodsList) {
            total = Money.ADD(total.toString(),
                    gi.getSum().toString());
            System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAA " + gi);
        }
        
        for (GoodsInfo gi : sortedGoodsList) {
            Path p = Paths.get(gi.getPath());
            p = p.subpath(0, p.getNameCount() - 1);

            if (!prevPath.equals(p.toString())) {
                prevPath = p.toString();
                System.out.println(p);
                html += "<tr><td colspan=\"3\" style=\"border-top-style:dashed;"
                        + "border-bottom-style:dashed;border-top-width:.5pt;"
                        + "border-bottom-width:.5pt;padding:5pt;\">" + p + "</td></tr>\n";
                System.out.println(gi.getName());

                html += "<tr><td style=\"padding:5pt;\">" + gi.getName() + "</td>"
                        + "<td align=\"center\" style=\"padding:5pt;\">"
                        + Quantity.format(gi.getQuantity()) + "</td>"
                        + "<td align=\"right\" style=\"padding:5pt;\">"
                        + Money.formatToMoney(gi.getSum().doubleValue())
                        + "=</td></tr>\n";

            } else {
                System.out.println(gi.getName());
                html += "<tr><td style=\"padding:5pt;\">" + gi.getName() + "</td>"
                        + "<td align=\"center\" style=\"padding:5pt;\">"
                        + Quantity.format(gi.getQuantity()) + "</td>"
                        + "<td align=\"right\" style=\"padding:5pt;\">"
                        + Money.formatToMoney(gi.getSum().doubleValue())
                        + "=</td></tr>\n";
            }
        }

        html += "<tr><td colspan=\"2\" align=\"right\" style=\"padding:5pt;"
                + "border-top-style:solid; border-bottom-style:solid;"
                + "font-weight:bold;\">ИТОГО:</td>"
                + "<td align=\"right\" style=\"padding:5pt;border-top-style:solid; "
                + "border-bottom-style:solid;font-weight:bold;\">"
                + Money.formatToMoney(total.doubleValue())
                + "=</td></tr>";

        html += "</table>";
        html += "<div style=\"font-family:Arial, Helvetica, "
                + "sans-serif; font-size:11pt; font-weight:bold;\">"
                + "Мастер: Х.З."
                + "</div>\n";
        html += "</html>";



        try {
            try (BufferedWriter bw = Files.newBufferedWriter(pathToHTML, Charset.forName("utf-8"),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE)) {
                bw.write(html);
                bw.flush();
            }

            Path templateDir = Paths.get(System.getProperty("user.home"),
                    ".saas", "app", "print");
            Path pathToXSL = templateDir.resolve(Paths.get("xsl-fo", "transform.xsl"));
            Path pathToFO = templateDir.resolve(Paths.get("xsl-fo", "out.fo"));
            Path pathToFOPConfig = Paths.get(System.getProperty("user.home"),
                    ".saas", "app", "config", "fop.xconf");

            MediaFormat mediaFormat = MediaFormat.A4Portrait();
            XPathUtil.setValue(pathToXSL.toString(),
                    "/xs:stylesheet/xs:template/fo:root/fo:layout-master-set/fo:simple-page-master",
                    mediaFormat.getXSLPageFormat());

            Map<String, String> m = new HashMap<>();
            m.put("master-reference", mediaFormat.getPageFormat());
            XPathUtil.setValue(pathToXSL.toString(),
                    "/xs:stylesheet/xs:template/fo:root/fo:page-sequence",
                    m);

            m = new HashMap<>();
            double bodyWidth = mediaFormat.getWidth()
                    - (mediaFormat.getMarginLeft() + mediaFormat.getMarginRight());
            m.put("select", Objects.toString(bodyWidth));
            XPathUtil.setValue(pathToXSL.toString(), "/xs:stylesheet/xs:variable", m);

            boolean makePDFResult = PDFMaker.html2PDF(
                    pathToXSL.toString(),
                    pathToFO.toString(),
                    pathToHTML.toString(),
                    pathToPDF.toString(),
                    pathToFOPConfig.toString());

            if (!makePDFResult) {
                throw new ReportException("Can't generate pdf report file!");
            }

        } catch (IOException ex) {
            Logger.getGlobal().log(Level.WARNING, null, ex);
        }

    }
    
    private void goodsUpdateSet(Set<GoodsInfo> set, GoodsInfo goodsInfo) {
        Iterator<GoodsInfo> it = set.iterator();
        while (it.hasNext()) {
            GoodsInfo gInfo = it.next();
            if (gInfo.getGoodsId() == goodsInfo.getGoodsId()) {
                gInfo.modifyQuantity(goodsInfo.getQuantity());
                gInfo.modifySum(goodsInfo.getSum());
                break;
            }
        }
    }

    private void serviceUpdateSet(Set<ServiceInfo> set, ServiceInfo serviceInfo) {
        Iterator<ServiceInfo> it = set.iterator();
        while (it.hasNext()) {
            ServiceInfo sInfo = it.next();
            if (sInfo.getServiceId() == serviceInfo.getServiceId()) {
                sInfo.modifyQuantity(serviceInfo.getQuantity());
                sInfo.modifySum(serviceInfo.getSum());
                break;
            }
        }
    }

    final class GoodsInfo implements Comparable {

        long goodsId;
        BigDecimal quantity, sum;
        String path = "";

        public GoodsInfo(long goodsId, BigDecimal quantity, BigDecimal sum) {
            this.goodsId = goodsId;
            this.quantity = quantity;
            this.sum = sum;
        }

        public long getGoodsId() {
            return goodsId;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void modifyQuantity(BigDecimal quantity) {
            this.quantity = Money.ADD(this.quantity.toString(), quantity.toString());
        }

        public BigDecimal getSum() {
            return sum;
        }

        public void modifySum(BigDecimal sum) {
            this.sum = Money.ADD(this.sum.toString(), sum.toString());
        }

        public String getName() {
            Goods g = (Goods) dataBase.getObject(Goods.class.getName(), goodsId);
            return g.getName();
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getPath() {
            String p = "  ";

            ArrayList<TreeLeaf> list = Trees.getFilteredLeafs("GoodsAndService",
                    Goods.class.getName(), dataBase, goodsId);
            if (!list.isEmpty()) {
                p = Trees.getPath(list.get(0), dataBase);
            }

            return p.replaceAll("GoodsAndService", "").substring(1);
        }

        public String toString() {
            return "GoodsInfo{" + path + ", goodsId=" + goodsId
                    + ", quantity=" + quantity + ", sum=" + sum + '}';
        }

        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final GoodsInfo other = (GoodsInfo) obj;
            if (this.goodsId != other.goodsId) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            int hash = 5;
            hash = 83 * hash + (int) (this.goodsId ^ (this.goodsId >>> 32));
            return hash;
        }

        @Override
        public int compareTo(Object o) {
            int val = 0;
            if (o != null) {
                if (o instanceof GoodsInfo) {
                    GoodsInfo another = (GoodsInfo) o;
                    val = path.compareTo(another.path);
                }
            }

            return val;
        }
    }

    final class ServiceInfo implements Comparable {

        long serviceId;
        BigDecimal quantity, sum;
        String path = "";

        public ServiceInfo(long serviceId, BigDecimal quantity, BigDecimal sum) {
            this.serviceId = serviceId;
            this.quantity = quantity;
            this.sum = sum;
        }

        public long getServiceId() {
            return serviceId;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void modifyQuantity(BigDecimal quantity) {
            this.quantity = Money.ADD(this.quantity.toString(), quantity.toString());
        }

        public BigDecimal getSum() {
            return sum;
        }

        public void modifySum(BigDecimal sum) {
            this.sum = Money.ADD(this.sum.toString(), sum.toString());
        }

        public String getName() {
            Service s = (Service) dataBase.getObject(Service.class.getName(), serviceId);
            return s.getName();
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getPath() {
            String p = "  ";

            ArrayList<TreeLeaf> list = Trees.getFilteredLeafs("GoodsAndService",
                    Service.class.getName(), dataBase, serviceId);
            if (!list.isEmpty()) {
                p = Trees.getPath(list.get(0), dataBase);
            }

            return p.replaceAll("GoodsAndService", "").substring(1);
        }

        public String toString() {
            return "ServiceInfo{" + path + ", serviceId=" + serviceId
                    + ", quantity=" + quantity + ", sum=" + sum + '}';
        }

        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ServiceInfo other = (ServiceInfo) obj;
            if (this.serviceId != other.serviceId) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + (int) (this.serviceId ^ (this.serviceId >>> 32));
            return hash;
        }

        @Override
        public int compareTo(Object o) {
            int val = 0;
            if (o != null) {
                if (o instanceof ServiceInfo) {
                    ServiceInfo another = (ServiceInfo) o;
                    val = path.compareTo(another.path);
                }
            }

            return val;
        }
    }

    @Override
    public Path getHTMLReport() {
        if (Files.exists(pathToHTML, LinkOption.NOFOLLOW_LINKS)) {
            return pathToHTML;

        } else {
            return null;
        }
    }

    @Override
    public Path getPDFReport() {
        if (Files.exists(pathToPDF, LinkOption.NOFOLLOW_LINKS)) {
            return pathToPDF;

        } else {
            return null;
        }
    }
}
