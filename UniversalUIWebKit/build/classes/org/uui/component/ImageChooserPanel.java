/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import org.uui.event.EventListenerList;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class ImageChooserPanel extends Component {

    private Path p;
    private String[] imageFolders;
    private String model = "", title = "";
    private int width = 600, height = 400;
    private EventListenerList listenerList = new EventListenerList();

    // ~/.saas /app/ui/ img/icons
    public ImageChooserPanel(String sessionId, String basePath, String... imageFolders) {
        super(sessionId);
        this.imageFolders = imageFolders;
        String s[] = new String[imageFolders.length + 2];
        s[0] = "app";
        s[1] = "ui";
        System.arraycopy(imageFolders, 0, s, 2, imageFolders.length);
        p = Paths.get(basePath, s);
    }

    public void addUIEventListener(UIEventListener listener) {
        listenerList.add(UIEventListener.class, listener);
    }

    private void fireExplorerEvent(UIEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == UIEventListener.class) {
                ((UIEventListener) listeners[i + 1]).event(evt);
            }
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public String getModel() {
        model = "<div id='imageChooser' title='" + getTitle() + "' "
                + "style='overflow:auto;' "
                + "panelWidth='" + getWidth() + "' "
                + "panelHeight='" + getHeight() + "'>";

        try {
            Files.walkFileTree(p, new FileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String image = "";
                    for (String s : imageFolders) {
                        image += s + "/";
                    }
                    image += file.getName(file.getNameCount() - 1);
                    ImageIcon imageIcon = new ImageIcon(image);
                    imageIcon.setStyle("cursor:pointer;");
                    imageIcon.addUIEventListener(new UIEventListener() {

                        @Override
                        public void event(UIEvent evt) {
                            fireExplorerEvent(evt);
                        }
                    });
                    model += "<div style='float:left; margin:3px;'>" + imageIcon.getModel() + "</div>";

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });

            model += "</div>";
        } catch (Exception e) {
        }

        return model;
    }
}
