package ru.sibek.plugin.account.operation;

import java.io.File;
import org.uui.component.WorkPanel;
import org.uui.plugin.Plugin;
import org.uui.plugin.WorkPanelPlugin;

public class AccountOperationUUIPlugin
        implements Plugin, WorkPanelPlugin {

    private AccountOperationPanel accountOperationPanel;

    @Override
    public String getPluginName() {
        return "Операции";
    }

    @Override
    public String getPluginDescription() {
        return "Набор хозяйственных операций";
    }

    @Override
    public String getSelectorGroupName() {
        return "Отчеты";
    }

    @Override
    public int getSelectorGroupPosition() {
        return 1000;
    }

    @Override
    public String getSelectorGroupImagePath() {
        return "icons" + File.separator + "selector_report.png";
    }

    @Override
    public int getSelectorLabelPosition() {
        return 500;
    }

    @Override
    public String getWorkPanelClassName() {
        return AccountOperationPanel.class.getName();
    }

    @Override
    public WorkPanel getWorkPanel() {
        if (this.accountOperationPanel == null) {
            this.accountOperationPanel = new AccountOperationPanel();
        }

        return this.accountOperationPanel;
    }

    @Override
    public String getWorkPanelName() {
        return "Операции";
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String getGroupDescription() {
        return "Работа с отчетами, сменами";
    }
}

/* Location:           /home/developer/bin/decompilers/jd-gui-0.3.3.linux.i686/Carssier/Carssier_Account_Operation.jar
 * Qualified Name:     ru.sibek.plugin.account.operation.AccountOperationUUIPlugin
 * JD-Core Version:    0.6.0
 */