/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.rules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArraySet;
import org.jssdb.core.proxy.KnowsId;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class Rules implements Serializable, KnowsId {

    private CopyOnWriteArraySet<Rule> rules = new CopyOnWriteArraySet<>();
    private long userId, id;
    public static final long serialVersionUID = 1L;

    public Rules() {
        //
    }

    public void setUserId(long id) {
        userId = id;
    }

    public long getUserId() {
        return userId;
    }

    public boolean addRule(Rule rule) {
        return rules.add(rule);
    }

    public ArrayList<Rule> getRules() {
        ArrayList<Rule> list = new ArrayList<>();
        list.addAll(rules);
        Collections.sort(list, new Comparator<Rule>() {

            @Override
            public int compare(Rule o1, Rule o2) {
                return o1.getModuleName().compareToIgnoreCase(o2.getModuleName());
            }
        });
        return list;
    }

    public boolean removeRule(Rule rule) {
        return rules.remove(rule);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Rules{" + "rules=" + rules + ", userId=" + userId + ", id=" + id + '}';
    }
}
