/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ubo.payment;

/**
 *
 * @author Pechenko Anton aka parilo, forpost78 aaaaaat gmail doooooot com
 *         (C) Copyright by Pechenko Anton, created 29.03.2011
 */
public class TransferType {

    public static int TYPE_BALANCEADD = 1; // пополнение баланса
    public static int TYPE_BALANCEADDCOMM = 2; // комиссия за пополнение баланса
    public static int TYPE_TERMINALPAY = 3; //платеж с терминала
    public static int TYPE_INCOMMISSION = 4; //внутренняя комиссия
    public static int TYPE_OUTCOMMISSION = 5; //внешняя комиссия
    public static int TYPE_CREDIT = 6; //средства по кредиту

    //через что проведена проводка
    public static int THROUGH_TYPE_BANK = 1;
    public static int THROUGH_TYPE_CASHBOX = 2;
    public static int THROUGH_TYPE_VIRTUALPAY = 3;
    
}
