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

package org.jssdb.handler;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import org.jssdb.core.DBProperties;
import org.jssdb.utils.MetaInfoData;

/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com
 * Omsk, Russia,
 * created 09.05.2010
 */
public class MetaInfo {

   private static MetaInfo self = null;
   private static final Logger logger = Logger.getLogger(MetaInfo.class.getName());

    public static MetaInfo getDefault(){
        if(self == null)
            self = new MetaInfo();

        return self;
    }
    
    public MetaInfoData getMetaInfo(long id, String className){
        if(className == null){
            return null;
        }
        
        Path p = Paths.get(DBProperties.getInstance().getPathToDB(), className, "" + id);
        if(!p.toFile().exists()){
            return null;
        }

        return new MetaInfoData(p.toFile());
    }

    public MetaInfoData getMetaInfo(long id, Class cls){
        return getMetaInfo(id, cls.getName());
    }

}
