/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jssdb.filesystem;

/**
 *
 * @author developer
 */
public class Marker{

    long startIndexPosition, id, start, end, timestamp, service;

    Marker(long startIndexPosition, long id, long start, long end, long timestamp, long service) {
        this.startIndexPosition = startIndexPosition;
        this.id = id;
        this.start = start;
        this.end = end;
        this.timestamp = timestamp;
        this.service = service;
    }
}
