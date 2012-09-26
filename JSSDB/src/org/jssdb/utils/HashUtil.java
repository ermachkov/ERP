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
package org.jssdb.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class HashUtil {

    public static byte[] getHash(Object object) {
        return new HashUtil().computeHash(object);
    }

    public static byte[] getHash(byte[] b) {
        byte[] md5 = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md5 = md.digest(b);
            
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return md5;
        }

        return md5;
    }

    public byte[] computeHash(Object object) {
        if (object == null) {
            System.err.println("computeHash for " + object);
            return null;
        }

        byte[] md5 = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            MessageDigest md = MessageDigest.getInstance("MD5");
            md5 = md.digest(baos.toByteArray());

        } catch (IOException | NoSuchAlgorithmException e) {
            Logger.getGlobal().log(Level.WARNING, Objects.toString(object), e);
        }

        return md5;
    }
}
