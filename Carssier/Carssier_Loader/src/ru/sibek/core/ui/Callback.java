/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.core.ui;

import org.uui.component.Component;
import ru.sibek.business.ui.JSMediator;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public abstract class Callback extends Component {

    public Callback(String session) {
        super(session);
    }

    public void request(String jsEval) {
        JSMediator.callback(getSession(), getIdentificator(), jsEval);
    }

    public abstract void callback(String json);

    @Override
    public void fireEvent(String json) {
        callback(json);
    }

    @Override
    public void setSession(String session) {
        super.setSession(session);
    }
    
    
}
