package org.uui.explorer;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import javax.imageio.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileImageChooser extends JPanel implements PropertyChangeListener {

    private JFileChooser jfc;
    private Image img;

    public FileImageChooser(JFileChooser jfc) {
        this.jfc = jfc;
        Dimension sz = new Dimension(200, 200);
        setPreferredSize(sz);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        try {
            File file = jfc.getSelectedFile();
            updateImage(file);
        } catch (IOException ex) {
            Logger.getLogger(FileImageChooser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateImage(File file) throws IOException {
        if (file == null) {
            return;
        }

        img = ImageIO.read(file);
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        // fill the background
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON));
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if (img != null) {
            // calculate the scaling factor
            int w = img.getWidth(null);
            int h = img.getHeight(null);
            int side = Math.max(w, h);
            double scale = 200.0 / (double) side;
            w = (int) (scale * (double) w);
            h = (int) (scale * (double) h);

            // draw the image
            g.drawImage(img, 0, 0, w, h, null);

            // draw the image dimensions
            //String dim = w + " x " + h;
            //g2d.setColor(Color.black);
            //g2d.drawString(dim, 31, 196);
            //g2d.setColor(Color.white);
            //g2d.drawString(dim, 30, 195);

        } else {

            // print a message
            //g2d.setColor(Color.black);
            //g2d.drawString("Not an image", 30, 100);
        }
    }
}
