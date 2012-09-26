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
 * Omsk, Russia, created 29.05.2010
 * (C) Copyright by Zubanov Dmitry
 */


package org.jssdb.core;

import java.util.Map;
import org.jssdb.utils.MetaInfoData;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public interface DBHandler {

    public long add(Object obj);

    public Object get(long id, Class cls);

    public Object get(long id, String className);

    public boolean update(long id, Object obj);

    public boolean delete(long i, Class cls);

    public boolean delete(long i, String className);
    
    public Map<Long, Object> getCollection(Class cls);

    public Map<Long, Object> getCollection(String className);

    public long getMinId(Class cls);

    public long getMinId(String className);

    public long getMaxId(Class cls);

    public long getMaxId(String className);

    public MetaInfoData getMetaInfo(long id, Class cls);

}
