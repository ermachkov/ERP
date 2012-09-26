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
package org.jssdb.core.proxy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.jssdb.core.DBProperties;
import org.jssdb.handler.FileStorageHandler;
import org.jssdb.utils.FileInfo;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class ImageIconHandler {

    private static ImageIconHandler self = null;
    private FileStorageHandler fsh;

    private ImageIconHandler() {
        fsh = FileStorageHandler.getDefault();
    }

    public static synchronized ImageIconHandler getInstance() {
        if (self == null) {
            self = new ImageIconHandler();
        }

        return self;
    }

    public ProxyImageIcon handle(ImageIcon imageIcon) {
        ProxyImageIcon proxyImage = null;
        MessageDigest md;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(imageIcon);
            md = MessageDigest.getInstance("MD5");
            byte[] md5 = md.digest(baos.toByteArray());

            boolean isFind = false;
            String strDir = DBProperties.getInstance().getPathToDB() + ProxyImageIcon.class.getName();
            Path p = Paths.get(strDir);
            if (!p.toFile().exists()) {
                p.toFile().mkdir();
            }

            for (File file : p.toFile().listFiles()) {
                if (file.isDirectory()) {
                    continue;
                }

                if (!file.isFile()) {
                    continue;
                }

                ProxyImageIcon ti = (ProxyImageIcon) ProxyIO.read(file.getPath());
                if (Arrays.equals(ti.getMD5(), md5)) {
                    proxyImage = ti;
                    isFind = true;
                    break;
                }
            }

            if (!isFind) {
                proxyImage = new ProxyImageIcon();
                proxyImage.setIcon(imageIcon);
                proxyImage.setMD5(md5);
                FileInfo fInfo = getOptimisticFileInfo(proxyImage);
                proxyImage.setId(fInfo.getNumber());
                ProxyIO.write(fInfo.getFileName(), proxyImage);
            }

        } catch (IOException | NoSuchAlgorithmException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);

        } finally {
            return proxyImage;
        }
    }

    private synchronized FileInfo getOptimisticFileInfo(Object object) {
        File f = new File(DBProperties.getInstance().getPathToDB() + object.getClass().getName());

        if (!f.isDirectory()) {
            f.mkdir();
        }

        FileInfo fInfo = fsh.getSaveFileInfo(f);
        return fInfo;
    }
}
