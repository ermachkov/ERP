/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.explorer;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.Timer;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class MouseHandler {

    private Timer timer;
    private Component component;
    private MouseEvent event;
    private int timerInterval;

    private MouseHandler() {
        timerInterval = (Integer) Toolkit.getDefaultToolkit().getDesktopProperty(
                "awt.multiClickInterval");
        timer = new Timer(timerInterval, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                single();
            }
        });
        timer.setRepeats(false);
    }

    public void start(Component component, MouseEvent event) {
        this.component = component;
        this.event = event;

        if (timer != null) {
            if (timer.isRunning()) {
                component.dispatchEvent(new MouseEvent(component,
                        event.getID(),
                        event.getWhen(),
                        event.getModifiers(),
                        event.getX(),
                        event.getY(),
                        2,
                        false));
                timer.stop();
            }
        }

        timer.start();
    }

    private void single() {
        component.dispatchEvent(new MouseEvent(component,
                event.getID(),
                event.getWhen(),
                event.getModifiers(),
                event.getX(),
                event.getY(),
                1,
                false));
    }

    public static MouseHandler getInstance() {
        return MouseHandlerHolder.INSTANCE;
    }

    private static class MouseHandlerHolder {

        private static final MouseHandler INSTANCE = new MouseHandler();
    }
}
