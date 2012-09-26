/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.webpriceeditor;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.goods.Goods;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.service.Service;
import org.ubo.tree.*;
import org.ubo.utils.Result;
import org.ubo.www.PriceACL;
import org.uui.component.CheckBox;
import org.uui.component.WorkPanel;
import org.uui.event.NavigatorChangeListener;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.table.*;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.ui.MainFrame;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class WebPriceEditorPanel extends WorkPanel {

    private MacTableModel macTableModel;
    private CarssierCore core;
    private ArrayList<CheckBox> allowShowCheckBoxes = new ArrayList<>();
    private ArrayList<CheckBox> allowUseBasketCheckBoxes = new ArrayList<>();

    public WebPriceEditorPanel(String sessionId) {
        super(sessionId);
        core = CarssierCore.getInstance();
        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Наименование", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Цена", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Показывать на сайте", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Разрешить добавление в корзину", String.class, false));

        macTableModel = new MacTableModel(sessionId, true);
        macTableModel.setHeader(mth);
        macTableModel.setCssClass("leftMacTable");
        macTableModel.setId("wwwPriceTable");

        macTableModel.getMacTableNavigator().setRowsOnPage(100);
        macTableModel.addNavigatorChangeListener(new NavigatorChangeListener() {

            @Override
            public void event(int event) {
                    JSMediator.setWorkPanel(getSession(), getModel());
            }
        });
    }

    private void refreshTable() {
        allowShowCheckBoxes.clear();
        allowUseBasketCheckBoxes.clear();
        WebKitEventBridge.getInstance().cleanMacTable(getSession(), macTableModel);

        Result r = core.getTreeGoodsAndService();
        if (r.isError()) {
            JSMediator.alert(getSession(), r.getReason());
            return;
        }

        final ArrayList<MacTableRow> rows = new ArrayList<>();
        TreeBasic tree = (TreeBasic) r.getObject();
        TreeNodeWalker tnw = new TreeNodeWalker(tree.getRootFolder()) {

            @Override
            public TreeNodeVisitResult visitFolder(TreeFolder folder) {
                MacTableRow row = new MacTableRow();
                row.addCell(new MacTableCell(getSession(), 
                        "<strong>" + folder.getPath().replaceAll("//GoodsAndService", "") + "</strong>",
                        false));
                row.addCell(new MacTableCell(getSession(), "", false));
                row.addCell(new MacTableCell(getSession(), "", false));
                row.addCell(new MacTableCell(getSession(), "", false));
                rows.add(row);
                return TreeNodeVisitResult.CONTINUE;
            }

            @Override
            public TreeNodeVisitResult visitLeaf(final TreeLeaf leaf) {
                if (leaf.getContainer().getClassName().equals(Goods.class.getName())) {
                    Result r = core.getGoods(leaf.getContainer().getId());
                    if (r.isError()) {
                        return TreeNodeVisitResult.CONTINUE;
                    }
                    Goods goods = (Goods) r.getObject();

                    MacTableRow row = new MacTableRow();
                    row.addCell(new MacTableCell(getSession(), goods.getShortName(), false));
                    row.addCell(new MacTableCell(getSession(), 
                            "<div align='right' style='font-weight:bold;'>"
                            + goods.getSalePrice(CarssierDataBase.getDataBase()) 
                            + "</div>", 
                            false));

                    final CheckBox chkAllowShow = new CheckBox(getSession(), "");
                    allowShowCheckBoxes.add(chkAllowShow);
                    Result res = core.getPriceACL(((TreeLeafBasic) leaf).getId());
                    if(res.isError()){
                        chkAllowShow.setChecked(false);
                    } else {
                        chkAllowShow.setChecked(((PriceACL)res.getObject()).isAllowShow());
                    }
                    
                    chkAllowShow.addUIEventListener(new UIEventListener() {

                        @Override
                        public void event(UIEvent evt) {
                            core.setPriceACLShow(((TreeLeafBasic) leaf).getId(), chkAllowShow.isChecked());
                        }
                    });
                    row.addCell(new MacTableCell(getSession(), 
                            "<div align='center'>"
                            + chkAllowShow.getModel()
                            + "</div>", false));

                    final CheckBox chkAllowUse = new CheckBox(getSession(), "");
                    allowUseBasketCheckBoxes.add(chkAllowUse);
                    res = core.getPriceACL(((TreeLeafBasic) leaf).getId());
                    if(res.isError()){
                        chkAllowUse.setChecked(false);
                    } else {
                        chkAllowUse.setChecked(((PriceACL)res.getObject()).isAllowUse());
                    }
                    chkAllowUse.addUIEventListener(new UIEventListener() {

                        @Override
                        public void event(UIEvent evt) {
                            core.setPriceACLUse(((TreeLeafBasic) leaf).getId(), chkAllowUse.isChecked());
                        }
                    });
                    row.addCell(new MacTableCell(getSession(), 
                            "<div align='center'>"
                            + chkAllowUse.getModel()
                            + "</div>", false));
                    
                    rows.add(row);

                } else {
                    Result r = core.getService(leaf.getContainer().getId());
                    if (r.isError()) {
                        return TreeNodeVisitResult.CONTINUE;
                    }
                    Service service = (Service) r.getObject();

                    MacTableRow row = new MacTableRow();
                    row.addCell(new MacTableCell(getSession(), service.getShortName(), false));
                    row.addCell(new MacTableCell(getSession(), 
                            "<div align='right' style='font-weight:bold;'>" 
                            + service.getSalePrice(CarssierDataBase.getDataBase())
                            + "</div>", 
                            false));
                    
                    final CheckBox chkAllowShow = new CheckBox(getSession(), "");
                    allowShowCheckBoxes.add(chkAllowShow);
                    Result res = core.getPriceACL(((TreeLeafBasic) leaf).getId());
                    if(res.isError()){
                        chkAllowShow.setChecked(false);
                    } else {
                        chkAllowShow.setChecked(((PriceACL)res.getObject()).isAllowShow());
                    }
                    chkAllowShow.addUIEventListener(new UIEventListener() {

                        @Override
                        public void event(UIEvent evt) {
                            core.setPriceACLShow(((TreeLeafBasic) leaf).getId(), chkAllowShow.isChecked());
                        }
                    });
                    row.addCell(new MacTableCell(getSession(), 
                            "<div align='center'>"
                            + chkAllowShow.getModel()
                            + "</div>", false));
                    
                    
                    final CheckBox chkAllowUse = new CheckBox(getSession(), "");
                    allowUseBasketCheckBoxes.add(chkAllowUse);
                    res = core.getPriceACL(((TreeLeafBasic) leaf).getId());
                    if(res.isError()){
                        chkAllowUse.setChecked(false);
                    } else {
                        chkAllowUse.setChecked(((PriceACL)res.getObject()).isAllowUse());
                    }
                    chkAllowUse.addUIEventListener(new UIEventListener() {

                        @Override
                        public void event(UIEvent evt) {
                            core.setPriceACLUse(((TreeLeafBasic) leaf).getId(), chkAllowUse.isChecked());
                        }
                    });
                    row.addCell(new MacTableCell(getSession(), 
                            "<div align='center'>"
                            + chkAllowUse.getModel()
                            + "</div>", false));
                    
                    rows.add(row);
                }

                return TreeNodeVisitResult.CONTINUE;
            }
        };

        tnw.start();

        macTableModel.setData(rows);
    }

    @Override
    public String getModel() {
        refreshTable();

        String model = "";
        model += macTableModel.getModel();

        return model;
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
            setSession(jsonObject.getString("session"));

            if (jsonObject.getString("eventType").equals("click")) {
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "WWW прайс-лист");
                JSMediator.setWorkPanel(getSession(), getModel());
                JSMediator.hideNavigationPanel(getSession());
            }
        } catch (JSONException  ex) {
            Logger.getGlobal().log(Level.WARNING, json, ex);
        }
    }
}
