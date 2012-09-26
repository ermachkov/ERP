package ru.sibek.plugin.salary.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.datetime.DateTime;
import org.ubo.employee.Employee;
import org.ubo.money.Money;
import org.ubo.report.ReportShift;
import org.ubo.utils.Result;
import org.uui.component.Button;
import org.uui.component.Component;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.table.*;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;

public class SalaryInfoPanel extends Component {

    private ReportShift reportShift;
    private MacTableModel macTableModel;
    private CarssierCore core = CarssierCore.getInstance();
    private Button btnPay, btnPrint;
    private Map<Long, BigDecimal> salaryForCrews;
    private String salaryForCrewsInfo;

    public SalaryInfoPanel(String sessionId) {
        super(sessionId);
        macTableModel = new MacTableModel(sessionId, true);
        macTableModel.setCssClass("macTable");
        macTableModel.setId("saleDistributionTable");
        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Работник", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Начислено", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Удержано", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("К получению", String.class, false));
        macTableModel.setHeader(mth);

        btnPay = new Button(sessionId, "Выплатить");
        btnPay.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    WebKitEventBridge.getInstance().pushEventToComponent(
                            getSession(), SalaryReportPanel.class.getName(),
                            "{eventType:push, session:" + getSession() + ", action:paySalary, "
                            + "reportShiftId:" + reportShift.getId() + "}");

                    JSMediator.hideRightPanel(getSession());

                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, evt.toString(), e);
                }
            }
        });

        btnPrint = new Button(sessionId, "Печать");
        btnPrint.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                print();
            }
        });

    }

    public void print() {
        String retained = "0.00";
        String withdrawal = "0.00";
        if (reportShift.getWithdrawal().doubleValue() > 0) {
            withdrawal = reportShift.getWithdrawal().toString();
        } else {
            retained = reportShift.getWithdrawal().toString().replaceAll("-", "");
        }

        BigDecimal total = Money.ADD(
                core.getSalarySumForReportShift(reportShift.getId()).toString(),
                reportShift.getWithdrawal().toString());

        String foreman = "", cashmaster = "";
        for (Employee employee : reportShift.getEmployeeSet()) {
            if (employee.getRole().equals("foreman")) {
                foreman = employee.getText();
                break;
            }
        }

        for (Employee employee : reportShift.getEmployeeSet()) {
            if (employee.getRole().indexOf("cash") != -1) {
                cashmaster = employee.getText();
                break;
            }
        }

        String doc = ""
                + "<div style='width:210mm; padding:25mm;'>"
                + "<div align='center' style='font-weight:bold;'>"
                + "Отчет о выплаченной з/п за "
                + DateTime.getFormatedDate("dd.MM.yy HH:mm", reportShift.getDatePaid())
                + "</div>"
                + "<br/><br/>"
                + "Фонд з/п по итогам смены: "
                + "<strong>" + core.getSalarySumForReportShift(reportShift.getId()) + "</strong> р."
                + "<br/>"
                //+ "Удержано со смены: <strong>" + retained + "</strong> p.<br/>"
                + "Доп. вознаграждение смены: <strong>" + withdrawal + "</strong> p.<br/>"
                + "Итого: <strong>" + total + "</strong> p.<br/><br/>"
                + macTableModel.getModel()
                + "<br/><br/><br/><br/>"
                + "Мастер смены: ____________________<strong>" + foreman + "</strong><br/><br/><br/>"
                + "Кассир смены: ____________________<strong>" + cashmaster + "</strong>"
                + "</div>";

        JSMediator.print(getSession(), doc);

    }

    public void setReportShift(long reportShiftId) {
        try {
            JSMediator.showLockPanel(getSession());
            Result result = core.getReportShift(reportShiftId);
            if (result.isError()) {
                JSMediator.alert(getSession(), result.getReason());
            } else {
                reportShift = ((ReportShift) result.getObject());
            }

            salaryForCrews = core.getSalaryForCrews(getSession(), reportShift);
            salaryForCrewsInfo = "<div style='font-size:70%; margin-left:15px;'>"
                    + "Начисление з/п по бригадам (справочно):<br>";
            Iterator<Long> it = salaryForCrews.keySet().iterator();
            while (it.hasNext()) {
                long idCrew = it.next();
                salaryForCrewsInfo += "- "
                        + core.getCrewById(idCrew).getName() + " = "
                        + salaryForCrews.get(idCrew) + " р.<br/>";
            }

            salaryForCrewsInfo += "</div>";

            JSMediator.hideLockPanel(getSession());

        } catch (Exception e) {
            JSMediator.alert(getSession(), e.toString());

            try {
                JSMediator.hideLockPanel(getSession());
            } catch (Exception ex) {
            }
        }
    }

    @Override
    public String getModel() {
        String actionsButtons = "";
        if (reportShift.getSalaryStatus() == ReportShift.SALARY_DISTRIBUTED_UNPAID) {
            actionsButtons = "<td align='right'>" + btnPay.getModel() + "</td>";
        }

        if (reportShift.getSalaryStatus() == ReportShift.SALARY_DISTRIBUTED_PAID) {
            actionsButtons = "<td align='right'>" + btnPrint.getModel() + "</td>";
        }

        String sign = reportShift.getWithdrawal().doubleValue() >= 0 ? "+" : "-";
        BigDecimal sum = Money.ADD(
                core.getSalarySumForReportShift(reportShift.getId()).toString(),
                reportShift.getWithdrawal().toString());
        String info = "Фонд зарплаты: " + core.getSalarySumForReportShift(reportShift.getId())
                + " " + sign + " " + Math.abs(reportShift.getWithdrawal().doubleValue())
                + " = <strong>" + sum + " p.</strong>";

        String model = "<div style='width:97%;font-size:80%;padding:5px;"
                + "border-radius:6px;border: 1px solid gray;margin:3px;'>"
                + "<table width='100%' height='100%' border='0'>"
                + "<tr><td valign='middle'>"
                + info
                + "</td>"
                + actionsButtons
                + "</tr>"
                + "</table>"
                + "</div>";

        ArrayList list = new ArrayList();
        int row = 0;
        for (Employee employee : reportShift.getEmployeeSet()) {
            MacTableRow macTableRow = new MacTableRow();
            String img = "<img src='img/subbuttons/employee.png' />";
            if (employee.getRole().equals("foreman")) {
                img = "<img src='img/subbuttons/master.png' />";
            }

            String employeeName = employee.getFullName();
            if (employeeName.equals("")) {
                employeeName = employee.getShortName();
            }
            String employeeString = img + "&nbsp;" + employeeName;

            macTableRow.addCell(new MacTableCell(getSession(), employeeString, false));
            macTableRow.addCell(new MacTableCell(getSession(), employee.getAccruedWages(), false));
            macTableRow.addCell(new MacTableCell(getSession(), employee.getRetained(), false));
            macTableRow.addCell(new MacTableCell(getSession(), 
                    Money.SUBSTRACT(employee.getAccruedWages().toString(),
                    employee.getRetained().toString()),
                    false));
            list.add(macTableRow);

            row++;
        }

        macTableModel.setData(list);

        model = model
                + "<div style='overflow:auto; width:99%;'>"
                + macTableModel.getModel()
                + "</div><br/><br/><br/><hr/>"
                + salaryForCrewsInfo;

        return model;
    }
}