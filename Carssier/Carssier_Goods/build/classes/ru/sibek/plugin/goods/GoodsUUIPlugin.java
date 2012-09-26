/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.goods;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.uui.component.RightPanel;
import org.uui.component.WorkPanel;
import org.uui.plugin.Plugin;
import org.uui.plugin.RightPanelPlugin;
import org.uui.plugin.WorkPanelPlugin;
import org.uui.ribbon.RibbonButton;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author Zubanov Dmitry, Pechenko Anton aka parilo, forpost78 aaaaaat gmail doooooot com (C)
 * Created 16.12.2010
 */
public class GoodsUUIPlugin implements Plugin, WorkPanelPlugin, RightPanelPlugin {

    private String session;
    private GoodsPanel goodsPanel;
    private ArrayList<RightPanel> panels;

    @Override
    public void setSession(String session) {
        this.session = session;
    }

    public GoodsUUIPlugin() {
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public String getSelectorGroupName() {
        return "Справочники";
    }

    @Override
    public String getWorkPanelClassName() {
        return GoodsPanel.class.getName();
    }

    @Override
    public int getSelectorGroupPosition() {
        return 950;
    }

    @Override
    public String getSelectorGroupImagePath() {
        return "icons" + File.separator + "selector_goods.png";
    }

    @Override
    public int getSelectorLabelPosition() {
        return 10;
    }

    public RibbonButton getSelectorLabel() {
        return null;
    }

    @Override
    public synchronized WorkPanel getWorkPanel() {
        if (goodsPanel == null) {
            goodsPanel = new GoodsPanel(session, CarssierDataBase.getDataBase());
        }
        return goodsPanel;
    }

    @Override
    public List<RightPanel> getRightPanels() {
        if (panels == null) {
            panels = new ArrayList<>();
            panels.add(new GoodsCard(session));
        }
        return panels;
    }

    @Override
    public String getWorkPanelName() {
        return "Товары";
    }

    @Override
    public String getGroupDescription() {
        return "Работа со справочниками";
    }

    @Override
    public String getPluginName() {
        return "Товары";
    }

    @Override
    public String getPluginDescription() {
        return "Создание, редактирование, удаление товаров. Распределение товаров по группам.";
    }
}
