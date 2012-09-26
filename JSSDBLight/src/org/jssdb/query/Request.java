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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Request implements Serializable {

    private Class clazz;
    private LinkedHashMap<String, Object[]> methods;
    private LinkedHashMap<String, Expression> filter;
    private SelectById selectById;
    private Limit limit;
    static final long serialVersionUID = 100000000000099001L;

    /**
     * 
     * @param className
     * @param methods
     * @param filter
     * @param expId
     * @param limit
     * @throws ClassNotFoundException
     */
    public Request(String className, LinkedHashMap<String, Object[]> methods,
            LinkedHashMap<String, Expression> filter, SelectById expId, Limit limit)
            throws ClassNotFoundException {

        clazz = Class.forName(className);
        this.methods = methods;
        this.filter = filter;
        this.selectById = expId;
        this.limit = limit;
    }

    public Request(Class cls) throws ClassNotFoundException {
        clazz = cls;
    }

    public Request(String className) throws ClassNotFoundException {
        clazz = Class.forName(className);
    }

    public void setFilter(LinkedHashMap<String, Expression> filter) {
        this.filter = filter;
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    public void setMethods(LinkedHashMap<String, Object[]> methods) {
        this.methods = methods;
    }

    public void setSelectById(SelectById selectById) {
        this.selectById = selectById;
    }

    public Class getClazz() {
        return clazz;
    }

    public SelectById getSelectById() {
        return selectById;
    }

    public Limit getLimit() {
        return limit;
    }

    public Map<String, Object[]> getMethods() {
        return this.methods;
    }

    public LinkedHashMap<String, Expression> getFilter() {
        return this.filter;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Request other = (Request) obj;
        if (!Objects.equals(this.clazz, other.clazz)) {
            return false;
        }
        if (!Objects.equals(this.methods, other.methods)) {
            return false;
        }
        if (!Objects.equals(this.filter, other.filter)) {
            return false;
        }
        if (!Objects.equals(this.selectById, other.selectById)) {
            return false;
        }
        if (!Objects.equals(this.limit, other.limit)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.clazz);
        hash = 13 * hash + Objects.hashCode(this.methods);
        hash = 13 * hash + Objects.hashCode(this.filter);
        hash = 13 * hash + Objects.hashCode(this.selectById);
        hash = 13 * hash + Objects.hashCode(this.limit);
        return hash;
    }
}
