/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.business.ui;

//import org.http.server.WebSocketBundle;
import org.uui.webkit.WebKitUtil;
import org.uws.server.WebSocketBundle;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class JSMediator {

    public static void refreshElement(String sessionId, String elementId, String content) {
        if (sessionId == null || elementId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().refreshElement("
                + "'" + elementId + "', "
                + "'" + WebKitUtil.prepareToJS(content) + "')");
    }

    public static void callback(String sessionId, String identificator, String jsEval) {
        if (sessionId == null || identificator == null || jsEval == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "callback("
                + "'" + identificator + "', "
                + "'" + WebKitUtil.prepareToJS(jsEval) + "')");
    }

    public static void requestAllSelectedNodes(String sessionId, String elementId, String identificator) {
        if (sessionId == null || elementId == null || identificator == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().requestAllSelectedNodes("
                + "'" + elementId + "', "
                + "'" + identificator + "')");
    }

    public static void refreshVideoFrame(String sessionId, String pictureName, String videoFrameId) {
        if (sessionId == null || pictureName == null || videoFrameId == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().refreshVideoFrame("
                + "'" + pictureName + "', "
                + "'" + videoFrameId + "')");
    }

    public static void selectedTreeLeaf(String sessionId, String elementId, long id) {
        if (sessionId == null || elementId == null || id < 0) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().selectedTreeLeaf("
                + "'" + elementId + "', "
                + "'" + id + "')");
    }

    public static void setAllTableCheckBoxSelected(String sessionId,
            String tableId, boolean isChecked) {
        if (sessionId == null || tableId == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().setAllTableCheckBoxSelected("
                + "'" + tableId + "', "
                + "" + isChecked + ")");
    }

    public static void showSplashPanel(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().showSplashPanel('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void setLockPanelProgressBar(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().setLockPanelProgressBar('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void setSplashProgressBar(String sessionId, Object value) {
        if (sessionId == null || value == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().setSplashProgressBar(" + value + ");");
    }

    public static void initLogin(String sessionId, String identificator) {
        if (sessionId == null) {
            return;
        }
        
        System.out.println("initLogin sessionId = " + sessionId);
        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().initLogin('" + identificator + "');");
    }

    public static void showLoginPanel(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().showLoginPanel('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void setLoggerInfo(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().setLoggerInfo('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void setSliderPanel(String sessionId, String idComponent, String content, String direction) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().setSliderPanel("
                + "'" + idComponent + "', "
                + "'" + WebKitUtil.prepareToJS(content) + "', "
                + "'" + direction + "');");
    }

    public static void showFilterPanel(String sessionId, String content, String identificator) {
        if (sessionId == null || content == null || identificator == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().showFilterPanel('"
                + WebKitUtil.prepareToJS(content)
                + "', "
                + "'" + identificator + "');");
    }

    ;
    
    public static void showRemoveOrderPanel(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().showRemoveOrderPanel('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void alert(String sessionId, String message) {
        if (sessionId == null || message == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "alert('" + WebKitUtil.prepareToJS(message) + "');");
    }

    public static void setTopButtonsModel(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().setTopButtonsModel('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void setOperationButton(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().setOperationButton('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void setRightPanel(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().setRightPanel('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void switchModule(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().switchModule('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void showEditor(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().showEditor('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void setImageBrowser(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().setImageBrowser('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void setWorkPanelSelectable(String sessionId, boolean selectable) {
        if (sessionId == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().setWorkPanelSelectable(" + selectable + ");");
    }

    public static void setEditorContent(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().setEditorContent('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void setWorkPanel(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().setWorkPanel('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void setViewSwitcher(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().setViewSwitcher('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void setSubOperationButton(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().setSubOperationButton('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void setNavigatorPanel(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().setNavigatorPanel('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void setSelectTab(String sessionId, int tabIndex) {
        if (sessionId == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().setSelectTab(" + tabIndex + ");");
    }

    public static void setCashBox(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().setCashBox('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void print(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().print('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void setOrderDescription(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().setOrderDescription('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void refreshSmartChooserList(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().refreshSmartChooserList('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void setCustomersSelector(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().setCustomersSelector('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void setRightPanel(String sessionId, String content, double pos) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().setRightPanel('"
                + WebKitUtil.prepareToJS(content)
                + "', " + pos + ");");
    }

    public static void showSmartChooserEditor(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().showSmartChooserEditor('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void showImageChooser(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().showImageChooser('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void setIndicator(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().setIndicator('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void showProgressBar(String sessionId, int value) {
        if (sessionId == null || value < 0) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().showProgressBar(" + value + ");");
    }

    public static void dbDumpUpload(String sessionId) {
        if (sessionId == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().dbDumpUpload();");
    }

    public static void showStatusInfo(String sessionId, int number, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().showStatusInfo(" + number + ", '"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void setContextMenu(String sessionId, String content) {
        if (sessionId == null || content == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().setContextMenu('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    public static void setExplorerEditableMode(String sessionId, boolean isEditable) {
        if (sessionId == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId,
                "getUICore().setExplorerEditableMode(" + isEditable + ");");
    }

    public static void restoreBody(String sessionId) {
        if (sessionId == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId, "getUICore().restoreBody();");
    }

    public static void hideRightPanel(String sessionId) {
        if (sessionId == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId, "getUICore().hideRightPanel();");
    }

    public static void showLockPanel(String sessionId) {
        if (sessionId == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId, "getUICore().showLockPanel();");
    }

    public static void hideLockPanel(String sessionId) {
        if (sessionId == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId, "getUICore().hideLockPanel();");
    }

    public static void hideNavigationPanel(String sessionId) {
        if (sessionId == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId, "getUICore().hideNavigationPanel();");
    }

    public static void showLoggerPanel(String sessionId) {
        if (sessionId == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId, "getUICore().showLoggerPanel();");
    }

    public static void hideLoggerPanel(String sessionId) {
        if (sessionId == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId, "getUICore().hideLoggerPanel();");
    }

    public static void hidePopupPanel(String sessionId) {
        if (sessionId == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId, "getUICore().hidePopupPanel();");
    }

    public static void exec(String sessionId, String jsEval) {
        if (sessionId == null && jsEval == null) {
            return;
        }

        WebSocketBundle.getInstance().send(sessionId, jsEval);
    }
//    public static void invokeJS(String sessionId, String method, String content) {
//        if (sessionId == null || content == null) {
//            return;
//        }
//
//        WebSocketBundle.getInstance().send(sessionId,
//                "getUICore()." + method + "('"
//                + WebKitUtil.prepareToJS(content)
//                + "');");
//    }
}
