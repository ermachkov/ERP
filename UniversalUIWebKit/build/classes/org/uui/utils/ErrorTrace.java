/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class ErrorTrace {

    public static String getTrace(Exception e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        e.printStackTrace(ps);
        return new String(baos.toByteArray());
    }
}
