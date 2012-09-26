/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unet.rpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class MediatorCollector {

    private CopyOnWriteArrayList<Mediator> mediatorList;
    private static MediatorCollector self = null;

    private MediatorCollector() {
        mediatorList = new CopyOnWriteArrayList<>();
    }

    public static synchronized MediatorCollector getInstance() {
        if (self == null) {
            self = new MediatorCollector();
        }

        return self;
    }

    public void addMediator(Mediator mediator) {
        execute(mediator);
    }

    private void execute(Mediator mediator) {
        String className = mediator.getClassName();
        try {
            Class cls = Class.forName(className);
            Object object = Class.forName(className).newInstance();
            for (Method method : cls.getMethods()) {
                if (method.getName().equals(mediator.getMethodName())
                        && method.getParameterTypes().length == mediator.getArguments().length) {

                    boolean isFind = true;
                    int index = 0;
                    for (Class clsParam : method.getParameterTypes()) {
                        if (!clsParam.isInstance(mediator.getArguments()[index])) {
                            isFind = false;
                        }
                        index++;
                    }

                    if (isFind) {
                        Object result = method.invoke(object, mediator.getArguments());
                        mediator.setResult(result);
                        mediator.setStatus(Mediator.INVOKED);
                        mediatorList.add(mediator);
                        break;
                    }
                }
            }

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getGlobal().log(Level.SEVERE, mediator.toString(), ex);
        }
    }

    public Mediator getResult(String mediatorMark) {
        Mediator mediatorResult = null;
        int index = -1, count = 0;
        for (Mediator bundle : mediatorList) {
            if (bundle.getMark().equals(mediatorMark) && bundle.getStatus() == Mediator.INVOKED) {
                mediatorResult = bundle;
                index = count;
                break;
            }
            count++;
        }

        if (index > 0) {
            mediatorList.remove(index);
        }

        return mediatorResult;
    }
}
