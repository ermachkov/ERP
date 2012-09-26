/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package money;

import org.ubo.money.Money;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class TestMoney {
    
    public static void main(String args[]){
        System.out.println(Money.formatToMoney(10.409999d));
        System.out.println(Money.ADD("10", "10"));
    }
    
}
