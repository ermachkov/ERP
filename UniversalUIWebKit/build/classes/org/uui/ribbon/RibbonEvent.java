/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.ribbon;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.json.JSONObject;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class RibbonEvent {

    private String json;

    public RibbonEvent(String json) {
        this.json = json;
    }

    public String getJSON() {
        return json;
    }

    public JSONObject getJSONObject() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, json, e);
        }

        return jsonObject;
    }

    public String toString() {
        return "RibbonEvent{" + "json=" + json + '}';
    }
}
