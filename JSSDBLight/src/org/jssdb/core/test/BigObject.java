/*
 *  Copyright (C) 2010 Zubanov Dmitry
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com
 * Omsk, Russia, created 21.09.2010
 * (C) Copyright by Zubanov Dmitry
 */

package org.jssdb.core.test;

import java.io.Serializable;
import java.util.ArrayList;

public class BigObject implements Serializable{
    
    ArrayList<String> strList;

    public BigObject(int howMach){
        setObject(howMach);
    }

    private void setObject(int howMach){
        strList = new ArrayList();
        for (int i = 0; i < howMach; i++) {
            strList.add("This is easy string. Here count number is " + i);
        }
    }

    public ArrayList<String> getList(){
        return strList;
    }

}
