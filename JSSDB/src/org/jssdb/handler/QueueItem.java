/*
 * Copyright (C) 2011 developer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jssdb.handler;

import java.util.Date;
import java.util.Objects;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
class QueueItem implements Comparable {

    private long id;
    private Object object;
    private long timeMark;

    public QueueItem(long id, Object object) {
        this.id = id;
        this.object = object;
        timeMark = new Date().getTime();
    }

    public Object getObject() {
        return object;
    }

    public long getId() {
        return id;
    }

    public long getTimeMark() {
        return timeMark;
    }

    public String getObjectClass() {
        return object.getClass().getName();
    }

    public String toString() {
        return "QueueItem{" + "id=" + id + ", object=" + object + ", timeMark=" + timeMark + '}';
    }

    @Override
    public int compareTo(Object o) {
        int compare = 0;
        if (o instanceof QueueItem) {
            QueueItem o1 = (QueueItem) o;
            Long val = o1.getTimeMark();
            Long thisVal = getTimeMark();
            compare = thisVal.compareTo(val);
        }

        return compare;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QueueItem other = (QueueItem) obj;
        if (this.id != other.id) {
            return false;
        }
        if (!Objects.equals(this.object, other.object)) {
            return false;
        }
        if (this.timeMark != other.timeMark) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 37 * hash + Objects.hashCode(this.object);
        hash = 37 * hash + (int) (this.timeMark ^ (this.timeMark >>> 32));
        return hash;
    }
}
