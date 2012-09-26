/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.sibek.business.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pechenko Anton aka parilo<forpost78 at gmail dot com>
 */
public class JarLoader {

    private static final Class[] parameters = new Class[]{URL.class};

    public void addJarFile(String jarName){        
        try {
            File file = new File(jarName);
            URL u = file.toURI().toURL();
            URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Class sysclass = URLClassLoader.class;
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[]{u});
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | MalformedURLException ex) {
            Logger.getLogger(JarLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public HashMap<String,ArrayList<String>> scanDir(String dir, String classNameEndsWith) {

        //HashMap key is jar full path
        //ArrayList content is full names of classes
        HashMap<String,ArrayList<String>> found = new HashMap<>();

        File d = new File(dir);
        if (d.exists()) {
            String[] fls = d.list();
            for (String fname : fls) {
                if (fname.endsWith(".jar")) {
                    String fpath = dir + fname;
                    File f = new File(fpath);
                    if (f.isFile()) {

                        try {

                            JarFile jf = new JarFile(fpath);
                            Enumeration<JarEntry> jes = jf.entries();

                            ArrayList<String> classes = new ArrayList<>();

                            while (jes.hasMoreElements()) {
                                String je = jes.nextElement().getName();

                                if (je.endsWith(classNameEndsWith) && (je.indexOf("$") == -1)) {

                                    String classname = je.replaceAll("/", ".").substring(0, je.lastIndexOf(".class"));
                                    classes.add(classname);

                                }

                            }

                            if (classes.size() > 0) found.put(fpath, classes);

                        } catch (IOException ex) {
                            Logger.getLogger(JarLoader.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                }
            }
        }

        return found;

    }

    public Object createClass(String className) throws PluginInitException{
        Object object = null;
        try {
            Class cls = Class.forName(className);
            object = cls.getConstructor().newInstance();
            
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException ex) {
            Logger.getGlobal().log(Level.WARNING, "WARNING! Can't init class " + className, ex);
        }
        
        return object;
    }

}
