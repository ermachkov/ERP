/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.print;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class MediaFormat {

    private double width, height, marginTop, marginRight, marginBottom, marginLeft;
    private String masterName;

    private MediaFormat(String masterName, double width, double height, double marginTop,
            double marginRight, double marginBottom, double marginLeft) {
        this.masterName = masterName;
        this.width = width;
        this.height = height;
        this.marginTop = marginTop;
        this.marginRight = marginRight;
        this.marginBottom = marginBottom;
        this.marginLeft = marginLeft;
    }
    
    public static MediaFormat POS_80x160() {
        return new MediaFormat("Custom", 8, 16, .5, .5, .5, .5);
    }
    
    public static MediaFormat POS_72x160() {
        return new MediaFormat("Custom", 7.2, 16, .5, .5, .5, .5);
    }

    public static MediaFormat A4Portrait() {
        return new MediaFormat("A4", 21, 29.7, 1, 1, 1, 1);
    }
    
    public static MediaFormat A4Portrait(double marginTop,
            double marginRight, double marginBottom, double marginLeft) {
        return new MediaFormat("A4", 21, 29.7, marginTop, marginRight, 
                marginBottom, marginLeft);
    }
    
    public static MediaFormat A4Landscape() {
        return new MediaFormat("A4", 29.7, 21, 1, 1, 1, 1);
    }
    
    public static MediaFormat A4Landscape(double marginTop,
            double marginRight, double marginBottom, double marginLeft) {
        return new MediaFormat("A4", 29.7, 21, marginTop, marginRight, 
                marginBottom, marginLeft);
    }
    
    public Map<String, String> getXSLPageFormat(){
        Map<String, String> map = new HashMap<>();
        map.put("master-name", masterName);
        map.put("page-width", Objects.toString(width) + "cm");
        map.put("page-height", Objects.toString(height) + "cm");
        map.put("margin-top", Objects.toString(marginTop) + "cm");
        map.put("margin-right", Objects.toString(marginRight) + "cm");
        map.put("margin-bottom", Objects.toString(marginBottom) + "cm");
        map.put("margin-left", Objects.toString(marginLeft) + "cm");
        return map;
    }

    public double getHeight() {
        return height;
    }

    public double getMarginBottom() {
        return marginBottom;
    }

    public double getMarginLeft() {
        return marginLeft;
    }

    public double getMarginRight() {
        return marginRight;
    }

    public double getMarginTop() {
        return marginTop;
    }

    public double getWidth() {
        return width;
    }
    
    public String getPageFormat(){
        return masterName;
    }
}
