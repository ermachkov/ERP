/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import java.util.LinkedHashMap;
import org.uui.event.EventListenerList;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class TableRowNavigator extends Component {

    private Button btnFastBackward, btnBackward, btnForward, btnFastForward;
    private ComboBox cboRows;
    private TextField txtPage;
    private EventListenerList listenerList = new EventListenerList();
    public static final int FAST_BACKWARD = 0, BACKWARD = 1, FORWARD = 2, FAST_FORWARD = 3;
    private int tableRows = 0, pageCount = 0, currentPage = 0, startIndex = 0, endIndex = 0;

    public TableRowNavigator(String sessionId) {
        super(sessionId);
        btnFastBackward = new Button(getSession(), "<<");
        btnFastBackward.setStyle("font-weight:bold;");
        btnFastBackward.setEnabled(false);
        btnFastBackward.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                currentPage = 1;
                endIndex = currentPage * Integer.parseInt(cboRows.getSelectedValue());
                currentPage--;
                startIndex = currentPage * Integer.parseInt(cboRows.getSelectedValue());
                boolean enabled = currentPage == 0 ? false : true;
                btnBackward.setEnabled(enabled);
                btnFastBackward.setEnabled(enabled);
                btnForward.setEnabled(true);
                btnFastForward.setEnabled(true);
                
                txtPage.setText(currentPage + "/" + pageCount);

                UIEvent e = new UIEvent("{eventType:click, action:forward, "
                        + "startIndex:" + startIndex + ", endIndex:" + endIndex + "}");

                fireEvent(e);
            }
        });

        btnBackward = new Button(getSession(), "<");
        btnBackward.setStyle("font-weight:bold;");
        btnBackward.setEnabled(false);
        btnBackward.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                endIndex = currentPage * Integer.parseInt(cboRows.getSelectedValue());
                currentPage--;
                startIndex = currentPage * Integer.parseInt(cboRows.getSelectedValue());
                boolean enabled = currentPage == 0 ? false : true;
                btnBackward.setEnabled(enabled);
                btnFastBackward.setEnabled(enabled);
                btnForward.setEnabled(true);
                btnFastForward.setEnabled(true);
                
                txtPage.setText(currentPage + "/" + pageCount);

                UIEvent e = new UIEvent("{eventType:click, action:forward, "
                        + "startIndex:" + startIndex + ", endIndex:" + endIndex + "}");
                fireEvent(e);
            }
        });

        btnForward = new Button(getSession(), ">");
        btnForward.setStyle("font-weight:bold;");
        btnForward.setEnabled(false);
        btnForward.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                startIndex = currentPage * Integer.parseInt(cboRows.getSelectedValue());
                currentPage++;
                endIndex = currentPage * Integer.parseInt(cboRows.getSelectedValue());

                boolean enabled = currentPage == pageCount ? false : true;
                btnForward.setEnabled(enabled);
                btnFastForward.setEnabled(enabled);

                btnBackward.setEnabled(true);
                btnFastBackward.setEnabled(true);
                
                txtPage.setText(currentPage + "/" + pageCount);

                UIEvent e = new UIEvent("{eventType:click, action:forward, "
                        + "startIndex:" + startIndex + ", endIndex:" + endIndex + "}");
                fireEvent(e);
            }
        });

        btnFastForward = new Button(getSession(), ">>");
        btnFastForward.setStyle("font-weight:bold;");
        btnFastForward.setEnabled(false);
        btnFastForward.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                currentPage = pageCount - 1;
                startIndex = currentPage * Integer.parseInt(cboRows.getSelectedValue());
                currentPage++;
                endIndex = currentPage * Integer.parseInt(cboRows.getSelectedValue());

                boolean enabled = currentPage == pageCount ? false : true;

                btnForward.setEnabled(enabled);
                btnFastForward.setEnabled(enabled);

                btnBackward.setEnabled(true);
                btnFastBackward.setEnabled(true);

                txtPage.setText(currentPage + "/" + pageCount);

                UIEvent e = new UIEvent("{eventType:click, action:forward, "
                        + "startIndex:" + startIndex + ", endIndex:" + endIndex + "}");
                fireEvent(e);
            }
        });

        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("50", "50");
        map.put("100", "100");
        map.put("150", "150");
        map.put("300", "300");
        cboRows = new ComboBox(getSession(), map);
        cboRows.setSelectedValue("100");

        txtPage = new TextField(getSession());
        txtPage.setText("0");
        txtPage.setStyle("text-align:center; font-weight:bold; width:40px;");
        txtPage.setEnabled(false);
    }

    public int getEndIndex() {
        return endIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setTableRows(int rows) {
        if (rows > 0) {
            tableRows = rows;
            pageCount = tableRows / Integer.parseInt(cboRows.getSelectedValue());
            int m = tableRows % Integer.parseInt(cboRows.getSelectedValue());
            pageCount = m > 0 ? pageCount = pageCount + 1 : pageCount;
            currentPage = 0;
            btnForward.setEnabled(true);
            btnFastForward.setEnabled(true);
            txtPage.setText(currentPage + "/" + pageCount);
        }
    }

    public void addUIEventListener(UIEventListener listener) {
        listenerList.add(UIEventListener.class, listener);
    }

    private void fireEvent(UIEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == UIEventListener.class) {
                ((UIEventListener) listeners[i + 1]).event(evt);
            }
        }
    }

    public int getMaxRowsCount() {
        return Integer.parseInt(cboRows.getSelectedValue());
    }

    public void setEnableButton(int buttonType, boolean isEnable) {
        if (buttonType < 0) {
            return;
        }

        if (buttonType > 3) {
            return;
        }

        switch (buttonType) {
            case 0:
                btnFastBackward.setEnabled(isEnable);
                break;

            case 1:
                btnBackward.setEnabled(isEnable);
                break;

            case 2:
                btnForward.setEnabled(isEnable);
                break;

            case 3:
                btnFastForward.setEnabled(isEnable);
                break;
        }
    }

    public void setPage(int number) {
        txtPage.setText("" + number);
    }

    @Override
    public String getModel() {
        String model = ""
                + "<span class='tableRowNavigator'>"
                + btnFastBackward.getModel()
                + btnBackward.getModel()
                + txtPage.getModel()
                + cboRows.getModel()
                + btnForward.getModel()
                + btnFastForward.getModel()
                + "</span>";

        return model;
    }
}
