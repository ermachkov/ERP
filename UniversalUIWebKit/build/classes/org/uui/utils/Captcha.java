/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Captcha {

    public static void createCaptcha(String str, int width, int height, Path imagePath) {
        Captcha captcha = new Captcha();
        captcha.generateCaptcha(str, width, height, imagePath);
    }

    private void generateCaptcha(String str, int width, int height, Path imagePath) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setBackground(Color.GRAY);
        g2d.setColor(Color.BLACK);
        g2d.drawString(str, 10, height / 2);
        if (imagePath.toFile().exists()) {
            imagePath.toFile().delete();
        }

        try {
            ImageIO.write(bufferedImage, "PNG", imagePath.toFile());
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, str, e);
        }
    }
}
