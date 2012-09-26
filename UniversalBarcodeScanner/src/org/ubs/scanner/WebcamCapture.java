/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubs.scanner;

import au.edu.jcu.v4l4j.CaptureCallback;
import au.edu.jcu.v4l4j.FrameGrabber;
import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.VideoDevice;
import au.edu.jcu.v4l4j.VideoFrame;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.ubo.utils.XPathUtil;

/**
 *
 * @author developer
 */
public class WebcamCapture implements CaptureCallback {

    private static int width = 320, height = 240,
            std = V4L4JConstants.STANDARD_WEBCAM, channel = 0;
    private static String device = "/dev/video0";
    private VideoDevice videoDevice;
    private FrameGrabber frameGrabber;
    private ImageScanner imageScanner;
    private JFrame frame;
    private JLabel lblView;
    private boolean isAimerVisible = false, isDebug = false;

    public WebcamCapture(ImageScanner imageScanner, String scannerName, Path pathToSystemXML) {
        this.imageScanner = imageScanner;

        String val = XPathUtil.getNodeValue("/root/barcode_scanner[@name='" + scannerName + "']",
                pathToSystemXML.toString(), "width");
        width = Integer.parseInt(val);

        val = XPathUtil.getNodeValue("/root/barcode_scanner[@name='" + scannerName + "']",
                pathToSystemXML.toString(), "height");
        height = Integer.parseInt(val);

        val = XPathUtil.getNodeValue("/root/barcode_scanner[@name='" + scannerName + "']",
                pathToSystemXML.toString(), "device");
        device = val;

        val = XPathUtil.getNodeValue("/root/barcode_scanner[@name='" + scannerName + "']",
                pathToSystemXML.toString(), "aimer");
        if (val.equals("true")) {
            isAimerVisible = true;
        }

        val = XPathUtil.getNodeValue("/root/barcode_scanner[@name='" + scannerName + "']",
                pathToSystemXML.toString(), "debug");
        if (val.equals("true")) {
            isDebug = true;
        }

        try {
            init();
        } catch (V4L4JException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }

        frame = new JFrame();
        lblView = new JLabel();
        frame.getRootPane().getContentPane().add(lblView);
        frame.setSize(width, height);
        frame.setAlwaysOnTop(true);
    }

    private void init() throws V4L4JException {
    }

    public void start() throws V4L4JException {
        videoDevice = new VideoDevice(device);
        frameGrabber = videoDevice.getJPEGFrameGrabber(width, height, channel,
                std, V4L4JConstants.MAX_JPEG_QUALITY);
        frameGrabber.setFrameInterval(1, 30);
        frameGrabber.setCaptureCallback(this);

        if (isAimerVisible) {
            frame.setVisible(true);
        }

        frameGrabber.startCapture();
        System.out.println("Starting capture at " + width + "x" + height);
    }

    public void stop() throws V4L4JException {
        frameGrabber.stopCapture();
        videoDevice.releaseFrameGrabber();
        frame.setVisible(false);
        System.out.println("Stop capture at " + width + "x" + height);
    }

    @Override
    public void nextFrame(VideoFrame frame) {
        if (isAimerVisible) {
            BufferedImage bi = frame.getBufferedImage();
            Graphics2D g2d = bi.createGraphics();

            Stroke stroke = new BasicStroke(1.5f);
            g2d.setStroke(stroke);
            g2d.setColor(Color.red);

            g2d.drawLine(0, height / 2, width, height / 2);
            g2d.drawLine(width / 2, 0, width / 2, height);
            g2d.drawRect(
                    (int) ((double) width * .1d),
                    (int) ((double) height * .1d),
                    width - (int) ((double) width * .1d) * 2,
                    height - (int) ((double) height * .1d) * 2);

            g2d.dispose();

            lblView.setIcon(new ImageIcon(bi));
        }
        //System.out.println(imageScanner.recognize(frame.getBufferedImage()));
        try {
            imageScanner.recognize(frame.getBufferedImage());
            
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        
        frame.recycle();
    }

    @Override
    public void exceptionReceived(V4L4JException vlje) {
        Logger.getGlobal().log(Level.WARNING, null, vlje);
    }
}
