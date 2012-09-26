package org.uui.explorer;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public interface Explorer {

    public void moveUp();

    public void goHome();

    public void moveDown();

    public void keyboardMoveDown();
    
    public String refreshAndGetHTMLModel();

}
