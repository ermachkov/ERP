/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.rules;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Objects;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class RuleItem implements Serializable {

    public final static int BOOLEAN = 0, RADIO = 1, MULTI_SELECT = 2;
    public static final long serialVersionUID = 1L;
    private int type;
    private String key, description;
    private Object value;

    public RuleItem() {
        //
    }

    public static RuleItem newRuleItemBoolean(String key, String description, boolean defaultValue) {
        Objects.requireNonNull(key, "Key can't be null");
        Objects.requireNonNull(description, "Description can't be null");

        RuleItem ruleItem = new RuleItem();
        ruleItem.setType(BOOLEAN);
        ruleItem.setKey(key);
        ruleItem.setValue(defaultValue);
        ruleItem.setDescription(description);
        return ruleItem;
    }

    public static RuleItem newRuleItemRadio(String key, String description,
            LinkedList<SelectorRuleItem> list, int defaultSelectedIndex) {
        Objects.requireNonNull(key, "Key can't be null");
        Objects.requireNonNull(description, "Description can't be null");
        Objects.requireNonNull(list, "SelectRuleItem list can't be null");

        RuleItem ruleItem = new RuleItem();
        ruleItem.setType(RADIO);
        ruleItem.setKey(key);

        if (defaultSelectedIndex >= 0 || defaultSelectedIndex < list.size()) {
            for (SelectorRuleItem item : list) {
                item.setSelected(false);
            }
            list.get(defaultSelectedIndex).setSelected(true);
        }
        ruleItem.setValue(list);
        ruleItem.setDescription(description);
        return ruleItem;
    }

    public static RuleItem newRuleItemMultiSelect(String key, String description,
            LinkedList<SelectorRuleItem> list, int ... defaultSelectedIndexes) {
        Objects.requireNonNull(key, "Key can't be null");
        Objects.requireNonNull(description, "Description can't be null");
        Objects.requireNonNull(list, "SelectRuleItem list can't be null");

        RuleItem ruleItem = new RuleItem();
        ruleItem.setType(MULTI_SELECT);
        ruleItem.setKey(key);
        
        if(defaultSelectedIndexes != null){
            for(int index : defaultSelectedIndexes){
                if(index >= 0 && index < list.size()){
                    list.get(index).setSelected(true);
                }
            }
        }
        
        ruleItem.setValue(list);
        ruleItem.setDescription(description);
        return ruleItem;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "RuleItem{" + "type=" + type + ", key=" + key + ", description=" + description + ", value=" + value + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RuleItem other = (RuleItem) obj;
        if (this.type != other.type) {
            return false;
        }
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + this.type;
        hash = 17 * hash + Objects.hashCode(this.key);
        hash = 17 * hash + Objects.hashCode(this.description);
        return hash;
    }
    
    
}
