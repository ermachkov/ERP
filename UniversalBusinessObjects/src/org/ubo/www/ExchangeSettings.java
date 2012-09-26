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
public class ExchangeSettings implements Serializable, KnowsId {

    private boolean enabled = false;
    private int exchangeTimeOut = 1;
    private String remoteHost = "", login = "", password = "";
    static final long serialVersionUID = 1L;
    private long id;

    public ExchangeSettings() {
        //
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getExchangeTimeOut() {
        return exchangeTimeOut;
    }

    public void setExchangeTimeOut(int exchangeTimeOut) {
        this.exchangeTimeOut = exchangeTimeOut;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
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
        return "ExchangeSettings{" + "enabled=" + enabled + ", exchangeTimeOut=" + exchangeTimeOut + ", remoteHost=" + remoteHost + ", id=" + id + '}';
    }
}
