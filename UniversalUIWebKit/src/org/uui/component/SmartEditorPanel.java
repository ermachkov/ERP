/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import org.uui.event.EventListenerList;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.table.MacTableModel;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class SmartEditorPanel extends Component {

    private String identificator = "", model = "";
    private MacTableModel tblEditor;
    private Button btnBack;
    private Object editableObject;
    private EventListenerList listenerList = new EventListenerList();

    public SmartEditorPanel(String sessionId, MacTableModel tblEditor) {
        super(sessionId);
        btnBack = new Button(sessionId, "Назад");
        btnBack.setCssClass("btnSmartEditorBack");
        this.tblEditor = tblEditor;
    }

    public Object getEditableObject() {
        return editableObject;
    }

    public void setEditableObject(Object editableObject) {
        this.editableObject = editableObject;
    }

    public Button getButtonBack() {
        return btnBack;
    }

    public void setTable(MacTableModel tblEditor) {
        this.tblEditor = tblEditor;
        model = "";
    }

    public void addUIEventListener(UIEventListener listener) {
        listenerList.add(UIEventListener.class, listener);
    }

    private void fireExplorerEvent(UIEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == UIEventListener.class) {
                ((UIEventListener) listeners[i + 1]).event(evt);
            }
        }
    }

    @Override
    public void setModel(String html) {
        model = html;
    }

    @Override
    public String getModel() {
        boolean isDefined = true;
        if (model == null) {
            isDefined = false;

        } else if (model.equals("")) {
            isDefined = false;
        }

        if (!isDefined) {
            model = "<table width='100%' height='100%'>"
                    + "<tr>"
                    + "<td valign='top' height='95%'>"
                    + tblEditor.getModel()
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td valign='middle' align='right' height='5%'>"
                    + btnBack.getModel()
                    + "</td>"
                    + "</tr>"
                    + "</table>";
        }

        return model;
    }

    @Override
    public String getIdentificator() {
        if (identificator == null) {
            identificator = "" + hashCode();
        } else if (identificator.equals("")) {
            identificator = "" + hashCode();
        }

        return identificator;
    }

    @Override
    public void setIdentificator(String identificator) {
        this.identificator = identificator;
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
        fireExplorerEvent(new UIEvent(json));
    }
}
