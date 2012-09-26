/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.business.users;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.jssdb.core.proxy.KnowsId;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class User implements Serializable, KnowsId {

    private String name = "", login, password, imageFileName;
    public static final long serialVersionUID = 1L;
    private long id, rulesId;
    private Map<String, String> extraInfo = new HashMap<>();

    public User() {
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        if (name == null) {
            name = "";
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getRulesId() {
        return rulesId;
    }

    public void setRulesId(long rulesId) {
        this.rulesId = rulesId;
    }

    @Override
    public String toString() {
        return "User{" + "name=" + name + ", login=" + login + ", password=" + password + ", imageFileName=" + imageFileName + ", id=" + id + ", rulesId=" + rulesId + ", extraInfo=" + extraInfo + '}';
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    public void addExtraInfo(String key, String value) {
        extraInfo.put(key, value);
    }

    public Map getExtraInfo() {
        return extraInfo;
    }

    public String getExtraInfoByKey(String key) {
        String value = extraInfo.get(key);
        if (value == null) {
            value = "";
        }

        return value;
    }
}
