/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com
 * Omsk, Russia, created 20.03.2011
 * (C) Copyright by Zubanov Dmitry
 */
package ru.sibek.plugin.goods;

import java.text.DateFormat;
import java.util.Date;
import org.ubo.datetime.DateTime;

public class DateTest {

    public static void main(String args[]) {
        DateTime dt = new DateTime();
        System.out.println(dt.getFormatedDate("yyyy-MM-dd HH:mm:ss", new Date()));
    }
}
