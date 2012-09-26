/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import org.uui.event.EventListenerList;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class SmartChooser extends Component {

    private EventListenerList listenerList = new EventListenerList();
    private static int MODE_SEARCH = 0, MODE_EDIT = 1;
    private int mode = 0;
    private List list;
    private SmartSearchPanel smartSearchPanel;
    private String model = "", identificator = "", filter = "";

    public SmartChooser(String sessionId, List list) {
        super(sessionId);
        this.list = list;
        smartSearchPanel = new SmartSearchPanel(getSession(), "Найти", "Добавить");
        smartSearchPanel.setIdentificator(SmartChooser.class.getName() + SearchPanel.class.getName());
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

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public SmartSearchPanel getSmartSearchPanel() {
        return smartSearchPanel;
    }

    public void setList(List list) {
        this.list = list;
        model = "";
    }

    public List getList() {
        return list;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    @Override
    public void setModel(String html) {
        model = html;
    }

    @Override
    public String getModel() {
        if (model == null) {
            model = "";
        }

        if (mode == SmartChooser.MODE_SEARCH) {
            if (model.equals("")) {
                model = "<div class='smartChooser' identificator='" + getIdentificator() + "'>"
                        + "<table width='100%' heigth='100%'>"
                        + "<tr heigth='48'>"
                        + "<td valign='middle'>"
                        + smartSearchPanel.getModel()
                        + "</td>"
                        + "</tr>"
                        + "<tr>"
                        + "<td><div style='width:100%; heigth:100%; overflow:auto;' id='smartListChooser'>"
                        + list.getModel()
                        + "</div></td>"
                        + "</tr>"
                        + "</table>"
                        + "</div>";
            }

        } else if (mode == SmartChooser.MODE_EDIT) {
            if (model.equals("")) {
                model = "";
            }
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
        //
    }

    @Override
    public void fireEvent(String json) {if(json == null){             return;         }                  if(json.equals("")){             return;         }                  if(json.equals("{}")){             return;         }
        fireExplorerEvent(new UIEvent(json));
    }
}
