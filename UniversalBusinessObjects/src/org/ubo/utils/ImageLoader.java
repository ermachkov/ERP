/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.utils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class ImageLoader {

    private ImageLoader() {
    }

    public ImageIcon getFromFile(String file) {
        ImageIcon icon = null;

        if (!Paths.get(file).toFile().exists()) {
            return null;
        }

        try {
            icon = new ImageIcon(ImageIO.read(new File(file)));
        } catch (IOException ex) {
            Logger.getLogger(ImageLoader.class.getName()).log(Level.SEVERE, Objects.toString(file), ex);
        } finally {
            return icon;
        }
    }
    
    public static boolean createCompositeFolderImage(String dstFileName){
        try {
            BufferedImage bi = ImageLoader.getInstance().getBufferedImageFromFile(dstFileName);
            Graphics2D g2d = bi.createGraphics();
            BufferedImage smallFolder = ImageLoader.getInstance().getBufferedImageFromFile(
                    Paths.get(System.getProperty("user.home"), ".saas", "app", "ui", "img", "src", "folder_empty_32x32.png").toString());
            g2d.drawImage(smallFolder, 32, 32, null);
            g2d.dispose();
            ImageIO.write(bi, "PNG", new File(dstFileName));
            
            return true;
            
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, dstFileName, e);
            return false;
        }
    }
    
    public BufferedImage getBufferedImageFromFile(String file) {
        ImageIcon icon = null;
        BufferedImage bImage = null;

        if (!Paths.get(file).toFile().exists()) {
            return null;
        }

        try {
            icon = new ImageIcon(ImageIO.read(new File(file)));
            bImage = new BufferedImage(icon.getIconWidth(),
                    icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics g = bImage.getGraphics();
            g.drawImage(icon.getImage(), 0, 0, null);
            g.dispose();
            
        } catch (IOException ex) {
            Logger.getLogger(ImageLoader.class.getName()).log(Level.SEVERE, Objects.toString(file), ex);
        } finally {
            return bImage;
        }
    }

    public ImageIcon getFromClass(String res) {
        ImageIcon icon = null;
        try {
            icon = new javax.swing.ImageIcon(getClass().getResource(res));

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, res, e);

        } finally {
            return icon;
        }
    }

    public ImageIcon getFromClass(String res, int width, int height) {
        ImageIcon icon = null;
        BufferedImage bi = getFromClassBufferedImage(res);
        if (bi != null) {
            BufferedImage bImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D g = bImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(bi, 0, 0, width, height, null);
            icon = new ImageIcon(bImage);
        }

        return icon;
    }

    public BufferedImage getFromClassBufferedImage(String res) {
        BufferedImage bImage = null;
        ImageIcon icon = null;
        try {
            icon = new javax.swing.ImageIcon(getClass().getResource(res));

            bImage = new BufferedImage(icon.getIconWidth(),
                    icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics g = bImage.getGraphics();
            g.drawImage(icon.getImage(), 0, 0, null);
            g.dispose();

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, res, e);

        }
        return bImage;
    }

    public VolatileImage getFromClassVolatileImage(String res) {
        ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(res));
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
        VolatileImage vImage = gc.createCompatibleVolatileImage(icon.getIconWidth(),
                icon.getIconHeight(), Transparency.TRANSLUCENT);
        Graphics2D g2d = vImage.createGraphics();
        g2d.setComposite(AlphaComposite.Src);
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, vImage.getWidth(), vImage.getHeight()); // Clears the image.
        g2d.drawImage(icon.getImage(), 0, 0, null);
        return vImage;
    }

    public static ImageLoader getInstance() {
        return ImageLoaderHolder.INSTANCE;
    }

    private static class ImageLoaderHolder {

        private static final ImageLoader INSTANCE = new ImageLoader();
    }
}
