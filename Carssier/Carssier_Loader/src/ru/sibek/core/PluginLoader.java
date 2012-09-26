/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.core;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.uui.event.EventListenerList;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.plugin.Plugin;
import org.uui.plugin.RightPanelPlugin;
import org.uui.plugin.WorkPanelPlugin;

public class PluginLoader {

    private String homeDir;
    private String pluginsDir;
    private WeakHashMap<String, WorkPanelPlugin> wpps;
    private WeakHashMap<String, ArrayList<RightPanelPlugin>> rpps;
    private ArrayList<Plugin> foundPlugins;
    private ArrayList<Plugin> loadedPlugins;
    private EventListenerList listenerList = new EventListenerList();

    public PluginLoader() {
        foundPlugins = new ArrayList<>();
        homeDir = System.getProperty("user.home") + File.separator + ".saas"
                + File.separator + "app" + File.separator;
        pluginsDir = homeDir + "plugin" + File.separator;
        wpps = new WeakHashMap<>();
        rpps = new WeakHashMap<>();
        loadedPlugins = new ArrayList<>();
    }

    public void addUIEventListener(UIEventListener listener) {
        listenerList.add(UIEventListener.class, listener);
    }

    private void fireEvent(UIEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == UIEventListener.class) {
                ((UIEventListener) listeners[i + 1]).event(evt);
            }
        }
    }

    public ArrayList<Plugin> getFoundPlugins() {
        return foundPlugins;
    }

    public ArrayList<Plugin> getLoadedPlugins() {
        return loadedPlugins;
    }

    public void findPlugins() {
        JarLoader jarLoader = new JarLoader();
        HashMap<String, ArrayList<String>> found = jarLoader.scanDir(pluginsDir, "Plugin.class");

        for (String jarpath : found.keySet()) {
            String s = "Try loading jar plugin by path " + jarpath;
            Logger.getGlobal().log(Level.INFO, s);
            fireEvent(new UIEvent("{eventType:\"push\", eventName:\"findPlugins\", data:\"" + s + "\"}"));
            ArrayList<String> classnames = found.get(jarpath);
            jarLoader.addJarFile(jarpath);

            for (int i = 0; i < classnames.size(); i++) {
                String cn = classnames.get(i);
                Plugin p;
                try {
                    p = (Plugin) jarLoader.createClass(cn);
                    foundPlugins.add(p);
                } catch (PluginInitException ex) {
                    Logger.getGlobal().log(Level.WARNING, jarpath + ", " + cn, ex);
                }
            }
        }
    }

    public WorkPanelPlugin getWorkPanelPlugin(String workPanelClassName) {
        return wpps.get(workPanelClassName);
    }

    public void loadPlugins(String sessionId, List<Plugin> ps) {
        Iterator<Plugin> i = ps.iterator();
        while (i.hasNext()) {

            Plugin p = i.next();

            if (!loadedPlugins.contains(p)) {

                loadedPlugins.add(p);

                if (p instanceof WorkPanelPlugin) {
                    WorkPanelPlugin wpp = (WorkPanelPlugin) p;
                    wpps.put(wpp.getWorkPanelClassName(), wpp);
                    fireEvent(new UIEvent("{eventType:\"push\", "
                            + "eventName:\"loadPlugins\", "
                            + "data:\"" + wpp.getWorkPanelClassName() + "\"}"));
                    Logger.getGlobal().log(Level.INFO, "PluginLoader: {0}", wpp.getWorkPanelClassName());
                }
            }
        }
    }

    private void unloadPlugins(List<Plugin> ps) {
        Iterator<Plugin> i = ps.iterator();
        while (i.hasNext()) {
            Plugin p = i.next();
            if (loadedPlugins.contains(p)) {
                loadedPlugins.remove(p);
            }
        }
    }

    public boolean isWorkPanelAvailable(String workPanelClassName) {
        return wpps.containsKey(workPanelClassName);
    }

    public String getWorkPanelName(String workPanelClassName) {
        if (wpps.containsKey(workPanelClassName)) {
            return wpps.get(workPanelClassName).getWorkPanelName();
        }
        return null;
    }

    public boolean getWorkPanelIsSingle(String workPanelClassName) {
        return wpps.get(workPanelClassName).isSingle();
    }
}
