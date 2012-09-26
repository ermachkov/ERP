/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.explorer;

import org.uui.component.Component;
import org.uui.event.EventListenerList;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class ExplorerLeaf extends Component {

    private String className = "", text = "", image = "", identificator = "",
            extraText = "";
    private long id;
    private EventListenerList listenerList = new EventListenerList();

    public ExplorerLeaf(String sessionId, String className, long id, String text, String image) {
        super(sessionId);
        this.className = className;
        this.id = id;
        this.text = text;
        this.image = image;
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

    public void setExtraText(String text) {
        extraText = text;
    }

    public String getExtraText() {
        return extraText;
    }

    @Override
    public String getModel() {
            String extraDiv = "";
            if (extraText != null) {
                if (!extraText.equals("")) {
                    extraDiv = "<div class=\"extraText\">" + extraText + "</div>";
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("<div class=\"treeLeaf\" ").append("className=\"")
                    .append(className).append("\" dbid=\"").append(id)
                    .append("\" " + "identificator=\"").append(getIdentificator()).append("\"")
                    .append(">").append(extraDiv).append("<img src=\"").append(image)
                    .append("\"/><div class=\"explorerText\">").append(text).append("</div>")
                    .append("</div>");

        return sb.toString();
    }

    @Override
    public String getIdentificator() {
        if (identificator == null) {
            identificator = "" + hashCode();
        }

        if (identificator.equals("")) {
            identificator = "" + hashCode();
        }

        return identificator;
    }

    @Override
    public void setIdentificator(String identificator) {
        this.identificator = identificator;
    }

    @Override
    public void fireEvent(String json) {if(json == null){             return;         }                  if(json.equals("")){             return;         }                  if(json.equals("{}")){             return;         }
        fireExplorerEvent(new UIEvent(json));
    }
}
