/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.webkit;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class WebKitUtil {

    public static String prepareToJS(String str) {
        str = str.replaceAll("'", "\"");
        str = str.replaceAll("\r", "");
        str = str.replaceAll("\n", "");
        return str;
    }
}
