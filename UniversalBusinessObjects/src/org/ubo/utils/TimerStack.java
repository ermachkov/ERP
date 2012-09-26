/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ubo.utils;

import java.util.Date;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class TimerStack {
    
    private Date dateStart;

    public TimerStack() {
        dateStart = new Date();
    }
    
    public void fix(){
        dateStart = new Date();
    }
    
    public void print(){
        long t = new Date().getTime() - dateStart.getTime();
        System.out.println("time = " + t);
        StackTraceElement ste[] = Thread.currentThread().getStackTrace();
        System.out.println(ste[2]);
    }
    
    public void printAndFix(){
        long t = new Date().getTime() - dateStart.getTime();
        System.out.print("Stack time point = " + t + ", ");
        dateStart = new Date();
        StackTraceElement ste[] = Thread.currentThread().getStackTrace();
        System.out.println(ste[2]);
    }
    
    public void printStackAndFix(){
        long t = new Date().getTime() - dateStart.getTime();
        System.out.print("Stack time point = " + t + ", ");
        dateStart = new Date();
        StackTraceElement ste[] = Thread.currentThread().getStackTrace();
        for (int i = 2; i < ste.length; i++) {
            System.out.println("Stack " + ste[i]);
        }
    }
 }
