/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ubo.properties;

import java.util.List;
import org.jssdb.core.proxy.KnowsId;
import org.uui.db.DataBase;

/**
 *
 * @author Pechenko Anton aka parilo, forpost78 aaaaaat gmail doooooot com
 *         (C) Copyright by Pechenko Anton, created 21.02.2011
 */
public class PropertiesTool {

    private DataBase db;
    private String objectsClassName;
    private String templClassName;

    public PropertiesTool(DataBase db, String objectsClassName, String templClassName){
        this.db = db;
        this.objectsClassName = objectsClassName;
        this.templClassName = templClassName;
    }

    public void addPropertyGlobally(Property prop){
        List<Object> objs = db.getObjects(objectsClassName);
        for(int i=0; i<objs.size(); i++){
            HasProperties hp = (HasProperties) objs.get(i);
            Property p = prop.getClone();
            hp.getProperties().addProperty(p);
            db.updateObject((KnowsId)hp);
        }

        Properties ps = (Properties)db.getObject(templClassName);
        Property p = prop.getClone();
        ps.addProperty(p);
        db.updateObject(ps);
    }

    public void removePropertyGlobally(Property prop){
        List<Object> objs = db.getObjects(objectsClassName);
        for(int i=0; i<objs.size(); i++){
            HasProperties hp = (HasProperties) objs.get(i);
            Property p = prop.getClone();
            hp.getProperties().removeProperty(p);
            db.updateObject((KnowsId)hp);
        }

        Properties ps = (Properties)db.getObject(templClassName);
        Property p = prop.getClone();
        ps.removeProperty(p);
        db.updateObject(ps);
    }

    public void changePropertyNameGlobally(Property prop, String newName){
        List<Object> objs = db.getObjects(objectsClassName);
        for(int i=0; i<objs.size(); i++){
            HasProperties hp = (HasProperties) objs.get(i);
            Property p = prop.getClone();
            hp.getProperties().changePropertyName(p, newName);
            db.updateObject((KnowsId)hp);
        }

        Properties ps = (Properties)db.getObject(templClassName);
        Property p = prop.getClone();
        ps.changePropertyName(p, newName);
        db.updateObject(ps);
    }

    public void changePropertyGlobGlobally(Property prop, boolean newGlob){
        List<Object> objs = db.getObjects(objectsClassName);
        for(int i=0; i<objs.size(); i++){
            HasProperties hp = (HasProperties) objs.get(i);
            Property p = prop.getClone();
            hp.getProperties().changePropertyGlobality(p, newGlob);
            db.updateObject((KnowsId)hp);
        }

        Properties ps = (Properties)db.getObject(templClassName);
        Property p = prop.getClone();
        ps.changePropertyGlobality(p, newGlob);
        db.updateObject(ps);
    }

    public void changePropertyTypeGlobally(Property prop, int newType){
        List<Object> objs = db.getObjects(objectsClassName);
        for(int i=0; i<objs.size(); i++){
            HasProperties hp = (HasProperties) objs.get(i);
            Property p = prop.getClone();
            hp.getProperties().changePropertyType(p, newType);
            db.updateObject((KnowsId)hp);
        }

        Properties ps = (Properties)db.getObject(templClassName);
        Property p = prop.getClone();
        ps.changePropertyType(p, newType);
        db.updateObject(ps);
    }

}
