/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.properties;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.uui.db.DataBase;

/**
 *
 * @author Pechenko Anton aka parilo, forpost78 aaaaaat gmail doooooot com
 *         (C) Copyright by Pechenko Anton, created 11.02.2011
 */
public class Properties implements Serializable, Cloneable {

    static final long serialVersionUID = 1L;
    private ArrayList<Property> props;
    private HashMap<String, Property> byName;
    private HashMap<Property, String> byProperty;
    private HashMap<String, Serializable> oldValues;

    public Properties() {
        props = new ArrayList<>();
        byName = new HashMap<>();
        byProperty = new HashMap<>();
        oldValues = new HashMap<>();
    }

    public void initFromTemplate(DataBase db) {
        String templClassName = getClass().getName();
        Properties ps = (Properties) db.getObject(templClassName);
        if (ps == null) {
            try {
                ps = (Properties) getClass().getConstructor().newInstance();
                db.addObject(ps);
            } catch ( InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "initFromTemplate: cannot construct properties from template properties object");
            }
        }
        addProperties(ps);
    }
    
    public void changePropertyName(Property p, String newName) {
        Property myp = byName.get(p.getName());

        //resoring value of previously deleted field
        if (myp.getValue() == null) {
            myp.setValue(oldValues.get("f" + newName + p.getType()));
        }

        byName.remove(myp.getName());
        byProperty.remove(myp);

        myp.setName(newName);

        byName.put(myp.getName(), myp);
        byProperty.put(myp, myp.getName());
    }

    public void changePropertyType(Property p, int newType) {
        Property myp = byName.get(p.getName());

        oldValues.put("f" + myp.getName() + myp.getType(), myp.getValue());

        byProperty.remove(myp);
        myp.setType(newType);
        byProperty.put(myp, myp.getName());

        myp.setValue(oldValues.get("f" + myp.getName() + myp.getType()));
    }

    public void changePropertyGlobality(Property p, boolean newGlobality) {
        Property myp = byName.get(p.getName());
        byProperty.remove(myp);
        myp.setGlobal(newGlobality);
        byProperty.put(myp, myp.getName());
    }

    public void addProperty(Property prop) {

        Property p = prop;
        if (!props.contains(p)) {
            props.add(p);
            byName.put(p.getName(), p);
            byProperty.put(p, p.getName());

            //resoring value pf previously deleted field
            if (p.getValue() == null) {
                p.setValue(oldValues.get("f" + p.getName() + p.getType()));
            }
        }

    }

    public void addProperties(Properties p) {
        List<Property> ps = p.getProperties();
        for(int i=0; i<ps.size(); i++) addProperty(ps.get(i));
    }

    public void addProperties(HasProperties hp) {
        addProperties(hp.getProperties());
    }

    public void removeProperty(Property p) {
        //storing value
        //if somebody add same field in future
        //value will be restored
        Property myp = getProperty(p.getName());
        if (myp != null) {
            if (myp.equals(p)) {
                oldValues.put("f" + myp.getName() + myp.getType(), myp.getValue());
            }

            props.remove(p);
            byName.remove(p.getName());
            byProperty.remove(p);
        }
    }

    public List<Property> getProperties() {
        return props;
    }

    public Property getProperty(String name) {
        return byName.get(name);
    }

    public void setPropertyValue(Property p, Serializable value) {
        Property prop = byName.get(p.getName());
        prop.setValue(value);
    }

    @Override
    public Object clone() {
        try {
            Properties ps = this.getClass().newInstance();
            for (int i = 0; i < this.props.size(); i++) {
                Property newp = this.props.get(i).getClone();
                ps.props.add(newp);
                ps.byName.put(newp.getName(), newp);
                ps.byProperty.put(newp, newp.getName());
            }
            return ps;
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Properties.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
