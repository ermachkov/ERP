/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.table;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.datetime.DateTime;
import org.ubo.json.JSONObject;
import org.uui.component.Button;
import org.uui.component.Calendar;
import org.uui.component.ComboBox;
import org.uui.component.Component;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.webkit.WebKitComponent;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public abstract class MacTableNavigator extends Component {

    private Button btnFastBackward, btnBackward, btnForward, btnFastForward,
            btnFilter, btnRefresh;
    private int rowsOnPage = 50, totalRowCount, currentPage = 0;
    public static final int FAST_BACKWARD = 0, BACKWARD = 1, FORWARD = 2,
            FAST_FORWARD = 3, REFRESH = 4, FILTER = 5, CALENDAR = 6;
    private Calendar calendarStart, calendarEnd;
    private ComboBox cboHourStart, cboMinuteStart, cboHourEnd, cboMinuteEnd;
    private boolean dateSelectorEnable = false, filterVisible = false,
            isFilterVisibleAlways = false, isRefreshButtonVisible = true,
            isFilterButtonVisible = true;
    private WebKitComponent filterPanel;

    public MacTableNavigator(String sessionId) {
        super(sessionId);
        calendarStart = new Calendar(sessionId, new Date(), "dd.MM.yyyy");
        calendarStart.setStyle("text-align:center;font-weight:bold;width:90px;font-size:10px;");
        calendarStart.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                change(MacTableNavigator.CALENDAR);
            }
        });

        calendarEnd = new Calendar(sessionId, new Date(), "dd.MM.yyyy");
        calendarEnd.setStyle("text-align:center;font-weight:bold;width:90px;font-size:10px;");
        calendarEnd.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                change(MacTableNavigator.CALENDAR);
            }
        });

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

        cboHourStart = new ComboBox(sessionId, hourMap);
        cboHourStart.setSelectedIndex(0);
        cboHourStart.setStyle("font-size:10px;");

        cboMinuteStart = new ComboBox(sessionId, minuteMap);
        cboMinuteStart.setSelectedIndex(0);
        cboMinuteStart.setStyle("font-size:10px;");

        cboHourEnd = new ComboBox(sessionId, hourMap);
        cboHourEnd.setSelectedIndex(23);
        cboHourEnd.setStyle("font-size:10px;");

        cboMinuteEnd = new ComboBox(sessionId, minuteMap);
        cboMinuteEnd.setSelectedIndex(59);
        cboMinuteEnd.setStyle("font-size:10px;");

        btnFastBackward = new Button(sessionId, "<<");
        btnFastBackward.setCssClass("macTableNavigatorButton");
        btnFastBackward.setStyle(
                "-webkit-border-top-right-radius: 0;"
                + "-moz-border-radius-topright: 0;"
                + "border-top-right-radius: 0;"
                + "-webkit-border-bottom-right-radius: 0;"
                + "-moz-border-radius-bottomright: 0;"
                + "border-bottom-right-radius: 0;");
        btnFastBackward.setEnabled(false);
        btnFastBackward.setStyle("font-size:10px; font-weight:bold;");
        btnFastBackward.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                switcher(MacTableNavigator.FAST_BACKWARD);
            }
        });

        btnBackward = new Button(sessionId, "<");
        btnBackward.setCssClass("macTableNavigatorButton");
        btnBackward.setEnabled(false);
        btnBackward.setStyle("font-size:10px; font-weight:bold;");
        btnBackward.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                switcher(MacTableNavigator.BACKWARD);
            }
        });

        btnForward = new Button(sessionId, ">");
        btnForward.setCssClass("macTableNavigatorButton");
        btnForward.setStyle("font-size:10px; font-weight:bold;");
        btnForward.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                switcher(MacTableNavigator.FORWARD);
            }
        });

        btnFastForward = new Button(sessionId, ">>");
        btnFastForward.setCssClass("macTableNavigatorButton");
        btnFastForward.setStyle("font-size:10px; font-weight:bold;");
        btnFastForward.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                switcher(MacTableNavigator.FAST_FORWARD);
            }
        });

        btnFilter = new Button(sessionId, "");
        btnFilter.setImage("img/subbuttons/filter.png");
        btnFilter.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                change(MacTableNavigator.FILTER);
                filterVisible = true;
            }
        });

        btnRefresh = new Button(sessionId, "");
        btnRefresh.setImage("img/subbuttons/refresh.png");
        btnRefresh.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                change(MacTableNavigator.REFRESH);
            }
        });
    }

    public boolean isDateSelectorEnabled() {
        return dateSelectorEnable;
    }

    public void setDateSelectorEnabled(boolean dateSelectorEnable) {
        this.dateSelectorEnable = dateSelectorEnable;
    }

    public void resetPage() {
        btnBackward.setEnabled(false);
        btnFastBackward.setEnabled(false);
        btnForward.setEnabled(true);
        btnFastForward.setEnabled(true);
        currentPage = 0;
    }

    private void switcher(int btnType) {
        switch (btnType) {
            case FAST_BACKWARD:
                btnBackward.setEnabled(false);
                btnFastBackward.setEnabled(false);
                btnForward.setEnabled(true);
                btnFastForward.setEnabled(true);
                currentPage = 0;
                change(MacTableNavigator.FAST_BACKWARD);
                break;

            case BACKWARD:
                currentPage--;
                btnForward.setEnabled(true);
                btnFastForward.setEnabled(true);
                currentPage = currentPage < 0 ? 0 : currentPage;
                if (currentPage == 0) {
                    btnBackward.setEnabled(false);
                    btnFastBackward.setEnabled(false);
                }
                change(MacTableNavigator.BACKWARD);
                break;

            case FAST_FORWARD:
                btnBackward.setEnabled(true);
                btnFastBackward.setEnabled(true);

                int maxPage = totalRowCount / rowsOnPage;
                if ((totalRowCount % rowsOnPage) == 0) {
                    maxPage--;
                }

                currentPage = maxPage;
                btnForward.setEnabled(false);
                btnFastForward.setEnabled(false);
                change(MacTableNavigator.FAST_FORWARD);
                break;

            case FORWARD:
                btnBackward.setEnabled(true);
                btnFastBackward.setEnabled(true);
                currentPage++;
                maxPage = totalRowCount / rowsOnPage;
                //if ((totalRowCount % rowsOnPage) > 0) {
                //    maxPage++;
                //}

                if (((currentPage * rowsOnPage) + rowsOnPage) > totalRowCount) {
                    currentPage = maxPage;
                    btnForward.setEnabled(false);
                    btnFastForward.setEnabled(false);
                }
                change(MacTableNavigator.FORWARD);
                break;
        }
    }

    public abstract void change(int event);

    public int getStartRow() {
        return currentPage * rowsOnPage;
    }

    public int getEndRow() {
        return (currentPage * rowsOnPage) + rowsOnPage;
    }

    public void setRowsCount(int totalRowCount) {
        this.totalRowCount = totalRowCount;
    }

    @Override
    public String getModel() {
        int start = currentPage * rowsOnPage;
        int end = start + rowsOnPage;
        start++;

        String model;
        String sFilterPanel = isFilterVisibleAlways ? getFilterPanel().getModel() : "";
        if (dateSelectorEnable) {
            String dateSelector = calendarStart.getModel() + cboHourStart.getModel()
                    + cboMinuteStart.getModel() + "&nbsp;" + calendarEnd.getModel()
                    + cboHourEnd.getModel() + cboMinuteEnd.getModel();

            model = "<table width='100%'>"
                    + "<tr>"
                    + "<td valign='middle'>"
                    + dateSelector
                    + "</td>"
                    + "<td valign='middle' align='right'>"
                    + "<span style='font-size:10px; font-weight:bold;'>"
                    + start + "-" + end + " из " + totalRowCount
                    + "</span>"
                    + "&nbsp;"
                    + btnFastBackward.getModel()
                    + btnBackward.getModel()
                    + btnForward.getModel()
                    + btnFastForward.getModel();

            if (!isFilterVisibleAlways) {
                if (isFilterButtonVisible) {
                    model += btnFilter.getModel();
                }
            }

            if (isRefreshButtonVisible) {
                model += btnRefresh.getModel();
            }

            model += "</td>"
                    + "</tr>"
                    + "</table>"
                    + "<div id='" + getIdentificator() + "' style='border-top: "
                    + "1px dotted gray;'>"
                    + sFilterPanel
                    + "</div>";

        } else {
            model = "<div align='right' style='width:100%; font-size:10px; font-weight:bold;'>"
                    + start + "-" + end + " из " + totalRowCount
                    + "&nbsp;"
                    + btnFastBackward.getModel()
                    + btnBackward.getModel()
                    + btnForward.getModel()
                    + btnFastForward.getModel();

            if (!isFilterVisibleAlways) {
                if (isFilterButtonVisible) {
                    model += btnFilter.getModel();
                }
            }

            if (isRefreshButtonVisible) {
                model += btnRefresh.getModel();
            }

            model += "</div>"
                    + "<div id='" + getIdentificator() + "' style='border-top: "
                    + "1px dotted gray;'>"
                    + sFilterPanel
                    + "</div>";
        }


        return model;
    }

    public void setFilterPanelShowingAlways(boolean isShow) {
        isFilterVisibleAlways = isShow;
    }

    public boolean isFilterPanelShowingAlways() {
        return isFilterVisibleAlways;
    }

    public int getRowsOnPage() {
        return rowsOnPage;
    }

    public void setRowsOnPage(int rowsOnPage) {
        this.rowsOnPage = rowsOnPage;
    }

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

    public void setFilterPanel(WebKitComponent filterPanel) {
        this.filterPanel = filterPanel;
    }

    public WebKitComponent getFilterPanel() {
        return filterPanel;
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
            if (jsonObject.getString("eventType").equals("click")) {
                if (jsonObject.getString("value").equals("show")) {
                    filterVisible = true;
                }

                if (jsonObject.getString("value").equals("hide")) {
                    filterVisible = false;
                }
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, json, e);
        }
    }

    public boolean isFilterVisible() {
        return filterVisible;
    }

    public void setRefreshButtonVisible(boolean isVisible) {
        isRefreshButtonVisible = isVisible;
    }

    public void setFilterButtonVisible(boolean isVisible) {
        isFilterButtonVisible = isVisible;
    }

    public boolean isFilterButtonVisible() {
        return isFilterButtonVisible;
    }

    public boolean isRefreshButtonVisible() {
        return isRefreshButtonVisible;
    }
}
