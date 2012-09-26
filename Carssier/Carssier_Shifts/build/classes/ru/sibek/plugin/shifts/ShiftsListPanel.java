/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.shifts;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.ubo.datetime.DateTime;
import org.ubo.document.Order;
import org.ubo.document.OrderRow;
import org.ubo.document.SalesItem;
import org.ubo.goods.Goods;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.money.Money;
import org.ubo.quantity.Quantity;
import org.ubo.report.ReportShift;
import org.ubo.service.Service;
import org.ubo.utils.MapUtils;
import org.ubo.utils.Result;
import org.uui.component.*;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.table.*;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.ui.MainFrame;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class ShiftsListPanel extends WorkPanel {

    private TableDateFilter tableDataFilter;
    private CarssierCore core = CarssierCore.getInstance();
    private MacTableModel shiftsTable;
    private Button btnPrint;
    private RadioButton rbtTable, rbtGraph;
    private String reportModel = "";

    public ShiftsListPanel(String sessionId) {
        super(sessionId);
        tableDataFilter = new TableDateFilter(sessionId) {

            @Override
            public void filterChange(String text) {
                //
            }

            @Override
            public void btnShowPress(Date dateStart, Date dateEnd) {
                //
            }
        };

        tableDataFilter.setTableRowNavigatorEnable(false);
        tableDataFilter.setTextFilterEnable(false);
        tableDataFilter.setTimeSelectorEnable(false);

        shiftsTable = new MacTableModel(sessionId, true);
        shiftsTable.setCssClass("leftMacTable");
        shiftsTable.setId("shiftsTable");
        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Смена", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Выручка", Number.class, false));
        shiftsTable.setHeader(mth);

        shiftsTable.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                try {
                    if (evt.getJSONObject().getString("eventType").equals("click")) {
                        showReport();
                    }
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        });

        btnPrint = new Button(getSession(), "Печать");
        btnPrint.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                    JSMediator.print(getSession(),reportModel);
            }
        });

        rbtTable = new RadioButton(sessionId, "rbtView", "Таблица", true);
        rbtGraph = new RadioButton(sessionId, "rbtView", "График", false);
        RadioButtonGroup rbtGroup = new RadioButtonGroup();
        rbtGroup.addRadioButton(rbtTable);
        rbtGroup.addRadioButton(rbtGraph);

        rbtTable.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                showReport();
            }
        });

        rbtGraph.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                showReport();
            }
        });
    }

    private void showReport() {
        long reportShiftId = (Long) shiftsTable.getRow(shiftsTable.getSelectedRow()).getValue();
        if (rbtTable.isChecked()) {
            showReport(createReportForShift(reportShiftId));
        } else {
            try {
                showReport(createGraphForShift(reportShiftId));
            } catch (JSONException ex) {
                Logger.getLogger(ShiftsListPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private String createGraphForShift(long reportShiftId) throws JSONException {
        Result result = core.getReportShift(reportShiftId);
        if (result.isError()) {
            return result.getReason();
        }

        ReportShift reportShift = (ReportShift) result.getObject();
        String report = "<div style='padding:15mm;'>"
                + "<br/><br/>"
                + "<div align='center'><strong>"
                + "Отчет по смене от "
                + DateTime.getFormatedDate("dd.MM.yy HH:mm", reportShift.getDate())
                + "</strong></div>"
                + "<br/><br/><br/>";

        Path pServiceChart = Paths.get(
                System.getProperty("user.home"), ".saas", "app", "ui",
                "img", "tmp", "serviceChart_" + new Date().getTime() + ".png");
        Path pGoodsChart = Paths.get(
                System.getProperty("user.home"), ".saas", "app", "ui",
                "img", "tmp", "goodsChart_" + new Date().getTime() + ".png");

        ArrayList<JSONObject> m[] = getDetailsMaps(reportShiftId);
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        Map<Long, BigDecimal> map = new HashMap<>();
        for (JSONObject json : m[0]) {
            map.put(json.getLong("id"), new BigDecimal(json.getString("sum")));
        }

        TreeMap<Long, BigDecimal> treemap = (TreeMap<Long, BigDecimal>) MapUtils.sortByValues(map);

        int count = 0;
        Entry<Long, BigDecimal> entry;
        DefaultPieDataset pieDataset = new DefaultPieDataset();
        while ((entry = treemap.pollFirstEntry()) != null) {
            if (count > 4) {
                break;
            }

//            dataset.setValue(
//                    entry.getValue(),
//                    "Sum",
//                    ((Service) core.getService(entry.getKey()).getObject()).getShortName());
            pieDataset.setValue(((Service) core.getService(entry.getKey()).getObject()).getShortName(), entry.getValue());

            count++;
        }

        JFreeChart serviceChart = ChartFactory.createPieChart("Топ 5 услуг", pieDataset, true, true, Locale.getDefault());

        //JFreeChart serviceChart = ChartFactory.createBarChart("Топ 5 услуг", "Название", "Sum", dataset,
        //        PlotOrientation.VERTICAL, false, true, false);
        try {
            ChartUtilities.saveChartAsPNG(pServiceChart.toFile(), serviceChart, 600, 600);

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, report, e);
        }


        dataset = new DefaultCategoryDataset();
        map = new HashMap<>();
        for (JSONObject json : m[1]) {
            map.put(json.getLong("id"), new BigDecimal(json.getString("sum")));
        }

        treemap = (TreeMap<Long, BigDecimal>) MapUtils.sortByValues(map);

        count = 0;
        pieDataset = new DefaultPieDataset();
        while ((entry = treemap.pollFirstEntry()) != null) {
            if (count > 4) {
                break;
            }
//            dataset.setValue(
//                    entry.getValue(),
//                    "Sum",
//                    ((Goods) core.getGoods(entry.getKey()).getObject()).getShortName());
            pieDataset.setValue(((Goods) core.getGoods(entry.getKey()).getObject()).getShortName(), entry.getValue());

            count++;
        }

        //JFreeChart goodsChart = ChartFactory.createBarChart("Топ 5 товаров", "Название", "Sum", dataset,
        //        PlotOrientation.VERTICAL, false, true, false);
        JFreeChart goodsChart = ChartFactory.createPieChart("Топ 5 товаров", pieDataset, true, true, Locale.getDefault());
        try {
            ChartUtilities.saveChartAsPNG(pGoodsChart.toFile(), goodsChart, 600, 600);

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, report, e);
        }

        report += "<table width='100%'>"
                + "<tr>"
                + "<td align='center' valign='middle'>"
                + "<img src='img/tmp/" + pServiceChart.getName(pServiceChart.getNameCount() - 1) + "' />"
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td align='center' valign='middle'>"
                + "<img src='img/tmp/" + pGoodsChart.getName(pGoodsChart.getNameCount() - 1) + "' />"
                + "</td>"
                + "</tr>"
                + "</table>";

        reportModel = report;
        return report;
    }

    private ArrayList<JSONObject>[] getDetailsMaps(long reportShiftId) throws JSONException {
        ArrayList<JSONObject> serviceList = new ArrayList<>();
        ArrayList<JSONObject> goodsList = new ArrayList<>();
        BigDecimal discountSum = BigDecimal.ZERO;

        ArrayList<Order> ordersList = new ArrayList<>();
        ordersList.addAll(((ReportShift) core.getReportShift(reportShiftId).getObject()).getOrderSet());
        for (Order order : ordersList) {

            discountSum = Money.ADD(order.getTotalDiscountSum().toString(), discountSum.toString());

            for (OrderRow orderRow : order.getOrderRows()) {
                SalesItem salesItem = orderRow.getSalesItem(CarssierDataBase.getDataBase());
                if (salesItem.getType() == SalesItem.SERVICE) {
                    Result r = core.getService(salesItem.getDbId());
                    if (r.isError()) {
                        JSMediator.alert(getSession(), r.getReason());
                        continue;
                    }

                    Service service = (Service) r.getObject();
                    int pos = Collections.binarySearch(serviceList, new JSONObject("{id:" + service.getId() + "}"), new Comparator<JSONObject>() {

                        @Override
                        public int compare(JSONObject o1, JSONObject o2) {
                            try {
                                return ((Long) o1.getLong("id")).compareTo(o2.getLong("id"));
                            } catch (Exception e) {
                                JSMediator.alert(getSession(), e.toString());
                                return -1;
                            }

                        }
                    });

                    if (pos > 0) {
                        try {
                            JSONObject json = serviceList.get(pos);
                            String sCount = json.getString("count");
                            String sSum = json.getString("sum");
                            BigDecimal bCount = Quantity.ADD(sCount, orderRow.getCount().toString());
                            BigDecimal bSum = Money.ADD(sSum, orderRow.getSumWithDiscount().toString());
                            json.put("count", bCount.toString());
                            json.put("sum", bSum.toString());
                            serviceList.set(pos, json);

                        } catch (Exception e) {
                            JSMediator.alert(getSession(), e.toString());
                            Logger.getGlobal().log(Level.WARNING, null, e);
                            continue;
                        }

                    } else {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("id", service.getId());
                            json.put("count", orderRow.getCount().toString());
                            json.put("sum", orderRow.getSumWithDiscount().toString());
                            serviceList.add(json);

                        } catch (Exception e) {
                            JSMediator.alert(getSession(), e.toString());
                            Logger.getGlobal().log(Level.WARNING, null, e);
                            continue;
                        }

                    }

                } else {
                    Result r = core.getGoods(salesItem.getDbId());
                    if (r.isError()) {
                        JSMediator.alert(getSession(), r.getReason());
                        continue;
                    }

                    Goods goods = (Goods) r.getObject();
                    int pos = Collections.binarySearch(serviceList, new JSONObject("{id:" + goods.getId() + "}"), new Comparator<JSONObject>() {

                        @Override
                        public int compare(JSONObject o1, JSONObject o2) {
                            try {
                                return ((Long) o1.getLong("id")).compareTo(o2.getLong("id"));
                            } catch (Exception e) {
                                JSMediator.alert(getSession(), e.toString());
                                return -1;
                            }
                        }
                    });

                    if (pos > 0) {
                        try {
                            JSONObject json = goodsList.get(pos);
                            String sCount = json.getString("count");
                            String sSum = json.getString("sum");
                            BigDecimal bCount = Quantity.ADD(sCount, orderRow.getCount().toString());
                            BigDecimal bSum = Money.ADD(sSum, orderRow.getSumWithDiscount().toString());
                            json.put("count", bCount.toString());
                            json.put("sum", bSum.toString());
                            goodsList.set(pos, json);

                        } catch (Exception e) {
                            JSMediator.alert(getSession(), e.toString());
                            Logger.getGlobal().log(Level.WARNING, null, e);
                            continue;
                        }

                    } else {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("id", goods.getId());
                            json.put("count", orderRow.getCount().toString());
                            json.put("sum", orderRow.getSumWithDiscount().toString());
                            goodsList.add(json);

                        } catch (Exception e) {
                            JSMediator.alert(getSession(), e.toString());
                            Logger.getGlobal().log(Level.WARNING, null, e);
                            continue;
                        }
                    }
                }
            }
        }

        return new ArrayList[]{serviceList, goodsList};
    }

    private void showReport(String model) {
            JSMediator.refreshElement(getSession(), "shiftReport", model);
    }

    private String createReportForShift(long reportShiftId) {
        Result result = core.getReportShift(reportShiftId);
        if (result.isError()) {
            return result.getReason();
        }

        ReportShift reportShift = (ReportShift) result.getObject();
        String report = "<div style='padding:15mm;'>"
                + "<br/><br/>"
                + "<div align='center'><strong>"
                + "Отчет по смене от "
                + DateTime.getFormatedDate("dd.MM.yy HH:mm", reportShift.getDate())
                + "</strong></div>"
                + "<br/><br/><br/>";

        BigDecimal total = BigDecimal.ZERO;
        for (Order order : reportShift.getOrderSet()) {
            total = Money.ADD(total, order.getTotalWithTotalDiscount());
        }

        BigDecimal bank = BigDecimal.ZERO;
        for (Order order : reportShift.getOrderSet()) {
            if (order.getPaidType() == Order.BANK_ACCOUNT) {
                bank = Money.ADD(bank, order.getTotalWithTotalDiscount());
            }
        }

        String border = "style='border-left: 1px solid black; border-top: 1px solid black;'";
        String borderT = "style='border-right: 1px solid black; border-bottom: 1px solid black;'";
        report += "<table width='100%' cellpadding='5' cellspacing='0' " + borderT + ">"
                + "<thead>"
                + "<tr>"
                + "<th width='70%' " + border + ">Показатель</th>"
                + "<th " + border + ">Значение</th>"
                + "</tr>"
                + "</thead>"
                + "<tr>"
                + "<td " + border + ">Реализация общая</td>"
                + "<td align='right' " + border + ">" + total + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td " + border + ">Оплачено по б/н</td>"
                + "<td align='right' " + border + ">" + bank + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td " + border + ">Вычет/премия</td>"
                + "<td align='right' " + border + ">" + reportShift.getWithdrawal() + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td " + border + ">Ф.О.Т.</td>"
                + "<td align='right' " + border + ">"
                + reportShift.getSalarySum(CarssierDataBase.getDataBase()) + "</td>"
                + "</tr>"
                + "</table>";

        report += "</div>";
        reportModel = report;

        return report;
    }

    @Override
    public String getModel() {
        String model = ""
                + "<table width='100%' height='100%'>"
                + "<tr>"
                + "<td width='30%' valign='top' style='border-right: 1px dotted gray;'>"
                + getShiftsTable()
                + "</td>"
                + "<td valign='top'>"
                + getReport()
                + "</td>"
                + "</tr>"
                + "</table>";

        return model;
    }

    private String getReport() {
        String model = ""
                + "<table width='100%'>"
                + "<tr>"
                + "<td align='left' style='font-size:70%; border-bottom: 1px dotted gray;'>"
                + rbtTable.getModel() + rbtGraph.getModel()
                + "</td>"
                + "<td align='right' style='border-bottom: 1px dotted gray;'>"
                + btnPrint.getModel()
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td valign='top' id='shiftReport' colspan='2' style='font-size:80%;'>"
                + "Для просмотра отчета выберите смену слева"
                + "</td>"
                + "</tr>"
                + "</table>";


        return model;
    }

    private String getShiftsTable() {
        ArrayList<ReportShift> list = core.getClosedShifts();
        ArrayList<MacTableRow> rows = new ArrayList<>();
        for (ReportShift reportShift : list) {
            MacTableRow row = new MacTableRow();
            MacTableCell cell = new MacTableCell(getSession(), 
                    "Смена за " + DateTime.getFormatedDate("dd.MM.yy HH:mm",
                    reportShift.getDate()), false);
            row.addCell(cell);

            BigDecimal val = BigDecimal.ZERO;
            for (Order order : reportShift.getOrderSet()) {
                val = Money.ADD(val, order.getTotalWithTotalDiscount());
            }

            row.addCell(new MacTableCell(getSession(), val, false));
            row.setValue(reportShift.getId());
            rows.add(row);
        }
        shiftsTable.setData(rows);

        return "<div style='width:100%'>" + tableDataFilter.getModel() + "</div>" + shiftsTable.getModel();
    }

    @Override
    public void fireEvent(String json) {
        if (json == null) {
            return;
        }

        if (json.equals("")) {
            return;
        }

        if (json.equals("{}")) {
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(json);
            setSession(jsonObject.getString("session"));
            
            if (jsonObject.getString("eventType").equals("click")) {
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "Смены");
                JSMediator.setWorkPanel(getSession(), getModel());
                JSMediator.hideNavigationPanel(getSession());
            }

        } catch (JSONException  ex) {
            JSMediator.alert(getSession(), ex.toString());
            Logger.getGlobal().log(Level.WARNING, json, ex);
        }
    }
}
