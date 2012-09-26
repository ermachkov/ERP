/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.uc;

/**
 *
 * @author Pechenko Anton aka parilo, forpost78 aaaaaat gmail doooooot com
 *         (C) Copyright by Pechenko Anton, created 22.04.2011
 */
public class UniversalConfig {

    private static UniversalConfig self = null;
    
    public String printResourcesDir = "";    
    public String iconsDir = "";    
    
    private UniversalConfig(){}
    
    public static UniversalConfig getInstance() {
        if (self == null) {
            self = new UniversalConfig();
        }

        return self;
    }

}
