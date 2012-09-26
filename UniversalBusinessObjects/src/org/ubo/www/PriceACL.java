/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.www;

import java.io.Serializable;
import org.jssdb.core.proxy.KnowsId;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class PriceACL implements Serializable, KnowsId {

    static final long serialVersionUID = 1L;
    private long id, treeLeafId;
    private boolean allowShow, allowUse;

    public PriceACL() {
        //
    }

    public boolean isAllowShow() {
        return allowShow;
    }

    public void setAllowShow(boolean allowShow) {
        this.allowShow = allowShow;
    }

    public boolean isAllowUse() {
        return allowUse;
    }

    public void setAllowUse(boolean allowUse) {
        this.allowUse = allowUse;
    }

    public long getTreeLeafId() {
        return treeLeafId;
    }

    public void setTreeLeafId(long treeLeafId) {
        this.treeLeafId = treeLeafId;
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
        return "PriceACL{" + "id=" + id + ", treeLeafId=" + treeLeafId + ", allowShow=" + allowShow + ", allowUse=" + allowUse + '}';
    }
}
