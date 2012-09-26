/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.event;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.json.JSONObject;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class UIEvent {

    private String json;

    public UIEvent(String json) {
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

    @Override
    public String toString() {
        return "ExplorerEvent{" + "json=" + json + '}';
    }
}
