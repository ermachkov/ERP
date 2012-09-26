/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.rules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class Rule implements Serializable {

    public static final long serialVersionUID = 1L;
    private String moduleName, moduleClassName, moduleDescription, moduleWorkPanelClassName;
    private boolean isAllowToUse = false;
    private ArrayList<RuleItem> ruleItems = new ArrayList<>();

    public Rule() {
        //
    }

    public ArrayList<RuleItem> getRuleItems() {
        return ruleItems;
    }

    public void setRuleItems(ArrayList<RuleItem> ruleItems) {
        this.ruleItems = ruleItems;
    }

    public void setModuleWorkPanelClassName(String moduleWorkPanelClassName) {
        this.moduleWorkPanelClassName = moduleWorkPanelClassName;
    }

    public String getModuleWorkPanelClassName() {
        return "" + moduleWorkPanelClassName;
    }

    public String getModuleDescription() {
        return moduleDescription;
    }

    public void setModuleDescription(String moduleDescription) {
        this.moduleDescription = moduleDescription;
    }

    public String getModuleClassName() {
        return moduleClassName;
    }

    public void setModuleClassName(String moduleClassName) {
        this.moduleClassName = moduleClassName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public boolean isAllowToUse() {
        return isAllowToUse;
    }

    public void setAllowToUse(boolean isAllowToUse) {
        this.isAllowToUse = isAllowToUse;
    }

    @Override
    public String toString() {
        return "Rule{" + "moduleName=" + moduleName + ", moduleClassName=" + moduleClassName + ", moduleDescription=" + moduleDescription + ", moduleWorkPanelClassName=" + moduleWorkPanelClassName + ", isAllowToUse=" + isAllowToUse + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Rule other = (Rule) obj;
        if (!Objects.equals(this.moduleName, other.moduleName)) {
            return false;
        }
        if (!Objects.equals(this.moduleClassName, other.moduleClassName)) {
            return false;
        }
        if (!Objects.equals(this.moduleDescription, other.moduleDescription)) {
            return false;
        }
        if (!Objects.equals(this.moduleWorkPanelClassName, other.moduleWorkPanelClassName)) {
            return false;
        }
        if (!Objects.equals(this.ruleItems, other.ruleItems)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.moduleName);
        hash = 89 * hash + Objects.hashCode(this.moduleClassName);
        hash = 89 * hash + Objects.hashCode(this.moduleDescription);
        hash = 89 * hash + Objects.hashCode(this.moduleWorkPanelClassName);
        hash = 89 * hash + Objects.hashCode(this.ruleItems);
        return hash;
    }
}
