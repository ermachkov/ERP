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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class EventHashUtil {

    public static byte[] getHash(File file) {
        return new EventHashUtil().computeHash(file);
    }

    public byte[] computeHash(File file) {
        if (file == null) {
            System.err.println("computeHash for " + file);
            return null;
        }

        byte[] md5 = null;
        try {
            byte b[] = Files.readAllBytes(Paths.get(file.toString()));
            MessageDigest md = MessageDigest.getInstance("MD5");
            md5 = md.digest(b);

        } catch (IOException | NoSuchAlgorithmException e) {
            Logger.getGlobal().log(Level.WARNING, Objects.toString(file), e);
        }

        return md5;
    }
}
