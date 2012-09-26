/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import java.util.LinkedList;
import org.ubo.rules.RuleItem;
import org.ubo.rules.SelectorRuleItem;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public abstract class RuleMultiSelect extends Component{
    
    private RuleItem ruleItem;
    private String model;
    
    public RuleMultiSelect(String sessionId, RuleItem ruleItem) throws RuleItemTypeException{
        super(sessionId);
        if(ruleItem.getType() != RuleItem.MULTI_SELECT){
            throw new RuleItemTypeException("RuleItem " + ruleItem + " is not RuleItem.MULTI_SELECT");
        }
        this.ruleItem = ruleItem;
        
        init();
    }
    
    private void init(){
        model = "<dt>" + ruleItem.getDescription() + "<br/>";
        model += "<div class='ruleMultiSelectBlock'>";
        for(final SelectorRuleItem item : (LinkedList<SelectorRuleItem>) ruleItem.getValue()){
            final CheckBox checkBox = new CheckBox(getSession(), item.getDescription());
            checkBox.setChecked(item.isSelected());
            checkBox.addUIEventListener(new UIEventListener() {

                @Override
                public void event(UIEvent evt) {
                    item.setSelected(checkBox.isChecked());
                    change(ruleItem, checkBox.isChecked());
                }
            });
            
            model += checkBox.getModel() + "<br/>";
        }
        model += "</div>";
    }
    
    public abstract void change(RuleItem ruleItem, boolean isChecked);

    @Override
    public String getModel() {
        return model;
    }    
}
