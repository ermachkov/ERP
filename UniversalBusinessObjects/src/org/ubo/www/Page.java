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
public class Page implements Serializable, KnowsId {

    private String content = "";
    private String path = "";
    static final long serialVersionUID = 1L;
    private long id;

    public Page() {
        //
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
        return "Page{" + "content=" + content + ", path=" + path + ", id=" + id + '}';
    }
}
