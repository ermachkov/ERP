/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.money;

import java.math.BigDecimal;
import java.util.Objects;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class MoneyTest {
    
    public MoneyTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testDivide() {
        BigDecimal creditValue = Money.DIVIDE("0.7", "100");
        System.out.println("creditValue = " + creditValue);
        BigDecimal creditSum = Money.MULTIPLY(
                Objects.toString(new BigDecimal("10")), 
                Objects.toString(new BigDecimal("0.7")));
        creditSum = Money.DIVIDE(
                Objects.toString(creditSum), 
                Objects.toString(new BigDecimal("100")));
        System.out.println(creditSum);
    }
}
