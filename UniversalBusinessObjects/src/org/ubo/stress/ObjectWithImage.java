/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.stress;

import java.io.Serializable;
import javax.swing.ImageIcon;
import org.jssdb.core.proxy.KnowsId;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class ObjectWithImage implements Serializable, KnowsId{
    
    private ImageIcon icon;
    private long id;
    
    public void setImage(ImageIcon icon){
        this.icon = icon;
    }
    
    public ImageIcon getImage(){
        return this.icon;
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
