/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.order.unpaid;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.uui.component.RightPanel;
import org.uui.component.WorkPanel;
import org.uui.plugin.Plugin;
import org.uui.plugin.RightPanelPlugin;
import org.uui.plugin.WorkPanelPlugin;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class UnpaidUUIPlugin implements Plugin, WorkPanelPlugin, RightPanelPlugin {

    private String session;
    private ArrayList<RightPanel> list;
    private UnpaidPanel unpaidPanel;

    @Override
    public void setSession(String session) {
        this.session = session;
    }

    public UnpaidUUIPlugin() {
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String getSelectorGroupName() {
        return "Расчеты";
    }

    @Override
    public String getWorkPanelName() {
        return "Неоплачено";
    }

    @Override
    public String getSelectorGroupImagePath() {
        return "icons" + File.separator + "selector_order_unpaid.png";
    }

    @Override
    public String getWorkPanelClassName() {
        return UnpaidPanel.class.getName();
    }

    @Override
    public WorkPanel getWorkPanel() {
        if (unpaidPanel == null) {
            unpaidPanel = new UnpaidPanel(session, CarssierDataBase.getDataBase());
        }
        return unpaidPanel;
    }

    @Override
    public int getSelectorGroupPosition() {
        return 120;
    }

    @Override
    public int getSelectorLabelPosition() {
        return 10;
    }

    @Override
    public String getGroupDescription() {
        return "Работа с заказами";
    }

    @Override
    public List<RightPanel> getRightPanels() {
        if (list == null) {
            list = new ArrayList<>();
            list.add(new UnpaidRightPanel(session));
        }

        return list;
    }

    @Override
    public String getPluginName() {
        return "Оплата заказов";
    }

    @Override
    public String getPluginDescription() {
        return "Редактирование и оплата заказов.";
    }
}
