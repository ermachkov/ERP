/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com
 * Omsk, Russia, created 28.03.2011
 * (C) Copyright by Zubanov Dmitry
 */

package org.jssdb.core.proxy;

import java.io.Serializable;
import java.util.Arrays;
import javax.swing.ImageIcon;

public class ProxyImageIcon implements Serializable, KnowsId{
    
    private long id;
    static final long serialVersionUID = 1L;
    private byte md5[];
    private ImageIcon imageIcon;
    
    public void setIcon(ImageIcon icon){
        imageIcon = icon;
    }
    
    public ImageIcon getImageIcon(){
        return imageIcon;
    }
    
    public void setMD5(byte[] md5){
        this.md5 = md5;
    }
    
    public byte[] getMD5(){
        return md5;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    public String toString() {
        return "TreeImage{" + "id=" + id + ", md5=" + Arrays.toString(md5) + ", imageIcon=" + imageIcon + '}';
    }
    
    
}
