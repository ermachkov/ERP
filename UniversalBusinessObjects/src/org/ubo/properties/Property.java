/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ubo.properties;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;
import javax.swing.ImageIcon;

/**
 *
 * @author Pechenko Anton aka parilo, forpost78 aaaaaat gmail doooooot com
 *         (C) Copyright by Pechenko Anton, created 09.02.2011
 */
public class Property implements Serializable {

    static final long serialVersionUID = 1L;

    public transient static int TYPE_STRING = 1;
    public transient static int TYPE_NUMBER = 2;
    public transient static int TYPE_IMAGE = 3;
    public transient static int TYPE_PDF = 4;
    public transient static int TYPE_HYPERLINK = 5;
    public transient static int TYPE_ADDRESS = 6;
    public transient static int TYPE_BOOL = 7;
    public transient static int TYPE_LIST = 8; // list uses smartchooser to add items
    public transient static int TYPE_CONTACTS = 9;
    public transient static int TYPE_SELECT = 10; // jcombobox of objects of one specifed class
    public transient static int TYPE_BIGDECIMAL = 11;
    public transient static int TYPE_TECHNOLOGY_MAP = 12;
    public transient static int TYPE_CONSTSELECT = 13; //jcombobox of specifed objects, uses toString() as text
    public transient static int TYPE_OTHER = 10000;
    private int type = TYPE_STRING;
    
    private Serializable value = null;
    
    private boolean global = false;
    private boolean readOnly = false;
    private boolean inTree = false;
    private boolean enabled = true;
    
    private String name = "";
    private String listType = "";
    private String treeName = "";
    private String defaultImagePath = "";
    
    //TYPE_CONSTLIST
    private ArrayList<Object> constListItems;
    
    //!! after adding new fields don't forget add it to getClone() function

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Serializable getValue() {
        return value;
    }

    public void setValue(Serializable value) {
        this.value = value;
    }

    public ImageIcon getImageIcon(){
        return (ImageIcon) getValue();
    }

    public String getString(){
        return (String) getValue();
    }

    public long getLong(){
        return Long.parseLong(Objects.toString(getValue(), "0"));
        //return (Long)this.value;
    }

    public int getInt(){
        return (Integer)getValue();
    }
    
    public BigDecimal getBigDecimal(){
        return (BigDecimal) getValue();
    }
    
    public boolean getBoolean(){
        return (boolean) getValue();
    }

    public ArrayList<Object> getConstListItems() {
        return constListItems;
    }

    public void setConstListItems(ArrayList<Object> constListItems) {
        this.constListItems = constListItems;
    }
    
    public Property getClone(){
        Property p = new Property();
        p.setName(this.name);
        p.setType(this.type);
        p.setGlobal(this.global);
        p.setReadOnly(this.readOnly);
        p.setListType(this.listType);
        p.setInTree(this.inTree);
        p.setTreeName(this.treeName);
        p.setDefaultImagePath(this.defaultImagePath);
        p.setConstListItems(constListItems);
        
        return p;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public String getListType() {
        return listType;
    }

    public void setListType(String listType) {
        this.listType = listType;
    }

    public boolean isInTree() {
        return inTree;
    }

    public void setInTree(boolean inTree) {
        this.inTree = inTree;
    }

    public String getTreeName() {
        return treeName;
    }

    public void setTreeName(String treeName) {
        this.treeName = treeName;
    }

    public String getDefaultImagePath() {
        return defaultImagePath;
    }

    public void setDefaultImagePath(String defaultImagePath) {
        this.defaultImagePath = defaultImagePath;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * function for TYPE_SELECT it returns object which will appear in Select GUI.
     * Override it to define your variants
     * @return ArrayList<Serializable>
     */
    public ArrayList<Serializable> getSelectItems() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Property other = (Property) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (this.global != other.global) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 41 * hash + this.type;
        hash = 41 * hash + (this.global ? 1 : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "Property{" + "name=" + name + ", type=" + type + ", value=" + value + ", global=" + global + ", listType="+listType+"}";
    }


}
