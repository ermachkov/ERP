/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.rules;

import java.io.Serializable;
import java.util.concurrent.CopyOnWriteArraySet;
import org.jssdb.core.proxy.KnowsId;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class GlobalRules implements Serializable, KnowsId{
    
    private CopyOnWriteArraySet<GlobalRuleItem> rules = new CopyOnWriteArraySet<>();
    private long id;
    
    public GlobalRules(){
    }
    
    public boolean addGlobalRule(GlobalRuleItem globalRuleItem){
        return rules.add(globalRuleItem);
    }
    
    public CopyOnWriteArraySet<GlobalRuleItem> getGloabalRules(){
        return rules;
    }
    
    public GlobalRuleItem getGlobalRuleItemByKey(String ruleKey){
        GlobalRuleItem item = null;
        for(GlobalRuleItem globalRuleItem : rules){
            if(globalRuleItem.getKey().equals(ruleKey)){
                item = globalRuleItem;
                break;
            }
        }
        
        return item;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }
    
}
