/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.video;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.uui.component.CheckBox;
import org.uui.component.WorkPanel;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.ui.MainFrame;
import ru.sibek.core.ui.Callback;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class VideoPanel extends WorkPanel {
    
    private CheckBox chkEnable;
    private boolean isShowing = false;
    private Timer timer;
    private Callback callback;
    
    public VideoPanel(String sessionId) {
        super(sessionId);
        chkEnable = new CheckBox(getSession(), "Включить / Выключить");
        chkEnable.setChecked(false);
        chkEnable.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                if (chkEnable.isChecked()) {
                    isShowing = true;
                    timer = new Timer();
                    timer.scheduleAtFixedRate(new CheckVideoPanel(), 0, 250);
                    
                } else {
                    timer.cancel();
                }
            }
        });
        chkEnable.setStyleLabel("font-size:80%;");
    }
    
    final class CheckVideoPanel extends TimerTask {
        
        @Override
        public void run() {
            callback = new Callback(VideoPanel.this.getSession()) {
                @Override
                public void callback(String json) {
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        if(jsonObject.has("value")){
                            if(jsonObject.getString("value").equals("Видео")){
                                JSMediator.refreshVideoFrame(
                                        VideoPanel.this.getSession(), 
                                        "frame.jpg", 
                                        "cam_0");
                            }
                        }
                    } catch (JSONException ex) {
                        Logger.getLogger(VideoPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            
            callback.request("getUICore().getSelectedWorkPanel()");
        }
    }
    
    @Override
    public String getModel() {
        String model = ""
                + "<div>"
                + chkEnable.getModel()
                + "</div>"
                + "<div id='frames'>"
                + "<div id='camera_0'>"
                + "<img src='img/stream/last/frame.jpg' id='cam_0' />"
                + "</div>"
                + "</div>";
        
        return model;
    }
    
    @Override
    public void fireEvent(String json) {
        if (json == null) {
            return;
        }
        if (json.equals("")) {
            return;
        }
        if (json.equals("{}")) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            setSession(jsonObject.getString("session"));
            
            if (jsonObject.getString("eventType").equals("click")) {
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "Видео");
                JSMediator.setWorkPanel(getSession(), getModel());
                JSMediator.hideNavigationPanel(getSession());
            }
            
        } catch (JSONException ex) {
            Logger.getGlobal().log(Level.WARNING, json, ex);
        }
    }
}
