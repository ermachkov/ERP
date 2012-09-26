/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import java.util.LinkedList;
import org.ubo.rules.Rule;
import org.ubo.rules.RuleItem;
import org.uui.webkit.WebKitComponent;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public abstract class RulePanel extends Component {

    private LinkedList<WebKitComponent> components = new LinkedList<>();
    
    public RulePanel(String sessionId, Rule rule) throws RuleItemTypeException {
        super(sessionId);
        for(RuleItem ruleItem : rule.getRuleItems()){
            if(ruleItem.getType() == RuleItem.BOOLEAN){
                RuleBoolean ruleBoolean = new RuleBoolean(sessionId, ruleItem) {

                    @Override
                    public void change(RuleItem ruleItem, boolean isChecked) {
                        getMe().change(ruleItem, isChecked);
                    }
                };
                components.add(ruleBoolean);
            }
            
            if(ruleItem.getType() == RuleItem.RADIO){
                RuleRadio ruleRadio = new RuleRadio(sessionId, ruleItem) {

                    @Override
                    public void change(RuleItem ruleItem, boolean isChecked) {
                        getMe().change(ruleItem, isChecked);
                    }
                };
                components.add(ruleRadio);
            }
            
            if(ruleItem.getType() == RuleItem.MULTI_SELECT){
                RuleMultiSelect ruleMultiSelect = new RuleMultiSelect(sessionId, ruleItem) {

                    @Override
                    public void change(RuleItem ruleItem, boolean isChecked) {
                        getMe().change(ruleItem, isChecked);
                    }
                };
                components.add(ruleMultiSelect);
            }
        }
    }
    
    public RulePanel getMe(){
        return this;
    }
    
    public abstract void change(RuleItem ruleItem, boolean isChecked);
    
    public boolean isPanelEmpty(){
        return components.isEmpty();
    }

    @Override
    public String getModel() {
        String model = "<div class='ruleBlockSet'><section>"
                + "<details>"
                + "<summary>"
                + "Набор правил для модуля"
                + "</summary>"
                + "<dl>";
        
        for (WebKitComponent wc : components) {
            model = model + wc.getModel();
        }

        model = model + "</dl>"
                + "</details>"
                + "</section>"
                + "</div>";
        return model;
    }
}
