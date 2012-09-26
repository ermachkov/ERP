/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.db.event;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class DataBaseServiceEvent {

    private String json;

    public DataBaseServiceEvent(String json) {
        this.json = json;
    }

    public String getEvent() {
        return json;
    }

    @Override
    public String toString() {
        return "DataBaseServiceEvent{" + "json=" + json + '}';
    }
}
