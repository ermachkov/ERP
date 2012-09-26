/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.accountbook.trialbalance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.ubo.accountbook.AnalyticFilter;
import org.ubo.accountbook.AnalyticFilterItem;
import org.uui.component.Button;
import org.uui.component.Component;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import ru.sibek.business.ui.JSMediator;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public abstract class FilterAnalyticPanel extends Component {
    
    private Button btnAddFilter, btnBack;
    private ArrayList<FilterAnalytic> filters = new ArrayList<>();
    
    public FilterAnalyticPanel(String sessionId) {
        super(sessionId);
        btnAddFilter = new Button(getSession(), "Добавить фильтр");
        btnAddFilter.addUIEventListener(getAddFilterEvent());
        btnBack = new Button(getSession(), "<< Назад");
        btnBack.addUIEventListener(getBackEvent());
    }
    
    public abstract void backAction(ArrayList<FilterAnalytic> filters);
    
    public void clear() {
        filters.clear();
    }
    
    public void setAnalyticFilter(AnalyticFilter analyticFilter) {
        filters.clear();
        
        for (AnalyticFilterItem analyticFilterItem : analyticFilter.getAnalyticFilterItems()) {
            FilterAnalytic filterAnalytic = new FilterAnalytic(getSession()) {
                @Override
                public void removeFilter(String identificator) {
                    removeAnalyticFilter(identificator);
                    setAddEnabled(true);
                    FilterAnalyticPanel.this.refresh();
                }
                
                @Override
                public void filterSelected() {
                    setAddEnabled(true);
                    FilterAnalyticPanel.this.refresh();
                }
                
                @Override
                public void backAction() {
                    JSMediator.setSliderPanel(getSession(), "trialBalanceAnalytic", FilterAnalyticPanel.this.getModel(), "left");
                }
            };
            
            filterAnalytic.setAvailableFilters(getAvailableFilters());
            filterAnalytic.setFilterType(analyticFilterItem.getFilterType());
            filterAnalytic.setSelectedId(analyticFilterItem.getSelectedId());
            filterAnalytic.setSelectMode(analyticFilterItem.getSelectedMode());
            filterAnalytic.fixControls();
            filterAnalytic.setSession(getSession());
            
            filters.add(filterAnalytic);
        }
        
    }
    
    private UIEventListener getAddFilterEvent() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                FilterAnalytic filterAnalytic = new FilterAnalytic(getSession()) {
                    @Override
                    public void removeFilter(String identificator) {
                        removeAnalyticFilter(identificator);
                        setAddEnabled(true);
                        FilterAnalyticPanel.this.refresh();
                    }
                    
                    @Override
                    public void filterSelected() {
                        setAddEnabled(true);
                        FilterAnalyticPanel.this.refresh();
                    }
                    
                    @Override
                    public void backAction() {
                        JSMediator.setSliderPanel(getSession(), "trialBalanceAnalytic", FilterAnalyticPanel.this.getModel(), "left");
                    }
                };
                
                filterAnalytic.setSession(getSession());
                
                if (getAvailableFilters().size() > 1) {
                    filters.add(filterAnalytic);
                    filterAnalytic.setAvailableFilters(getAvailableFilters());
                    setAddEnabled(false);
                    refresh();
                    
                } else {
                    setAddEnabled(false);
                }
            }
        };
        
        return listener;
    }
    
    private void setAddEnabled(boolean isEnabled) {
        btnAddFilter.setEnabled(isEnabled);
    }
    
    private LinkedHashMap<String, String> getAvailableFilters() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        
        Map<String, String> dic = new HashMap<>();
        dic.put(FilterAnalytic.SELECT, "-Выбрать-");
        dic.put(FilterAnalytic.SERVICE, "Услуги");
        dic.put(FilterAnalytic.GOODS, "Товары");
        dic.put(FilterAnalytic.PARTNERS, "Клиенты");
        dic.put(FilterAnalytic.CREWS, "Бригады");
        dic.put(FilterAnalytic.EMPLOYEES, "Работники");
        
        ArrayList<String> availableList = new ArrayList<>();
        availableList.add(FilterAnalytic.SELECT);
        availableList.add(FilterAnalytic.SERVICE);
        availableList.add(FilterAnalytic.GOODS);
        availableList.add(FilterAnalytic.PARTNERS);
        availableList.add(FilterAnalytic.CREWS);
        availableList.add(FilterAnalytic.EMPLOYEES);
        
        ArrayList<String> removeList = new ArrayList<>();
        for (FilterAnalytic filterAnalytic : filters) {
            if (availableList.contains(filterAnalytic.getFilterType())) {
                removeList.add(filterAnalytic.getFilterType());
            }
        }
        
        availableList.removeAll(removeList);
        
        for (String s : availableList) {
            map.put(dic.get(s), s);
        }
        
        return map;
    }
    
    private void removeAnalyticFilter(String identificator) {
        int index = -1, i = 0;
        for (FilterAnalytic filterAnalytic : filters) {
            if (filterAnalytic.getIdentificator().equals(identificator)) {
                index = i;
                break;
            }
            i++;
        }
        
        if (index >= 0) {
            filters.remove(index);
        }
    }
    
    private UIEventListener getBackEvent() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                FilterAnalyticPanel.this.backAction(filters);
            }
        };
        
        return listener;
    }
    
    private void refresh() {
        JSMediator.refreshElement(getSession(), "filterAnalyticPanel", getModel());
    }
    
    @Override
    public String getModel() {
        String sFilters = "";
        for (FilterAnalytic filterAnalytic : filters) {
            sFilters += ""
                    + "<div style='float:left'>"
                    + filterAnalytic.getModel()
                    + "</div>";
        }
        
        String model = ""
                + "<div style='width:100%;' id='filterAnalyticPanel'>"
                + "<div style='width:100%; border-bottom: 1px solid darkgrey; height:30px;'>"
                + "<div style='float:left;'>" + btnBack.getModel() + "</div>"
                + "<div style='float:right;'>" + btnAddFilter.getModel() + "</div>"
                + "</div>"
                + "<div style='width:100%'>"
                + sFilters
                + "</div>"
                + "</div>";
        
        return model;
    }
}
