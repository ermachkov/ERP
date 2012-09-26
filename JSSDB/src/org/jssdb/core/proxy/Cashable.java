/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jssdb.core.proxy;

/**
 *
 * @author anton
 */
public interface Cashable {

    public String getCashableId();

    public void setCashableId(String id);

    public byte[] getCashableData();

    public void setCashableData(byte[] data);
}
