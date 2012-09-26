/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.rules;

import java.io.Serializable;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class SelectorRuleItem implements Serializable {

    private String key, description;
    private boolean isSelected;
    public static final long serialVersionUID = 1L;

    public SelectorRuleItem() {
        //
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "SelectorRuleItem{" + "key=" + key + ", description=" + description + ", isSelected=" + isSelected + '}';
    }
}
