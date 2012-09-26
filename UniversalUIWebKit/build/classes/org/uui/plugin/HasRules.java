/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.plugin;

import java.util.ArrayList;
import org.ubo.rules.RuleItem;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public interface HasRules {
    
    /**
     * 
     * @param userSystemId
     * User system id any positive int. 0 value reserved for admin. 1000 for ordinary user.
     * @return 
     */
    public ArrayList<RuleItem> getRuleItemsTemplate(int userSystemId);
    
}
