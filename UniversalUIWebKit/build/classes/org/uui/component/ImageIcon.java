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
public class ImageIcon extends Component {

    private String imagePath = "", cssClass = "imageIcon", style = "";
    private int width = -1, height = -1;
    private EventListenerList listenerList = new EventListenerList();

    public ImageIcon(String sessionId) {
        super(sessionId);
    }

    public ImageIcon(String sessionId, String imagePath) {
        super(sessionId);
        this.imagePath = imagePath;
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getModel() {
        String sWidth = "";
        if (width > 0) {
            sWidth = "width='" + sWidth + "'";
        }

        String sHeight = "";
        if (height > 0) {
            sHeight = "height='" + height + "'";
        }
        String _cssClass = "";
        if (cssClass != null) {
            if (!cssClass.equals("")) {
                _cssClass = "class='" + cssClass + "'";
            }
        }

        String _style = "";
        if (style != null) {
            if (!style.equals("")) {
                _style = "style='" + style + "'";
            }
        }

        String model = "<img src='" + imagePath + "' "
                + "identificator='" + getIdentificator() + "' "
                + "" + _cssClass + " " + _style + " " + sWidth + " " + sHeight + "/>";
        return model;
    }

    public String getIdentificator() {
        return "" + hashCode();
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public void fireEvent(String json) {if(json == null){             return;         }                  if(json.equals("")){             return;         }                  if(json.equals("{}")){             return;         }
        fireExplorerEvent(new UIEvent(json));
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

    public String toString() {
        return "ImageIcon{" + "imagePath=" + imagePath + ", cssClass=" + cssClass + ", style=" + style + ", width=" + width + ", height=" + height + '}';
    }
}
