/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import java.util.LinkedHashMap;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public abstract class TableSearchFilter extends Component {
    
    private TextField txtSearch;
    private Button btnSearch;
    private ComboBox cboRowCount;

    public TableSearchFilter(String sessionId) {
        super(sessionId);
        txtSearch = new TextField(getSession());
        txtSearch.setStyle("width:250px;");
        txtSearch.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                searchAction(txtSearch.getText(), getRowsCount());
            }
        });
        
        btnSearch = new Button(getSession(), "Показать");
        btnSearch.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                searchAction(txtSearch.getText(), getRowsCount());
            }
        });
        
        cboRowCount = new ComboBox(getSession());
        LinkedHashMap<String, String> m = new LinkedHashMap();
        m.put("30", "30");
        m.put("45", "45");
        m.put("60", "60");
        cboRowCount.setItems(m);
    }
    
    public int getRowsCount(){
        return Integer.parseInt(cboRowCount.getSelectedValue().trim());
    }
    
    public String getSearchPattern(){
        return txtSearch.getText();
    }
    
    public abstract void searchAction(String searchPattern, int rowsCount);

    @Override
    public String getModel() {
        String model = ""
                + "<div width='100%' class='tableSearchFilter' align='right'>"
                + "<img src='img/smartchooser/lens_16.png' />"
                + txtSearch.getModel()
                //+ btnSearch.getModel()
                + cboRowCount.getModel()
                + "</div>";
        
        return model;
    }
}
