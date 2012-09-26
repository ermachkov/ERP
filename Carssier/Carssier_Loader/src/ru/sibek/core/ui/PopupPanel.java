/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.core.ui;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import org.uui.component.Button;
import org.uui.component.Component;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.webkit.WebKitUtil;
import ru.sibek.business.ui.JSMediator;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class PopupPanel extends Component {

    private String model = "", title = "Вопрос", panelModel = "";
    private Button btnYes;
    private AtomicBoolean isButtonYes = new AtomicBoolean(false);
    private int width = -1, height = -1;

    public PopupPanel(String sessionId) {
        super(sessionId);
        btnYes = new Button(getSession(), "Ok");
        btnYes.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                isButtonYes.set(true);
            }
        });
    }

    public void showPanel() {
        width = -1;
        height = -1;
        //showPanel("getUICore().showPopupPanel");
    }

    /**
     *
     * @param autoHideTimeout in ms
     */
    public void setAutoHideTimeout(int autoHideTimeout) {
        Timer timer = new Timer();
        Date date = new Date();
        long time = date.getTime() + autoHideTimeout;
        date.setTime(time);
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                PopupPanel.this.hide();
            }
            
        }, date);
    }

    public void showPanel(int width, int height) {
        this.width = width;
        this.height = height;
       // showPanel("getUICore().showPopupPanel");
    }

    public void showPanel(String jsShowMessageFunctionName) {
        String cmd = jsShowMessageFunctionName + "("
                + "'" + WebKitUtil.prepareToJS(
                "<span style='font-size:80%'>" + getTitle() + "</span>") + "', "
                + "'" + WebKitUtil.prepareToJS(getModel()) + "', " + width + ", " + height + ")";

        JSMediator.exec(getSession(), cmd);
    }

    public void hide() {
        JSMediator.hidePopupPanel(getSession());
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPanel(String panelModel) {
        this.panelModel = panelModel;
    }

    @Override
    public String getModel() {
        boolean isDefined = true;
        if (model == null) {
            isDefined = false;

        } else if (model.equals("")) {
            isDefined = false;
        }

        if (isDefined) {
            return model;

        } else {
            String sHeight = "";
            String contentHeight = "";
            if (height > 0) {
                sHeight = "height='" + height + "'";
                contentHeight = "height:" + (height - 40) + "px;";
            }

            String _model = "<div class='popupPanel' style='font-size:80%;'>"
                    + "<table width='100%' " + sHeight + ">"
                    + "<tr><td valign='middle'>"
                    + "<div style='width:100%;" + contentHeight + "; overflow:auto;'>"
                    + panelModel
                    + "</div>"
                    + "</td></tr>"
                    + "<tr><td align='right' valign='bottom' style='height:40px;'>"
                    + btnYes.getModel()
                    + "</td></tr>"
                    + "</table>"
                    + "</div>";
            return _model;
        }
    }
}
