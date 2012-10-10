/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.techcard.ui;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.json.JSONArray;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.uui.component.*;
import org.uui.db.DataBase;
import org.uui.event.NavigatorChangeListener;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.table.*;
import org.uui.detaillist.*;
import org.uui.webkit.WebKitUtil;
import ru.sibek.techcard.db.*;



/**
 *
 * @author toor
 */
public class NewClass extends Component{
    private PasswordTextField txtPassword;
    
    public NewClass (String sessionId){
        super(sessionId);
     txtPassword = new PasswordTextField(getSession());
        txtPassword.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                System.out.println("txtPassword >>> " + txtPassword.getPassword());
            }
        });
       
}
    
 @Override
    public void fireEvent(String json) {
       
            
                JSMediator.exec(getSession(),
                        // WebKitFrame.getInstance().browserExecutor(
                        "getUICore().LoadPanel('"
                        + WebKitUtil.prepareToJS("<h4>Выберите форму:</h4>" + txtPassword.getModel())
                        + "');");   
}}
