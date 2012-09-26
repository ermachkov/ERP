/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unet.tester;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Tester implements Serializable{
    
    public Tester(){
        
    }
    
    public BigDecimal getSum(double one, double two){
        System.out.println("get sum////////////////////");
        BigDecimal res = new BigDecimal(one).add(new BigDecimal(two));
        return res;
    }
    
}
