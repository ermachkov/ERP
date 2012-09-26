/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class RadioButtonGroup {

    private Set<RadioButton> buttonSet = new HashSet<>();

    public RadioButtonGroup() {
        //
    }

    public RadioButtonGroup(RadioButton... buttons) {
        Objects.requireNonNull(buttons);
        buttonSet.addAll(Arrays.asList(buttons));
        for(final RadioButton radioButton : buttonSet){
            radioButton.addUIEventListener(new UIEventListener() {

                @Override
                public void event(UIEvent evt) {
                    setUncheckedAll();
                    radioButton.setChecked(true);
                    Logger.getGlobal().log(Level.INFO, "Debug: switched to checked {0}", radioButton);
                }
            });
        }
    }

    public boolean addRadioButton(final RadioButton radioButton) {
        boolean result = buttonSet.add(radioButton);
        if (result) {
            radioButton.addUIEventListener(new UIEventListener() {

                @Override
                public void event(UIEvent evt) {
                    setUncheckedAll();
                    radioButton.setChecked(true);
                    Logger.getGlobal().log(Level.INFO, "Debug: switched to checked {0}", radioButton);
                }
            });
        }
        return buttonSet.add(radioButton);
    }

    public boolean removeRadioButton(RadioButton radioButton) {
        return buttonSet.remove(radioButton);
    }

    public void setUncheckedAll() {
        for (RadioButton radioButton : buttonSet) {
            radioButton.setChecked(false);
        }
    }

    @Override
    public String toString() {
        return "RadioButtonGroup{" + "buttonSet=" + buttonSet + '}';
    }
}
