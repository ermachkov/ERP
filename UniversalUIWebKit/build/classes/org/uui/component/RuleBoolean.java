/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import org.ubo.rules.RuleItem;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public abstract class RuleBoolean extends Component {

    private RuleItem ruleItem;
    private CheckBox checkBox;

    public RuleBoolean(String sessionId, final RuleItem ruleItem) throws RuleItemTypeException {
        super(sessionId);
        
        if (ruleItem.getType() != RuleItem.BOOLEAN) {
            throw new RuleItemTypeException("RuleItem " + ruleItem + " is not RuleItem.BOOLEAN");
        }
        this.ruleItem = ruleItem;

        checkBox = new CheckBox(sessionId, ruleItem.getDescription());
        checkBox.setChecked((Boolean) ruleItem.getValue());
        checkBox.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                ruleItem.setValue(checkBox.isChecked());
                change(ruleItem, checkBox.isChecked());
            }
        });
    }
    
    public abstract void change(RuleItem ruleItem, boolean isChecked);

    public RuleItem getRuleItem() {
        return ruleItem;
    }

    @Override
    public String getModel() {
        return "<dt><div class='ruleBooleanBlock'>" + checkBox.getModel() + "</div></dt>";
    }
}
