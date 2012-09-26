/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.accountbook.trialbalance;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.uui.component.Button;
import org.uui.component.ComboBox;
import org.uui.component.Component;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import ru.sibek.business.ui.JSMediator;
//import org.uui.webkit.WebKitFrame;
//import org.uui.webkit.WebKitUtil;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public abstract class FilterAnalytic extends Component {

    private ComboBox cboType;
    private Button btnApply, btnEdit, btnRemove;
    public static final String SELECT = "SELECT", GOODS = "GOODS",
            SERVICE = "SERVICE", PARTNERS = "PARTNERS", CREWS = "CREWS",
            EMPLOYEES = "EMPLOYEES";
    public static final int SELECT_ALL = 0, SELECT_CHUNK = 1, SELECT_NOTHING = 2;
    private FilterAnalyticSelector filterAnalyticSelector;
    private int selectMode = SELECT_NOTHING;
    private ArrayList<Long> selectedId = new ArrayList<>();

    public FilterAnalytic(String sessionId) {
        super(sessionId);
        cboType = new ComboBox(sessionId);

        btnApply = new Button(getSession(), "Выбрать");
        btnApply.addUIEventListener(getApplyEvent());

        btnEdit = new Button(getSession(), "Редактировать");
        btnEdit.addUIEventListener(getEditEvent());
        btnEdit.setEnabled(false);

        btnRemove = new Button(getSession(), "Удалить");
        btnRemove.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                removeFilter(getMyIdentificator());
            }
        });

        filterAnalyticSelector = new FilterAnalyticSelector(sessionId) {

            @Override
            public void backAction(int selectMode, ArrayList<Long> selectedId) {
                FilterAnalytic.this.selectMode = selectMode;
                FilterAnalytic.this.selectedId = selectedId;
                FilterAnalytic.this.backAction();
            }
        };
    }

    public abstract void backAction();

    private UIEventListener getEditEvent() {
        UIEventListener listener = new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                try {
                    filterAnalyticSelector.setType(cboType.getSelectedValue());
                    switch (selectMode) {
                        case FilterAnalytic.SELECT_CHUNK:
                            filterAnalyticSelector.setSelectedItems(selectedId);
                            break;

                        case FilterAnalytic.SELECT_ALL:
                            filterAnalyticSelector.setSelectedAllItems(true);
                            break;

                        case FilterAnalytic.SELECT_NOTHING:
                            filterAnalyticSelector.setSelectedAllItems(false);
                            break;
                    }

                    JSMediator.setSliderPanel(getSession(), "trialBalanceAnalytic", 
                            filterAnalyticSelector.getModel(), "right");
                    
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        };

        return listener;
    }

    public void setFilterType(String filterType) {
        cboType.setSelectedValue(filterType);
    }

    public void setSelectedId(ArrayList<Long> selectedId) {
        this.selectedId = selectedId;
    }

    public void setSelectMode(int selectMode) {
        this.selectMode = selectMode;
    }

    public void fixControls() {
        cboType.setEnabled(false);
        btnApply.setEnabled(false);
        btnEdit.setEnabled(true);
    }

    private UIEventListener getApplyEvent() {
        UIEventListener listener = new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                if (cboType.getSelectedIndex() > 0) {
                    fixControls();
                    filterSelected();
                }
            }
        };

        return listener;
    }

    /**
     *
     * @return filter type <ul> <li>SELECT</li> <li>GOODS</li> <li>SERVICE</li>
     * <li>PARTNERS</li> <li>CREWS</li> <li>EMPLOYEES</li> </ul>
     */
    public String getFilterType() {
        return cboType.getSelectedValue();
    }

    /**
     *
     * @return select mode <ul> <li>SELECT_ALL</li> <li>SELECT_CHUNK</li>
     * <li>SELECT_NOTHING</li> </ul>
     */
    public int getSelectMode() {
        return selectMode;
    }

    /**
     *
     * @return selected id of data base objects<br/> It make sense only when id
     * select mode is SELECT_CHUNK
     */
    public ArrayList<Long> getSelectedId() {
        return selectedId;
    }

    public abstract void filterSelected();

    public void setAvailableFilters(LinkedHashMap<String, String> filtersMap) {
        cboType.setItems(filtersMap);
    }

    public void refresh() {
        JSMediator.refreshElement(getSession(), "filterAnalytic", getModel());
    }

    private String getMyIdentificator() {
        return getIdentificator();
    }

    public abstract void removeFilter(String identificator);

    @Override
    public String getModel() {
        String model = ""
                + "<div class='filterAnalytic'>"
                + cboType.getModel()
                + btnApply.getModel()
                + btnEdit.getModel()
                + btnRemove.getModel()
                + "</div>";

        return model;
    }

    @Override
    public void setSession(String session) {
        super.setSession(session);
        filterAnalyticSelector.setSession(session);
    }
    
    
}
