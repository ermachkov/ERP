/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class TabbedPane {

    /*
    <div id="tabs">
    <ul>
    <li><a href="#tabs-1">Nunc tincidunt</a></li>
    <li><a href="#tabs-2">Proin dolor</a></li>
    <li><a href="#tabs-3">Aenean lacinia</a></li>
    </ul>
    <div id="tabs-1">
    <p>Proin elit arcu, rutrum commodo, vehicula tempus, commodo a, risus. Curabitur nec arcu. Donec sollicitudin mi sit amet mauris. Nam elementum quam ullamcorper ante. Etiam aliquet massa et lorem. Mauris dapibus lacus auctor risus. Aenean tempor ullamcorper leo. Vivamus sed magna quis ligula eleifend adipiscing. Duis orci. Aliquam sodales tortor vitae ipsum. Aliquam nulla. Duis aliquam molestie erat. Ut et mauris vel pede varius sollicitudin. Sed ut dolor nec orci tincidunt interdum. Phasellus ipsum. Nunc tristique tempus lectus.</p>
    </div>
    <div id="tabs-2">
    <p>Morbi tincidunt, dui sit amet facilisis feugiat, odio metus gravida ante, ut pharetra massa metus id nunc. Duis scelerisque molestie turpis. Sed fringilla, massa eget luctus malesuada, metus eros molestie lectus, ut tempus eros massa ut dolor. Aenean aliquet fringilla sem. Suspendisse sed ligula in ligula suscipit aliquam. Praesent in eros vestibulum mi adipiscing adipiscing. Morbi facilisis. Curabitur ornare consequat nunc. Aenean vel metus. Ut posuere viverra nulla. Aliquam erat volutpat. Pellentesque convallis. Maecenas feugiat, tellus pellentesque pretium posuere, felis lorem euismod felis, eu ornare leo nisi vel felis. Mauris consectetur tortor et purus.</p>
    </div>
    <div id="tabs-3">
    <p>Mauris eleifend est et turpis. Duis id erat. Suspendisse potenti. Aliquam vulputate, pede vel vehicula accumsan, mi neque rutrum erat, eu congue orci lorem eget lorem. Vestibulum non ante. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Fusce sodales. Quisque eu urna vel enim commodo pellentesque. Praesent eu risus hendrerit ligula tempus pretium. Curabitur lorem enim, pretium nec, feugiat nec, luctus a, lacus.</p>
    <p>Duis cursus. Maecenas ligula eros, blandit nec, pharetra at, semper at, magna. Nullam ac lacus. Nulla facilisi. Praesent viverra justo vitae neque. Praesent blandit adipiscing velit. Suspendisse potenti. Donec mattis, pede vel pharetra blandit, magna ligula faucibus eros, id euismod lacus dolor eget odio. Nam scelerisque. Donec non libero sed nulla mattis commodo. Ut sagittis. Donec nisi lectus, feugiat porttitor, tempor ac, tempor vitae, pede. Aenean vehicula velit eu tellus interdum rutrum. Maecenas commodo. Pellentesque nec elit. Fusce in lacus. Vivamus a libero vitae lectus hendrerit hendrerit.</p>
    </div>
    </div>
     */
    private Set<String> tabsId = new LinkedHashSet();
    private Set<String> tabsLabel = new LinkedHashSet();
    private Set<String> tabsPanels = new LinkedHashSet();
    private String id = "", cssClass = "";

    public TabbedPane() {
        //
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void addTab(String idTab, String tabLabel, String tabPanel) {
        tabsId.add(idTab);
        tabsLabel.add(tabLabel);
        tabsPanels.add(tabPanel);
    }

    public void removeTab(int index) {
        int i = 0;
        String deleteValue = "";
        for (String s : tabsId) {
            if (i == index) {
                deleteValue = s;
            }
            i++;
        }
        tabsId.remove(deleteValue);

        i = 0;
        deleteValue = "";
        for (String s : tabsLabel) {
            if (i == index) {
                deleteValue = s;
            }
            i++;
        }
        tabsLabel.remove(deleteValue);

        i = 0;
        deleteValue = "";
        for (String s : tabsLabel) {
            if (i == index) {
                deleteValue = s;
            }
            i++;
        }
        tabsPanels.remove(deleteValue);
    }

    public void removeTab(String id) {
        int i = 0;
        int index = 0;
        String deleteValue = "";
        for (String s : tabsId) {
            if (s.equals(id)) {
                deleteValue = s;
                index = i;
            }
            i++;
        }
        tabsId.remove(deleteValue);

        i = 0;
        deleteValue = "";
        for (String s : tabsLabel) {
            if (i == index) {
                deleteValue = s;
            }
            i++;
        }
        tabsLabel.remove(deleteValue);

        i = 0;
        deleteValue = "";
        for (String s : tabsLabel) {
            if (i == index) {
                deleteValue = s;
            }
            i++;
        }
        tabsPanels.remove(deleteValue);
    }

    public String getModel() {
        String ss = Objects.toString(getCssClass(), "null");
        String css = "class='tabbedPane' ";
        if (!ss.equals("null") && !ss.equals("")) {
            css = "class='" + getCssClass() + "' ";
        }

        ss = Objects.toString(getCssClass(), "null");
        String _id = "";
        if (!ss.equals("null") && !ss.equals("")) {
            _id = "id='" + getId() + "' ";
        }

        //<li><a href="#tabs-1">Nunc tincidunt</a></li>
        String _tabLabels = "";
        for (int i = 0; i < tabsId.size(); i++) {

            int count = 0;
            for (String s : tabsId) {
                if (count == i) {
                    _tabLabels += "<li><a href='#" + s + "'>";
                    break;
                }
                count++;
            }

            count = 0;
            for (String s : tabsLabel) {
                if (count == i) {
                    _tabLabels += "<span style='font-size:80%'>" + s + "</span></a></li>";
                    break;
                }
                count++;
            }
        }

        //<div id="tabs-1">
        String _tabsPanels = "";
        for (int i = 0; i < tabsId.size(); i++) {

            int count = 0;
            for (String s : tabsId) {
                if (count == i) {
                    _tabsPanels += "<div id='" + s + "'>";
                    break;
                }
                count++;
            }

            count = 0;
            for (String s : tabsPanels) {
                if (count == i) {
                    _tabsPanels += s + "</div>";
                    break;
                }
                count++;
            }
        }

        String model = "<div " + _id + " " + css + ">"
                + "<ul>"
                + _tabLabels
                + "</ul>"
                + _tabsPanels
                + "</div>";

        return model;
    }

    public synchronized boolean updateTabPanel(int tabIndex, String panel) {
        boolean success = false;
        int index = 0;
        String sArr[] = tabsPanels.toArray(new String[tabsPanels.size()]);
        tabsPanels.clear();
        for (String s : sArr) {
            if (index == tabIndex) {
                success = tabsPanels.add(panel);
            } else {
                tabsPanels.add(s);
            }
            index++;
        }
        
        return  success;
    }

    public synchronized boolean updateTabPanel(String idTab, String panel) {
        int index = 0;
        int i = 0;
        for (String s : tabsId) {
            if (s.equals(idTab)) {
                index = i;
                break;
            }
            i++;
        }

        return updateTabPanel(index, panel);
    }
}
