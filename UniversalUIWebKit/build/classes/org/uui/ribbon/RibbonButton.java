/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.ribbon;

import java.util.ArrayList;
import org.uui.component.Component;
import org.uui.event.EventListenerList;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class RibbonButton extends Component {

    private boolean isSelected = false, isFlash = false;
    private ArrayList<RibbonButton> subButtonList;
    private String panelDescription, text, imageFileName = "", actionName = "",
            jsEval = "", htmlModel = "", identificator = "";
    private byte[] hashIcon;
    private int position;
    private EventListenerList listenerList = new EventListenerList();

    public RibbonButton(String sessionId, String imageFileName, String text) {
        super(sessionId);
        this.imageFileName = imageFileName;
        this.text = text;
    }

    public RibbonButton(String sessionId, String imageFileName, String text, String actionName) {
        super(sessionId);
        this.imageFileName = imageFileName;
        this.text = text;
        this.actionName = actionName;
    }

    public RibbonButton(String sessionId, String imageFileName, String text, String actionName, String jsEval) {
        super(sessionId);
        this.imageFileName = imageFileName;
        this.text = text;
        this.actionName = actionName;
        this.jsEval = jsEval;
    }

    public static RibbonButton createDefaultRibbonButton(String sessionId, String imageFileName,
            String text, String actionName) {
        RibbonButton ribbonButton = new RibbonButton(sessionId, imageFileName, text, actionName);
        String model = "<div class='subOperationButton' identificator='"
                + ribbonButton.getIdentificator() + "'>"
                //+ "<img src='" + ribbonButton.getImageFileName() + "'><br/>"
                + "<img src='" + ribbonButton.getImageFileName() + "' "
                + "style='vertical-align:middle;'>&nbsp;"
                + ribbonButton.getText() + "</div>";
        ribbonButton.setModel(model);

        return ribbonButton;
    }

    public static RibbonButton createDefaultRibbonButton(String sessionId, String imageFileName,
            String text, String actionName, String actionMethod) {
        RibbonButton ribbonButton = new RibbonButton(sessionId, imageFileName, text, actionName,
                actionMethod);
        String model = "<div class='subOperationButton' identificator='"
                + ribbonButton.getIdentificator() + "' "
                + "action='" + actionMethod + "'>"
                //+ "<img src='" + ribbonButton.getImageFileName() + "'><br/>"
                + "<img src='" + ribbonButton.getImageFileName() + "' "
                + "style='vertical-align:middle;'>&nbsp;"
                + ribbonButton.getText() + "</div>";
        ribbonButton.setModel(model);

        return ribbonButton;
    }

    @Override
    public void fireEvent(String json) {
        fireRibbonEvent(new RibbonEvent(json));
    }

    @Override
    public void setModel(String html) {
        htmlModel = html;
    }

    @Override
    public String getModel() {
        return htmlModel;
    }

    @Override
    public String getIdentificator() {
        String s = "" + identificator;
        if (s.equals("") || s.equals("null")) {
            identificator = "" + hashCode();
        }

        return identificator;
    }

    @Override
    public void setIdentificator(String identificator) {
        this.identificator = identificator;
    }

    public void addRibbonButtonEventListener(RibbonButtonEventListener listener) {
        listenerList.add(RibbonButtonEventListener.class, listener);
    }

    public void fireRibbonEvent(RibbonEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == RibbonButtonEventListener.class) {
                ((RibbonButtonEventListener) listeners[i + 1]).event(evt);
            }
        }
    }

    public String getJsEval() {
        return jsEval;
    }

    public void setJsEval(String jsEval) {
        this.jsEval = jsEval;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public byte[] getHashIcon() {
        return hashIcon;
    }

    public String getPanelDescription() {
        return panelDescription;
    }

    public void setPanelDescription(String panelDescription) {
        this.panelDescription = panelDescription;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public RibbonButton getMe() {
        return this;
    }

    public void addSubRibbonButton(RibbonButton rb) {
        subButtonList.add(rb);
    }

    public ArrayList<RibbonButton> getSubRibbonButtons() {
        return subButtonList;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return text;
    }
}
