/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jssdb.event;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class ServiceEvent {

    private String json;

    public ServiceEvent(String json) {
        this.json = json;
    }

    public String getEvent() {
        return json;
    }

    @Override
    public String toString() {
        return "ServiceEvent{" + "json=" + json + '}';
    }
}
