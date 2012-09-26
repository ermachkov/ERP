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
 * Omsk, Russia, created 17.10.2010
 * (C) Copyright by Zubanov Dmitry
 */

package org.jssdb.query;

import java.io.Serializable;
import java.text.MessageFormat;

public class Limit implements Serializable{
    
    private long from, len;
    static final long serialVersionUID = 100000000000099004L;

    public Limit(long from, long len){
        this.from = from;
        this.len = len;
    }

    public long getFrom() {
        return from;
    }

    public long getLen() {
        return len;
    }

    @Override
    public String toString(){
        return MessageFormat.format("from {0}, lenght {1}", new Object[]{from, len});
    }

}
