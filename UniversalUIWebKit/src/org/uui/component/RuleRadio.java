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
public abstract class RuleRadio extends Component{
    
    private RuleItem ruleItem;
    private String model;
    
    public RuleRadio(String sessionId, RuleItem ruleItem) throws RuleItemTypeException{
        super(sessionId);
        if(ruleItem.getType() != RuleItem.RADIO){
            throw new RuleItemTypeException("RuleItem " + ruleItem + " is not RuleItem.RADIO");
        }
        this.ruleItem = ruleItem;
        
        init();
    }
    
    private void init(){
        String _model = "<dt><div class='ruleRadioBlock'>" + ruleItem.getDescription() + "<br/>";
        for(final SelectorRuleItem item : (LinkedList<SelectorRuleItem>) ruleItem.getValue()){
            final RadioButton radioButton = new RadioButton(getSession(), ruleItem.getKey(), 
                    item.getDescription(), item.isSelected());
            radioButton.addUIEventListener(new UIEventListener() {

                @Override
                public void event(UIEvent evt) {
                    for(SelectorRuleItem i : (LinkedList<SelectorRuleItem>) ruleItem.getValue()){
                        i.setSelected(false);
                    }
                    item.setSelected(radioButton.isChecked());
                    change(ruleItem, radioButton.isChecked());
                }
            });
            _model = _model + radioButton.getModel() + "<br/>";
        }
        
        _model = _model + "</div></dt>";
        model = _model;
    }
    
    public abstract void change(RuleItem ruleItem, boolean isChecked);

    @Override
    public String getModel() {
        return model;
    }
}
