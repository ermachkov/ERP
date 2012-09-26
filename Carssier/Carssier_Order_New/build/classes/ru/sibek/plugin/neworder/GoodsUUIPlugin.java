package ru.sibek.plugin.neworder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.uui.component.RightPanel;
import org.uui.component.WorkPanel;
import org.uui.plugin.Plugin;
import org.uui.plugin.RightPanelPlugin;
import org.uui.plugin.WorkPanelPlugin;
import ru.sibek.database.CarssierDataBase;

public class GoodsUUIPlugin implements Plugin, WorkPanelPlugin, RightPanelPlugin {

    private GoodsPanel goodsPanel;
    private GoodsBasketPanel goodsBasketPanel;
    private ArrayList<RightPanel> panels;
    private String session;

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public String getSelectorGroupName() {
        return "Заказы";
    }

    @Override
    public String getWorkPanelClassName() {
        return GoodsPanel.class.getName();
    }

    @Override
    public int getSelectorGroupPosition() {
        return 100;
    }

    @Override
    public String getSelectorGroupImagePath() {
        return "icons" + File.separator + "selector_order_new.png";
    }

    @Override
    public int getSelectorLabelPosition() {
        return 10;
    }

    @Override
    public WorkPanel getWorkPanel() {
        goodsPanel = new GoodsPanel(session, CarssierDataBase.getDataBase());
        
        goodsBasketPanel = new GoodsBasketPanel(session);
        
        panels = new ArrayList<>();
        goodsBasketPanel = new GoodsBasketPanel(session);
        panels.add(goodsBasketPanel);
        
        goodsPanel.addBasketPanel(goodsBasketPanel);
        return goodsPanel;
    }

    @Override
    public List<RightPanel> getRightPanels() {
        return panels;
    }

    public String getRightPanelWorkPanelClassName() {
        return GoodsPanel.class.getName();
    }

    @Override
    public String getWorkPanelName() {
        return "Оформить";
    }

    @Override
    public String getGroupDescription() {
        return "Работа с заказами";
    }

    @Override
    public String getPluginName() {
        return "Оформление заказов";
    }

    @Override
    public String getPluginDescription() {
        return "Создание новых заказов. Отправка заказов в работу, отложенные, черновики.";
    }

    @Override
    public void setSession(String session) {
        this.session = session;
    }
}
