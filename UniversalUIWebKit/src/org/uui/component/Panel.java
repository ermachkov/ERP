/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import java.util.LinkedList;
import java.util.Objects;
import org.uui.webkit.WebKitComponent;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class Panel {

    private LinkedList<WebKitComponent> components = new LinkedList<>();
    private LinkedList<String> styles = new LinkedList<>();
    private String style = "";

    public Panel() {
        //
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public boolean addComponent(WebKitComponent wkc, String containerStyle) {
        boolean success = false;
        if (success = components.add(wkc)) {
            if (containerStyle == null) {
                containerStyle = "";
            }
            styles.add(containerStyle);
        }
        return success;
    }

    public synchronized boolean updateComponent(int index, WebKitComponent wkc) {
        boolean success = false;
        int i = 0;
        WebKitComponent[] wkArray = components.toArray(new WebKitComponent[components.size()]);
        components.clear();

        for (WebKitComponent wk : wkArray) {
            if (i == index) {
                success = components.add(wkc);

            } else {
                components.add(wk);
            }

            i++;
        }

        return success;
    }

    public boolean removeComponent(int index) {
        boolean success = false;
        int i = 0;
        WebKitComponent[] wkArray = components.toArray(new WebKitComponent[components.size()]);
        components.clear();

        for (WebKitComponent wk : wkArray) {
            if (i != index) {
                components.add(wk);
            } else {
                success = true;
            }

            i++;
        }

        styles.remove(index);

        return success;
    }

    public WebKitComponent getWebKitComponent(int index) {
        WebKitComponent wkFind = null;

        int i = 0;
        for (WebKitComponent wk : components) {
            if (i == index) {
                wkFind = wk;
                break;
            }

            i++;
        }

        return wkFind;
    }

    public int getWebKitComponentsLength() {
        return components.size();
    }

    public String getModel() {
        String model = "";
        String s = Objects.toString(getStyle(), "null");
        String _style = "";
        if (!s.equals("null") && !s.equals("")) {
            _style = "style='" + getStyle() + "' ";
        }

        model += "<div " + _style + ">";
        int index = 0;
        for (WebKitComponent wk : components) {
            String _st = "";
            String st = styles.get(index);
            if (!st.equals("")) {
                _st = "style='" + st + "' ";
            }
            model += "<div " + _st + ">";
            model += wk.getModel();
            model += "</div>";
            index++;
        }

        model += "</div>";
        return model;
    }
}
