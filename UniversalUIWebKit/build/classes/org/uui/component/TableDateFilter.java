/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import java.util.Date;
import java.util.LinkedHashMap;
import org.ubo.datetime.DateTime;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public abstract class TableDateFilter extends Component {

    private Calendar calendarStart;
    private Calendar calendarEnd;
    private ComboBox cboHourStart;
    private ComboBox cboMinuteStart;
    private ComboBox cboHourEnd;
    private ComboBox cboMinuteEnd;
    private TableRowNavigator tableRowNavigator;
    private Button btnShow;
    private TextField txtFilter;
    private boolean isTableRowNavigatorEnable = true,
            isTextFilterEnable = true, isTimeSelectorEnable = true;

    public TableDateFilter(String sessionId) {
        super(sessionId);
        calendarStart = new Calendar(getSession(), new Date(), "dd.MM.yyyy");
        calendarStart.setStyle("text-align:center;width:80%;font-weight:bold;");

        calendarEnd = new Calendar(getSession(), new Date(), "dd.MM.yyyy");
        calendarEnd.setStyle("text-align:center;width:80%;font-weight:bold;");

        LinkedHashMap hourMap = new LinkedHashMap();
        for (int i = 0; i < 24; i++) {
            String h = "" + i;
            if (i < 10) {
                h = "0" + h;
            }
            hourMap.put(h, h);
        }

        LinkedHashMap minuteMap = new LinkedHashMap();
        for (int i = 0; i < 60; i++) {
            String h = "" + i;
            if (i < 10) {
                h = "0" + h;
            }
            minuteMap.put(h, h);
        }

        cboHourStart = new ComboBox(getSession(), hourMap);
        cboHourStart.setSelectedIndex(0);

        cboMinuteStart = new ComboBox(getSession(), minuteMap);
        cboMinuteStart.setSelectedIndex(0);

        cboHourEnd = new ComboBox(getSession(), hourMap);
        cboHourEnd.setSelectedIndex(23);

        cboMinuteEnd = new ComboBox(getSession(), minuteMap);
        cboMinuteEnd.setSelectedIndex(59);

        tableRowNavigator = new TableRowNavigator(getSession());

        btnShow = new Button(getSession(), "Показать");
        btnShow.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                btnShowPress(getDateStart(), getDateEnd());
            }
        });

        txtFilter = new TextField(getSession());
        txtFilter.setStyle("width:80%;");
        txtFilter.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                filterChange(txtFilter.getText());
            }
        });

    }

    public abstract void filterChange(String text);

    public Date getDateStart() {
        String d = DateTime.getFormatedDate("yyyy-MM-dd", calendarStart.getDate());
        d += " " + cboHourStart.getSelectedValue() + ":" + cboMinuteStart.getSelectedValue() + ":00";
        return DateTime.getDateFromString("yyyy-MM-dd HH:mm:ss", d);
    }

    public Date getDateEnd() {
        String d = DateTime.getFormatedDate("yyyy-MM-dd", calendarEnd.getDate());
        d += " " + cboHourEnd.getSelectedValue() + ":" + cboMinuteEnd.getSelectedValue() + ":00";
        return DateTime.getDateFromString("yyyy-MM-dd HH:mm:ss", d);
    }

    public String getFilterText() {
        return txtFilter.getText();
    }

    public abstract void btnShowPress(Date dateStart, Date dateEnd);

    @Override
    public String getModel() {
        String model = ""
                + "<table width='100%' height='38'>"
                + "<tr>"
                + "<td class='dateTimePicker' style='white-space:nowrap;'>"
                + "<span style='font-size:80%;'>c:&nbsp;</span>"
                + calendarStart.getModel();

        if (isTimeSelectorEnable) {
            model += cboHourStart.getModel()
                    + cboMinuteStart.getModel();
        }

        model += "</td>"
                + "<td class='dateTimePicker' style='white-space:nowrap;'>"
                + "<span style='font-size:80%;'>по:&nbsp;</span>"
                + calendarEnd.getModel();

        if (isTimeSelectorEnable) {
            model += cboHourEnd.getModel()
                    + cboMinuteEnd.getModel();
        }

        model += "</td>";

        if (isTextFilterEnable) {
            model += "<td class='dateTimePicker' width='25%' align='center'>"
                    + txtFilter.getModel()
                    + "</td>";
        }

        if (isTableRowNavigatorEnable) {
            model += "<td align='center' class='tableNavigator'  style='white-space:nowrap;'>"
                    + tableRowNavigator.getModel()
                    + "</td>";
        }

        model += "<td align='center'>"
                + btnShow.getModel()
                + "</td>"
                + "</tr>"
                + "</table>";

        return model;
    }

    public void setTableRowNavigatorEnable(boolean isEnable) {
        isTableRowNavigatorEnable = isEnable;
    }

    public void setTextFilterEnable(boolean isEnable) {
        isTextFilterEnable = isEnable;
    }

    public boolean isTableRowNavigatorEnable() {
        return isTableRowNavigatorEnable;
    }

    public boolean isTextFilterEnable() {
        return isTextFilterEnable;
    }

    public void setTimeSelectorEnable(boolean isEnable) {
        isTimeSelectorEnable = isEnable;
    }

    public boolean isTimeSelectorEnable() {
        return isTimeSelectorEnable;
    }

    public Calendar getCalendarEnd() {
        return calendarEnd;
    }

    public Calendar getCalendarStart() {
        return calendarStart;
    }

    public ComboBox getCboHourEnd() {
        return cboHourEnd;
    }

    public ComboBox getCboHourStart() {
        return cboHourStart;
    }

    public ComboBox getCboMinuteEnd() {
        return cboMinuteEnd;
    }

    public ComboBox getCboMinuteStart() {
        return cboMinuteStart;
    }

    public TextField getTxtFilter() {
        return txtFilter;
    }
}
