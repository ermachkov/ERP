/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine.atol;

import java.util.ArrayList;

/**
 *
 * @author Dmitry Zubanov (zubanov@gmail.com)
 * date 30.07.2009
 */
public class DataHandler {

    private static DataHandler self = null;

    private DataHandler(){
        //
    }

    public synchronized static DataHandler getDefault(){
        if(self == null)
            self = new DataHandler();

        return self;
    }

    public int[] getDataBlock(int[] data) {
        ArrayList<Integer> v = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            if (data[i] == 0x10) {
                // Маскируем байт DLE (0x10)
                v.add(0x10);
            }

            if (data[i] == 0x03) {
                // Маскируем байт ETX (0x03)
                v.add(0x10);
            }

            v.add(data[i]);
        }
        // добавляем в конец ETX
        v.add(0x03);

        int crc = 0;
        for(int i = 0; i < v.size(); i++){
            crc = crc ^ v.get(i).intValue();
        }

        //System.out.println("crc " + crc);
        
        // Добавляем в начало STX
        v.add(0, 0x02);
        
        // Добавляем в конец <CRC>
        v.add(crc);

        int result[] = new int[v.size()];
        for (int i = 0; i < v.size(); i++) {
            result[i] = v.get(i).intValue();
            //System.out.println("result[" + i + "] " + result[i]);
        }

        return result;
    }
}
