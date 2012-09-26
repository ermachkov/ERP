package ru.sibek.plugin.salary.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.employee.Crew;
import org.ubo.employee.Employee;
import org.ubo.money.Money;
import org.ubo.report.ReportShift;
import org.ubo.utils.Result;
import org.ubo.utils.StringToNumber;
import org.uui.component.*;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.table.*;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.core.ui.ConfirmPanel;

public class SalaryDistributionPanel extends Component {

    private CarssierCore core = CarssierCore.getInstance();
    private ReportShift reportShift;
    private MacTableModel macTableModel;
    private SalaryModel salaryModel;
    private Button btnSave, btnToDistribute,
            btnToDistributeConfirm, btnBack, btnPrint;
    private TextField txtChange;
    private int currentPage = 0;
    private static final int PRE_DISTRIBUTION = 0, DISTRIBUTION = 1,
            DISTRIBUTION_CONFIRM = 2;
    private BigDecimal totalSalaryWithdrawal = BigDecimal.ZERO;
    private BigDecimal totalSalaryDistribution = BigDecimal.ZERO;
    private BigDecimal salarySumFromShift;
    private Map<Long, BigDecimal> salaryForCrews;
    private String salaryForCrewsInfo;

    public SalaryDistributionPanel(String sessionId) {
        super(sessionId);
        macTableModel = new MacTableModel(sessionId, true);
        macTableModel.setMode(MacTableModel.MODE_EDIT);
        macTableModel.setCanDeleteRow(false);
        macTableModel.setCssClass("macTable");
        macTableModel.setId("saleDistributionTable");
        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Работник", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Начислено", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Удержано", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Фиксировать", Boolean.class, false));
        macTableModel.setHeader(mth);
        macTableModel.addUIEventListener(getTableListener());

        btnSave = new Button(sessionId, "Сохранить");
        btnSave.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                saveSalaryDistribution();
            }
        });

        btnPrint = new Button(sessionId, "Печать");
        btnPrint.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                JSMediator.print(getSession(), macTableModel.getModel());
            }
        });

        txtChange = new TextField(getSession(), "0");
        txtChange.setStyle("text-align:center;");
        txtChange.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                if (evt.getJSON().indexOf("stopEditing") != -1) {
                    WebKitEventBridge.getInstance().pushEventToComponent(
                            getSession(),
                            SalaryReportRightPanel.class.getName(),
                            "{eventType:push, session:" + getSession() + ",  action:updateSalaryDistributionPanel}");
                }
            }
        });

        btnToDistribute = new Button(sessionId, "Далее");
        btnToDistribute.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                totalSalaryWithdrawal = BigDecimal.ZERO;
                try {
                    totalSalaryWithdrawal = new BigDecimal(txtChange.getText().replaceAll(",", ".").trim());
                } catch (Exception e) {
                    txtChange.setText("0");
                }

                totalSalaryDistribution = Money.ADD(
                        salarySumFromShift.toString(),
                        totalSalaryWithdrawal.toString());

                currentPage = DISTRIBUTION;
                createModel();

                WebKitEventBridge.getInstance().pushEventToComponent(
                        getSession(),
                        SalaryReportRightPanel.class.getName(),
                        "{eventType:push, session:" + getSession() + ",  action:updateSalaryDistributionPanel}");
            }
        });

        btnToDistributeConfirm = new Button(sessionId, "Далее");
        btnToDistributeConfirm.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                currentPage = DISTRIBUTION_CONFIRM;
                WebKitEventBridge.getInstance().pushEventToComponent(getSession(),
                        SalaryReportRightPanel.class.getName(),
                        "{eventType:push, session:" + getSession() + ",  action:updateSalaryDistributionPanel}");
            }
        });

        btnBack = new Button(sessionId, "Назад");
        btnBack.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                currentPage--;
                WebKitEventBridge.getInstance().pushEventToComponent(getSession(),
                        SalaryReportRightPanel.class.getName(),
                        "{eventType:push, session:" + getSession() + ",  action:updateSalaryDistributionPanel}");
            }
        });

    }

    private void saveSalaryDistribution() {
        ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
            @Override
            public void pressed(int button) {
                for (SalaryRow salaryRow : salaryModel.getRows()) {
                    if (salaryRow.getParent() == null) {
                        continue;
                    }
                    salaryRow.getEmployee().setAccruedWages(salaryRow.getSum());
                    salaryRow.getEmployee().setRetained(salaryRow.getRetained());
                }

                reportShift.setWithdrawal(totalSalaryWithdrawal);
                reportShift.setSalaryStatus(ReportShift.SALARY_DISTRIBUTED_UNPAID);
                core.modifyReportShift(reportShift);

                WebKitEventBridge.getInstance().pushEventToComponent(
                        getSession(), SalaryReportPanel.class.getName(),
                        "{eventType:push, session:" + getSession() + ",  action:hideRightPanel}");
            }
        };
        confirmPanel.setTitle("Вопрос");
        confirmPanel.setMessage("Сохранить результат распределения фонда з/п?");
        confirmPanel.showPanel();
    }

    private UIEventListener getTableListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    if (evt.getJSONObject().getString("eventType").equals("stopCellEditing")) {
                        int col = evt.getJSONObject().getInt("column");
                        int row = evt.getJSONObject().getInt("row");
                        String s = evt.getJSONObject().getString("value");
                        salaryModel.recalculate(row, col, StringToNumber.formatToMoney(s));
                        WebKitEventBridge.getInstance().pushEventToComponent(
                                getSession(), SalaryReportRightPanel.class.getName(),
                                "{eventType:push, session:" + getSession() + ",  action:updateSalaryDistributionPanel}");
                    }

                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, evt.toString(), e);
                }
            }
        };
        return listener;
    }

    public void setReportShift(long reportShiftId) {
        try {
            JSMediator.showLockPanel(getSession());
            currentPage = PRE_DISTRIBUTION;
            btnToDistributeConfirm.setEnabled(false);

            Result result = core.getReportShift(reportShiftId);
            if (result.isError()) {
                JSMediator.alert(getSession(), result.getReason());

            } else {
                reportShift = ((ReportShift) result.getObject());
            }
            salarySumFromShift = core.getSalarySumForReportShift(reportShift.getId());
            txtChange.setText("0");

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

    private void createModel() {
        salaryModel = new SalaryModel(totalSalaryDistribution);

        Iterator<Long> it = salaryForCrews.keySet().iterator();
        while (it.hasNext()) {
            long crewId = it.next();
            Crew crew = core.getCrewById(crewId);
            if (crew == null) {
                continue;
            }

            SalaryRow salaryRow = new SalaryRow(crew, salaryForCrews.get(crewId));
            salaryModel.addRow(salaryRow);

            BigDecimal salaryForPerson;
            BigDecimal count = BigDecimal.ZERO;

            for (Employee employee : reportShift.getEmployeeSet()) {
                boolean isDefaultCrew = false;
                boolean isAdditionCrew = false;

                if (employee.getDefaultCrew() != null) {
                    if (employee.getDefaultCrew().getId() == crew.getId()) {
                        isDefaultCrew = true;
                    }
                }

                Object obj = employee.getAdditionInfoByKey("crewList");
                if (obj != null) {
                    ArrayList<Long> crews = (ArrayList<Long>) obj;
                    isAdditionCrew = crews.contains(Long.valueOf(crew.getId()));
                }

                if (!isDefaultCrew && !isAdditionCrew) {
                    continue;
                }

                count = count.add(new BigDecimal("1"));
            }

            if (count.doubleValue() != 0) {
                salaryForPerson = Money.DIVIDE(salaryForCrews.get(crewId), count);
            } else {
                salaryForPerson = salaryForCrews.get(crewId);
            }

            for (Employee employee : reportShift.getEmployeeSet()) {
                boolean isDefaultCrew = false;
                boolean isAdditionCrew = false;

                if (employee.getDefaultCrew() != null) {
                    if (employee.getDefaultCrew().getId() == crew.getId()) {
                        isDefaultCrew = true;
                    }
                }

                Object obj = employee.getAdditionInfoByKey("crewList");
                if (obj != null) {
                    ArrayList<Long> crews = (ArrayList<Long>) obj;
                    isAdditionCrew = crews.contains(Long.valueOf(crew.getId()));
                }

                if (!isDefaultCrew && !isAdditionCrew) {
                    continue;
                }

                SalaryRow salaryRowEmployee = new SalaryRow(employee, salaryForPerson, BigDecimal.ZERO, false);
                salaryRowEmployee.setParent(salaryRow);
                salaryRow.addChild(salaryRowEmployee);
                salaryModel.addRow(salaryRowEmployee);
            }

        }
    }

    @Override
    public String getModel() {
        String model = "";
        switch (currentPage) {
            case PRE_DISTRIBUTION:
                model = getPreDistributionPanel();
                break;

            case DISTRIBUTION:
                model = getDistributionPanel();
                break;

            case DISTRIBUTION_CONFIRM:
                model = getDistributionConfirmPanel();
                break;

        }


        return model;
    }

    private String getDistributionConfirmPanel() {
        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Работник", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Начислено", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Удержано", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("К получению", String.class, false));
        macTableModel.setHeader(mth);

        String model = "<div style='width:97%;font-size:80%;padding:5px;"
                + "border-radius:6px;border: 1px solid gray;margin:3px;'>"
                + "<table width='100%' height='100%' border='0'>"
                + "<tr>"
                + "<td valign='middle'>Фонд з/п к распределению: "
                + "<strong>" + totalSalaryDistribution + "</strong> р."
                + "</td>"
                + "<td align='right'>"
                + btnBack.getModel() + btnPrint.getModel() + btnSave.getModel()
                + "</td>"
                + "</tr>"
                + "</table>"
                + "</div>";
        ArrayList<MacTableRow> rows = new ArrayList();
        int row = 0;

        Iterator<Long> it = salaryForCrews.keySet().iterator();
        while (it.hasNext()) {
            long crewId = it.next();
            Crew crew = core.getCrewById(crewId);
            if (crew == null) {
                continue;
            }

            MacTableRow crewTableRow = new MacTableRow();
            crewTableRow.addCell(new MacTableCell(getSession(), "<strong>" + crew.getName() + "</strong>", false));
            crewTableRow.addCell(new MacTableCell(getSession(), "<div align='right'><strong>" + salaryForCrews.get(crewId) + "</strong></div>", false));
            crewTableRow.addCell(new MacTableCell(getSession(), "<div align='center'>---</div>", false));
            crewTableRow.addCell(new MacTableCell(getSession(), "<div align='center'>---</div>", false));
            rows.add(crewTableRow);
            row++;

            for (Employee employee : reportShift.getEmployeeSet()) {
                boolean isDefaultCrew = false;
                boolean isSubCrew = false;
                MacTableRow employeeTableRow = new MacTableRow();

                Crew employeeCrew = employee.getDefaultCrew();
                if (employeeCrew == null) {
                    continue;
                }

                if (employeeCrew.getId() == crew.getId()) {
                    isDefaultCrew = true;
                }

                Object obj = employee.getAdditionInfoByKey("crewList");
                if (obj == null) {
                    if (!isDefaultCrew) {
                        continue;
                    }
                }

                if (obj != null) {
                    ArrayList<Long> crews = (ArrayList<Long>) obj;
                    for (long subCrewId : crews) {
                        if (subCrewId == crew.getId()) {
                            isSubCrew = true;
                        }
                    }
                }

                if (!isSubCrew && !isDefaultCrew) {
                    continue;
                }

                SalaryRow salaryRow = salaryModel.getRows().toArray(new SalaryRow[salaryModel.getRows().size()])[row];

                String img = "<img src='img/subbuttons/employee.png' />";
                if (employee.getRole().equals("foreman")) {
                    img = "<img src='img/subbuttons/master.png' />";
                }
                String employeeName = employee.getFullName();
                if (employeeName.equals("")) {
                    employeeName = employee.getShortName();
                }
                String employeeString = img + "&nbsp;" + employeeName;

                employeeTableRow.addCell(new MacTableCell(getSession(), employeeString, false));
                employeeTableRow.addCell(new MacTableCell(getSession(), salaryRow.getSum(), false));
                employeeTableRow.addCell(new MacTableCell(getSession(), salaryRow.getRetained(), false));

                final CheckBox checkBox = new CheckBox(getSession(), "");
                checkBox.setChecked(salaryRow.isFixed());
                checkBox.setValue("" + row);
                checkBox.setEnabled(false);
                employeeTableRow.addCell(new MacTableCell(getSession(), 
                        "<div align='center'>" + checkBox.getModel() + "</div>", false));

                rows.add(employeeTableRow);
                row++;
            }
        }

        macTableModel.setData(rows);

        return model + "<div style='overflow:auto; width:99%;'>"
                + macTableModel.getModel()
                + "</div>";

//        ArrayList list = new ArrayList();
//        int row = 0;
//        for (SalaryRow salaryRow : salaryModel.getRows()) {
//            MacTableRow macTableRow = new MacTableRow();
//            String img = "<img src='img/subbuttons/employee.png' />";
//            if (salaryRow.getEmployee().getRole().equals("foreman")) {
//                img = "<img src='img/subbuttons/master.png' />";
//            }
//
//            String employeeName = salaryRow.getEmployee().getFullName();
//            if (employeeName.equals("")) {
//                employeeName = salaryRow.getEmployee().getShortName();
//            }
//            String employeeString = img + "&nbsp;" + employeeName;
//
//            macTableRow.addCell(new MacTableCell(employeeString, false));
//            MacTableCell macTableCell = new MacTableCell(salaryRow.getSum(), false);
//            macTableCell.setStyle("font-weight:bold;");
//            macTableRow.addCell(macTableCell);
//
//            MacTableCell macTableCellRetained = new MacTableCell(salaryRow.getRetained(), false);
//            macTableCellRetained.setStyle("font-weight:bold;");
//            macTableRow.addCell(macTableCellRetained);
//
//            MacTableCell macTableCellOnHand = new MacTableCell(Money.SUBSTRACT(
//                    salaryRow.getSum().toString(),
//                    salaryRow.getRetained().toString()),
//                    false);
//            macTableRow.addCell(macTableCellOnHand);
//
//            list.add(macTableRow);
//            row++;
//        }
//
//        macTableModel.setData(list);
//
//        return model + "<div style='overflow:auto; width:99%;'>"
//                + macTableModel.getModel()
//                + "</div>";
//                //<br/><br/><br/><hr/>"
//                //+ salaryForCrewsInfo;
    }

    private String getPreDistributionPanel() {
        totalSalaryWithdrawal = BigDecimal.ZERO;
        try {
            totalSalaryWithdrawal = new BigDecimal(txtChange.getText().replaceAll(",", ".").trim());
        } catch (Exception e) {
            txtChange.setText("0");
        }

        totalSalaryDistribution = Money.ADD(
                salarySumFromShift.toString(),
                totalSalaryWithdrawal.toString());
        String model = ""
                + "<table width='100%' height='100%' border='0'>"
                + "<tr>"
                + "<td valign='middle' align='center' style='font-size:80%;'>"
                + "Фонд з/п к распределению по итогам смены: "
                + "<strong>" + salarySumFromShift + "</strong> р."
                + "<br/><br/>"
                + "Изменить фонд з/п на: "
                + txtChange.getModel()
                + "<br/><br/>";

        String style = "style='font-weight:bold;'";
        if (salarySumFromShift.doubleValue() != totalSalaryDistribution.doubleValue()) {
            style = " style='font-weight:bold; color:darkred;' ";
        }

        model += "Фонд з/п к распределению: <span " + style + ">" + totalSalaryDistribution + "</span> p."
                + "<br/><br/>"
                + btnToDistribute.getModel()
                + "</td>"
                + "</tr>"
                + "</table>";


        return model;
    }

    private String getDistributionPanel() {
        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Работник", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Начислено", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Удержано", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Фиксировать", Boolean.class, false));
        macTableModel.setHeader(mth);

        String model = "<div style='width:97%;font-size:80%;padding:5px;"
                + "border-radius:6px;border: 1px solid gray;margin:3px;'>"
                + "<table width='100%' height='100%' border='0'>"
                + "<tr>"
                + "<td valign='middle'>Фонд з/п к распределению: "
                + "<strong>" + totalSalaryDistribution + "</strong> р."
                + "</td>"
                + "<td align='right'>"
                + btnBack.getModel() + btnToDistributeConfirm.getModel()
                + "</td>"
                + "</tr>"
                + "</table>"
                + "</div>";

        ArrayList<MacTableRow> rows = new ArrayList();
        int row = 0;

        Iterator<Long> it = salaryForCrews.keySet().iterator();
        while (it.hasNext()) {
            long crewId = it.next();
            Crew crew = core.getCrewById(crewId);
            if (crew == null) {
                continue;
            }

            MacTableRow crewTableRow = new MacTableRow();
            crewTableRow.addCell(new MacTableCell(getSession(), "<strong>" + crew.getName() + "</strong>", false));
            crewTableRow.addCell(new MacTableCell(getSession(), "<div align='right'><strong>" + salaryForCrews.get(crewId) + "</strong></div>", false));
            crewTableRow.addCell(new MacTableCell(getSession(), "<div align='center'>---</div>", false));
            crewTableRow.addCell(new MacTableCell(getSession(), "<div align='center'>---</div>", false));
            rows.add(crewTableRow);
            row++;

            for (Employee employee : reportShift.getEmployeeSet()) {
                boolean isDefaultCrew = false;
                boolean isSubCrew = false;
                MacTableRow employeeTableRow = new MacTableRow();

                Crew employeeCrew = employee.getDefaultCrew();
                if (employeeCrew == null) {
                    continue;
                }

                if (employeeCrew.getId() == crew.getId()) {
                    isDefaultCrew = true;
                }

                Object obj = employee.getAdditionInfoByKey("crewList");
                if (obj == null) {
                    if (!isDefaultCrew) {
                        continue;
                    }
                }

                if (obj != null) {
                    ArrayList<Long> crews = (ArrayList<Long>) obj;
                    for (long subCrewId : crews) {
                        if (subCrewId == crew.getId()) {
                            isSubCrew = true;
                        }
                    }
                }

                if (!isSubCrew && !isDefaultCrew) {
                    continue;
                }

                SalaryRow salaryRow = salaryModel.getRows().toArray(new SalaryRow[salaryModel.getRows().size()])[row];

                String img = "<img src='img/subbuttons/employee.png' />";
                if (employee.getRole().equals("foreman")) {
                    img = "<img src='img/subbuttons/master.png' />";
                }
                String employeeName = employee.getFullName();
                if (employeeName.equals("")) {
                    employeeName = employee.getShortName();
                }
                String employeeString = img + "&nbsp;" + employeeName;

                employeeTableRow.addCell(new MacTableCell(getSession(), employeeString, false));
                if (salaryRow.isFixed()) {
                    MacTableCell macTableCell = new MacTableCell(getSession(), salaryRow.getSum(), false);
                    macTableCell.setStyle("font-weight:bold;");
                    employeeTableRow.addCell(macTableCell);

                } else {
                    employeeTableRow.addCell(new MacTableCell(getSession(), salaryRow.getSum(), true));
                }

                employeeTableRow.addCell(new MacTableCell(getSession(), salaryRow.getRetained(), true));

                final CheckBox checkBox = new CheckBox(getSession(), "");
                checkBox.setChecked(salaryRow.isFixed());
                checkBox.setValue("" + row);
                checkBox.addUIEventListener(new UIEventListener() {
                    @Override
                    public void event(UIEvent evt) {
                        salaryModel.setRowChecked(checkBox.isChecked(), Integer.parseInt(checkBox.getValue()));
                        WebKitEventBridge.getInstance().pushEventToComponent(
                                getSession(), SalaryReportRightPanel.class.getName(),
                                "{eventType:push, session:" + getSession() + ",  action:updateSalaryDistributionPanel}");
                    }
                });
                employeeTableRow.addCell(new MacTableCell(getSession(), 
                        "<div align='center'>" + checkBox.getModel() + "</div>", false));

                rows.add(employeeTableRow);
                row++;
            }
        }

        macTableModel.setData(rows);

        return model + "<div style='overflow:auto; width:99%;'>"
                + macTableModel.getModel()
                + "</div>";
        //+ "<br/><br/><br/><hr/>"
        //+ salaryForCrewsInfo;
    }

    private BigDecimal findEmployeeCount(Crew crew) {
        int count = 0;

        for (SalaryRow salaryRow : salaryModel.getRows()) {
            if (salaryRow.getEmployee() != null) {
                continue;
            }

            if (salaryRow.getCrew().getId() == crew.getId()) {
                count = salaryRow.getChildren().size();
                break;
            }
        }

        return new BigDecimal(count);
    }

    class SalaryRow {

        private Employee employee;
        private Crew crew;
        private BigDecimal sum = BigDecimal.ZERO;
        private BigDecimal retained = BigDecimal.ZERO;
        private boolean fixed = false;
        private SalaryRow parent;
        private ArrayList<SalaryRow> children = new ArrayList<>();

        public SalaryRow(Employee employee, BigDecimal sum, BigDecimal retained, boolean isFixed) {
            this.employee = employee;
            this.sum = sum;
            this.retained = retained;
            fixed = isFixed;
        }

        public SalaryRow(Crew crew, BigDecimal sum) {
            this.crew = crew;
            this.sum = sum;
        }

        public ArrayList<SalaryRow> getChildren() {
            return children;
        }

        public void addChild(SalaryRow child) {
            children.add(child);
        }

        public Crew getCrew() {
            return crew;
        }

        public SalaryRow getParent() {
            return parent;
        }

        public void setParent(SalaryRow parent) {
            this.parent = parent;
        }

        public Employee getEmployee() {
            return employee;
        }

        public void setEmployee(Employee employee) {
            this.employee = employee;
        }

        public boolean isFixed() {
            return fixed;
        }

        public void setFixed(boolean fixed) {
            this.fixed = fixed;
        }

        public BigDecimal getSum() {
            return sum;
        }

        public void setSum(BigDecimal sum) {
            this.sum = sum;
        }

        public BigDecimal getRetained() {
            return retained;
        }

        public void setRetained(BigDecimal retained) {
            this.retained = retained;
        }
    }

    class SalaryModel {

        private ArrayList<SalaryDistributionPanel.SalaryRow> rows = new ArrayList();
        private BigDecimal totalSalary;

        public SalaryModel(BigDecimal totalSalary) {
            this.totalSalary = totalSalary;
        }

        public void addRow(SalaryDistributionPanel.SalaryRow salaryRow) {
            rows.add(salaryRow);
        }

        public ArrayList<SalaryDistributionPanel.SalaryRow> getRows() {
            return rows;
        }

        public void recalculate(int rowIndex, int colIndex, BigDecimal value) {
            BigDecimal sumForCrew = rows.get(rowIndex).getParent().getSum();
            SalaryRow parentSalaryRow = rows.get(rowIndex).getParent();

            if (value.doubleValue() > sumForCrew.doubleValue()) {
                return;
            }

            if (colIndex == 2) {
                BigDecimal totalFixedSalary = BigDecimal.ZERO;
                int fixedCount = 0;
                for (SalaryRow salaryRow : rows) {
                    if (salaryRow.getParent() == null) {
                        continue;
                    }

                    if (salaryRow.getParent().getCrew().getId() != parentSalaryRow.getCrew().getId()) {
                        continue;
                    }

                    if (salaryRow.isFixed()) {
                        totalFixedSalary = Money.ADD(
                                totalFixedSalary.toString(),
                                salaryRow.getSum().toString());
                        fixedCount++;
                    }
                }

                if (value.doubleValue() > Money.SUBSTRACT(
                        sumForCrew,
                        totalFixedSalary).doubleValue()) {
                    return;
                }

                BigDecimal restMoney = Money.SUBSTRACT(sumForCrew, totalFixedSalary);
                restMoney = Money.SUBSTRACT(restMoney.toString(), value.toString());
                int restEmployee = parentSalaryRow.getChildren().size() - (fixedCount + 1);
                BigDecimal resultForRest = BigDecimal.ZERO;
                if (restEmployee > 0) {
                    resultForRest = Money.DIVIDE(restMoney.toString(), "" + restEmployee);
                }
                BigDecimal realRest = BigDecimal.ZERO;

                int row = 0;
                for (SalaryRow salaryRow : rows) {
                    if (salaryRow.getParent() == null) {
                        row++;
                        continue;
                    }

                    if (salaryRow.getParent().getCrew().getId() != parentSalaryRow.getCrew().getId()) {
                        row++;
                        continue;
                    }

                    if ((!salaryRow.isFixed()) && (row != rowIndex)) {
                        salaryRow.setSum(resultForRest);
                        realRest = Money.ADD(realRest.toString(), resultForRest.toString());
                    }
                    row++;
                }

                BigDecimal initiatorValue = Money.SUBSTRACT(sumForCrew, realRest);
                initiatorValue = Money.SUBSTRACT(initiatorValue.toString(), totalFixedSalary.toString());

                BigDecimal v = Money.SUBSTRACT(value, initiatorValue);
                if (v.doubleValue() > 0) {
                    row = 0;
                    for (SalaryRow salaryRow : rows) {
                        if (salaryRow.getParent() == null) {
                            row++;
                            continue;
                        }

                        if (salaryRow.getParent().getCrew().getId() != parentSalaryRow.getCrew().getId()) {
                            row++;
                            continue;
                        }

                        if ((!salaryRow.isFixed()) && (row != rowIndex)) {
                            salaryRow.setSum(Money.SUBSTRACT(salaryRow.getSum(), v));
                            break;
                        }
                        row++;
                    }
                }

                rows.get(rowIndex).setSum(value);
                btnToDistributeConfirm.setEnabled(true);

            } else if (colIndex == 3) {
                rows.get(rowIndex).setRetained(value);
            }
        }

        public BigDecimal getTotalSalary() {
            return totalSalary;
        }

        public void setRowChecked(boolean isChecked, int row) {
            ((SalaryDistributionPanel.SalaryRow) rows.get(row)).setFixed(isChecked);
        }
    }
}