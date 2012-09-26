/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.core.ui;

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
public abstract class ConfirmPanel extends Component {

    private String model = "", title = "Вопрос", message = "";
    private Button btnYes, btnNo;
    public static int YES = 0, NO = 1;

    public ConfirmPanel(String sessionId) {
        super(sessionId);
        btnYes = new Button(getSession(), "Да");
        btnYes.setCssClass("btnConfirm");
        
        btnYes.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                pressed(ConfirmPanel.YES);
            }
        });

        btnNo = new Button(getSession(), "Нет");
        btnNo.setCssClass("btnConfirm");
        btnNo.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                pressed(ConfirmPanel.NO);
            }
        });
    }
    
    public abstract void pressed(int button);

    public void showPanel() {
        showPanel("getUICore().showConfirmPanel");
    }

    public void showPanel(String jsShowMessageFunctionName) {
        String cmd = jsShowMessageFunctionName + "("
                + "'" + WebKitUtil.prepareToJS(
                "<span style='font-size:80%'>" + getTitle() + "</span>") + "', "
                + "'" + WebKitUtil.prepareToJS(getModel()) + "')";
        JSMediator.exec(getSession(), cmd);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
            String _model = "<div class='confirmPanel' style='font-size:80%;'>"
                    + "<table width='100%' height='100%'>"
                    + "<tr><td height='95%' valign='middle'>"
                    + message
                    + "</td></tr>"
                    + "<tr><td align='right' valign='bottom'>"
                    + btnYes.getModel() + btnNo.getModel()
                    + "</td></tr>"
                    + "</table>"
                    + "</div>";
            return _model;
        }
    }
}
