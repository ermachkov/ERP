/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jssdb.tests;

import java.io.Serializable;
import org.jssdb.core.proxy.KnowsId;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class DFC implements Serializable, KnowsId {

    private long id;
    private String name = "";
    static final long serialVersionUID = 1;

    public DFC() {
        //
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        return "DFC{" + "id=" + id + ", name=" + name + '}';
    }
}
