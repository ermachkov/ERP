/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ubo.equipment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import org.jssdb.core.proxy.KnowsId;
import org.ubo.properties.HasProperties;
import org.ubo.properties.Properties;
import org.ubo.properties.Property;
import org.uui.db.DataBase;

/**
 *
 * @author Pechenko Anton aka parilo, forpost78 aaaaaat gmail doooooot com
 *         (C) Copyright by Pechenko Anton, created 28.02.2011
 */
public class Equipment implements Serializable, KnowsId, HasProperties {

    static final long serialVersionUID = 1L;

    private long id;

    private ImageIcon photo;
    private String name = "";
    private boolean planing = false;
    private EquipmentProps props;

    public Equipment(DataBase db){
        initProps(db);
    }

    private void initProps(DataBase db){
        EquipmentProps ps = (EquipmentProps)db.getObject(EquipmentProps.class.getName());
        if( ps == null ){
            ps = new EquipmentProps();
            db.addObject(ps);
        }
        this.props = (EquipmentProps)ps.clone();
        
        Property p;
        
        p = new Property(){
            @Override
            public Serializable getValue() {
                return getPhoto();
            }
            @Override
            public void setValue(Serializable value) {
                setPhoto((ImageIcon) value);
            }
        };
        p.setName("image");
        p.setType(Property.TYPE_IMAGE);
        p.setGlobal(true);
        p.setReadOnly(true);
        props.addProperty(p);

        p = new Property(){
            @Override
            public Serializable getValue() {
                return getName();
            }
            @Override
            public void setValue(Serializable value) {
                setName((String)value);
            }
        };
        p.setName("Name");
        p.setType(Property.TYPE_STRING);
        p.setGlobal(true);
        p.setReadOnly(true);
        props.addProperty(p);

        p = new Property(){
            @Override
            public Serializable getValue() {
                return isPlaning();
            }
            @Override
            public void setValue(Serializable value) {
                setPlaning((Boolean)value);
            }
        };
        p.setName("Planning");
        p.setType(Property.TYPE_BOOL);
        p.setGlobal(true);
        p.setReadOnly(true);
        props.addProperty(p);
        
    }

    public ImageIcon getPhoto() {
        return photo;
    }

    public void setPhoto(ImageIcon photo) {
        this.photo = photo;
    }

    public boolean isPlaning() {
        return planing;
    }

    public void setPlaning(boolean planing) {
        this.planing = planing;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public Properties getProperties() {
        return props;
    }

}
