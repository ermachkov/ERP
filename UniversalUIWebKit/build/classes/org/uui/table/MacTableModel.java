/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com Omsk, Russia, created
 * 16.02.2011 (C) Copyright by Zubanov Dmitry
 */
package org.uui.table;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.json.Validator;
import org.ubo.utils.StringToNumber;
import org.uui.component.*;
import org.uui.event.EventListenerList;
import org.uui.event.NavigatorChangeListener;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

public class MacTableModel extends Component {

    private ArrayList<String> headers = new ArrayList();
    private ArrayList<Class> columnClasses = new ArrayList();
    private ArrayList<Boolean> columnSortable = new ArrayList();
    private boolean[] columnVisible;
    private ArrayList<MacTableRow> rows = new ArrayList();
    private boolean isNumeratorEnable, isWasSorted, canDeleteRow = false,
            isNavigatorEnable = true, isRowCheckedButtonEnable = false,
            isNavigatorShowingAlways = false, isEditButtonEnable = false,
            isRemoveButtonEnable = true;
    private int sortIndex;
    private MacTableSummator summator;
    private ArrayList<MacTableRow> selectedRows;
    private MacTableHeaderModel headerModel;
    private Set<Integer> checkedRows = new HashSet<>();
    public static int MODE_VIEW = 1, MODE_EDIT = 2, MODE_CELLEDIT = 3;
    private int mode = MODE_VIEW, selectedRow = -1;
    private String identificator = "", cssClass = "", id = "";
    private Button btnRemoveButton, btnEditButton, btnAllRowChecked, btnAllRowUncheked;
    private EventListenerList listenerList = new EventListenerList();
    private MacTableNavigator tableNavigator;

    public MacTableModel(String sessionId, boolean isNumeratorEnable, MacTableSummator summator) {
        super(sessionId);
        this.isNumeratorEnable = isNumeratorEnable;
        this.summator = summator;
        selectedRows = new ArrayList<>();

        btnRemoveButton = new Button(sessionId, "Удалить");
        btnRemoveButton.setCssClass("macTableRemoveButton");

        btnEditButton = new Button(sessionId, "Редактировать");
        btnEditButton.setCssClass("macTableEditButton");

        btnAllRowChecked = new Button(sessionId, "Выделить все");
        btnAllRowChecked.setCssClass("macTableAllRowCheckedButton");

        btnAllRowUncheked = new Button(sessionId, "Снять выделение");
        btnAllRowUncheked.setCssClass("macTableAllRowUncheckedButton");

        tableNavigator = new MacTableNavigator(sessionId) {

            @Override
            public void change(int event) {
                fireNavigatorEvent(event);
            }
        };
    }

    public MacTableModel(String sessionId, boolean isNumeratorEnable) {
        this(sessionId, true, null);
    }

    public MacTableModel(String sessionId) {
        this(sessionId, false, null);
    }

    public boolean isRemoveButtonEnable() {
        return isRemoveButtonEnable;
    }

    public void setRemoveButtonEnable(boolean isRemoveButtonEnable) {
        this.isRemoveButtonEnable = isRemoveButtonEnable;
    }

    public void setEditButonEnabled(boolean isEnabled) {
        isEditButtonEnable = isEnabled;
    }

    public void setRowCheckedButonEnabled(boolean isEnabled) {
        isRowCheckedButtonEnable = isEnabled;
    }

    public boolean isNavigatorShowingAlways() {
        return isNavigatorShowingAlways;
    }

    public void setNavigatorShowingAlways(boolean isNavigatorShowingAlways) {
        this.isNavigatorShowingAlways = isNavigatorShowingAlways;
    }

    public Button getMacTableRemoveButton() {
        return btnRemoveButton;
    }

    public Button getMacTableEditButton() {
        return btnEditButton;
    }

    public Button getMacTableAllRowCheckedButton() {
        return btnAllRowChecked;
    }

    public Button getMacTableAllRowUncheckedButton() {
        return btnAllRowUncheked;
    }

    public void addNavigatorChangeListener(NavigatorChangeListener listener) {
        listenerList.add(NavigatorChangeListener.class, listener);
    }

    private void fireNavigatorEvent(int event) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == NavigatorChangeListener.class) {
                ((NavigatorChangeListener) listeners[i + 1]).event(event);
            }
        }
    }

    public void addUIEventListener(UIEventListener listener) {
        listenerList.add(UIEventListener.class, listener);
    }

    private void fireMacTableEvent(UIEvent evt) {
        try {
            if (!Validator.isValid(evt.getJSONObject(), "row")) {
                return;
            }

            selectedRow = evt.getJSONObject().getInt("row");
            for (MacTableRow r : getRows()) {
                r.setSelected(false);
            }
            getRow(selectedRow).setSelected(true);

            if (evt.getJSONObject().getString("eventType").equals("stopCellEditing")) {
                int row = evt.getJSONObject().getInt("row");
                int column = evt.getJSONObject().getInt("column");
                String value = evt.getJSONObject().getString("value");

                MacTableRow r = rows.get(row);
                MacTableCell c = r.getCell(column);
                Object o = c.getValue();
                if (isNumberInstance(o.getClass())) {
                    setCellNumberValue(c, o, StringToNumber.formatToMoney(value));
                    computeSum();
                } else {
                    c.setValue(value);
                }
            }

            if (evt.getJSONObject().getString("eventType").equals("keypress")) {
                int row = evt.getJSONObject().getInt("row");
                int column = evt.getJSONObject().getInt("column");
                String value = evt.getJSONObject().getString("value");

                MacTableRow r = rows.get(row);
                MacTableCell c = r.getCell(column);
                Object o = c.getValue();
                if (isNumberInstance(o.getClass())) {
                    setCellNumberValue(c, o, StringToNumber.formatToMoney(value));
                    computeSum();

                } else {
                    c.setValue(value);
                }
            }

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, evt.toString(), e);
        }

        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == UIEventListener.class) {
                ((UIEventListener) listeners[i + 1]).event(evt);
            }
        }
    }

    private void setCellNumberValue(MacTableCell cell, Object oldValue, BigDecimal newValue) {
        if (oldValue.getClass().getName().toLowerCase().indexOf("byte") != -1) {
            cell.setValue(newValue.byteValue());
        }

        if (oldValue.getClass().getName().toLowerCase().indexOf("short") != -1) {
            cell.setValue(newValue.shortValue());
        }

        if (oldValue.getClass().getName().toLowerCase().indexOf("int") != -1) {
            cell.setValue(newValue.intValue());
        }

        if (oldValue.getClass().getName().toLowerCase().indexOf("double") != -1) {
            cell.setValue(newValue.doubleValue());
        }

        if (oldValue.getClass().getName().toLowerCase().indexOf("float") != -1) {
            cell.setValue(newValue.floatValue());
        }
    }

    public ArrayList<MacTableRow> getRows() {
        return rows;
    }

    public MacTableRow getRow(int index) {
        MacTableRow macTableRow = null;
        if (index >= 0 && index < rows.size()) {
            macTableRow = rows.get(index);
        }
        return macTableRow;
    }

    public MacTableSummator getSummator() {
        return summator;
    }

    public boolean isSummatorEnable() {
        boolean result = false;
        if (summator == null) {
            return result;
        }

        if (summator.getSumColumns() != null) {
            result = true;
        }

        return result;
    }

    public void setHeader(MacTableHeaderModel headerModel) {
        this.headerModel = headerModel;
        headers.clear();

       /* if (isNumeratorEnable) {
            columnVisible = new boolean[headerModel.getHeaders().size() + 1];
            headers.add("#");
            columnClasses.add(Numerator.class);
            columnSortable.add(false);
            columnVisible[0] = true;

        } else {*/
            columnVisible = new boolean[headerModel.getHeaders().size()];
       // }

        for (int i = 0; i < columnVisible.length; i++) {
            columnVisible[i] = true;
        }

        int i = 0;
        for (MacHeaderColumn headColumn : headerModel.getHeaders()) {
            headers.add(headColumn.getText());
            columnClasses.add(headColumn.getHeaderClass());
            columnSortable.add(headColumn.isSortable());
            i++;
        }
    }

    public boolean isNumeratorEnable() {
        return isNumeratorEnable;
    }

    public MacTableRow getSummatorRow() {
        for (MacTableRow row : rows) {
            if (row.getRowType() == MacTableRow.SUM) {
                return row;
            }
        }
        return null;
    }

    public void computeSum() {
        if (summator == null) {
            return;
        }

        if (summator.getSumColumns() == null) {
            return;
        }

        MacTableRow deleteObject = null;
        for (MacTableRow deleteRow : rows) {
            if (deleteRow.getRowType() == MacTableRow.SUM) {
                deleteObject = deleteRow;
                break;
            }
        }

        if (deleteObject != null) {
            rows.remove(deleteObject);
        }

        MacTableRow macTableRow = new MacTableRow();
        macTableRow.setRowType(MacTableRow.SUM);
        int index = 0;
        for (Class cls : columnClasses) {
            if (isNumberInstance(cls)) {
//                if (isNumeratorEnable && index == 0) {
//                    macTableRow.addCell(new MacTableCell(0, false));
//                    continue;
//                }

                BigDecimal sum = new BigDecimal("0");
                for (MacTableRow row : rows) {
                    MacTableCell cell;
//                    if (isNumeratorEnable) {
//                        cell = row.getCell(index + 1);
//
//                    } else {
                    cell = row.getCell(index);
//                    }

                    try {
                        sum = sum.add(new BigDecimal("" + cell.getValue()));
                    } catch (Exception e) {
                        System.err.println(e.getMessage() + "\ncell.getValue() " + cell.getValue());
                        Logger.getGlobal().log(Level.SEVERE, "cell.getValue() " + cell.getValue(), e);
                    }

                }

                if (summator.getSumColumnsList().contains(index)) {
                    macTableRow.addCell(new MacTableCell(getSession(), sum.doubleValue(), false));
                } else {
                    macTableRow.addCell(new MacTableCell(getSession(), "", false));
                }


            } else {
                if (cls.getName().indexOf("ImageIcon") != -1) {
                    //macTableRow.addCell(new MacTableCell(new ImageIcon(), false));
                }

                macTableRow.addCell(new MacTableCell(getSession(), "", false));
            }
            index++;
        }

        rows.add(macTableRow);
//        rebuildNumerator();
    }

    public void setColumnVisible(int columnIndex, boolean isVisible) {
        if (isNumeratorEnable) {
            if (columnIndex < 0 || columnIndex > columnVisible.length) {
                return;
            }

        } else {
            if (columnIndex < 0 || columnIndex > columnVisible.length - 1) {
                return;
            }
        }

        columnVisible[columnIndex] = isVisible;
    }

    public boolean isColumnVisible(int columnIndex) {
        if (isNumeratorEnable) {
            if (columnIndex < 0 || columnIndex > columnVisible.length) {
                return false;
            }

        } else {
            if (columnIndex < 0 || columnIndex > columnVisible.length - 1) {
                return false;
            }
        }


        return columnVisible[columnIndex];
    }

    //@Override
    public Class getColumnClass(int c) {
        return columnClasses.get(c);
    }

    //@Override
    public String getColumnName(int c) {
        return headers.get(c);
    }

    public int getColumnByName(String name) {
        int index = -1;

        int i = 0;
        for (String str : headers) {
            if (str.equals(name)) {
                index = i;
                break;
            }
            i++;
        }

        return index;
    }

    public boolean isColumnSortable(int c) {
        return columnSortable.get(c);
    }

    public int getRowCount() {
        return rows.size();
    }

    private boolean isNumberInstance(Class cls) {
        boolean result = false;

        if (cls.getName().indexOf("Number") != -1) {
            result = true;

        } else if (cls.getName().indexOf("Byte") != -1) {
            result = true;

        } else if (cls.getName().indexOf("Short") != -1) {
            result = true;

        } else if (cls.getName().indexOf("Integer") != -1) {
            result = true;

        } else if (cls.getName().indexOf("Long") != -1) {
            result = true;

        } else if (cls.getName().indexOf("Double") != -1) {
            result = true;

        } else if (cls.getName().indexOf("Float") != -1) {
            result = true;

        } else if (cls.getName().indexOf("BigDecimal") != -1) {
            result = true;

        } else if (cls.getName().indexOf("byte") != -1) {
            result = true;

        } else if (cls.getName().indexOf("short") != -1) {
            result = true;

        } else if (cls.getName().indexOf("int") != -1) {
            result = true;

        } else if (cls.getName().indexOf("double") != -1) {
            result = true;

        } else if (cls.getName().indexOf("float") != -1) {
            result = true;
        }

        return result;
    }

    public Comparator getComparator(int i) {
        Comparator comparator = null;

        if (columnClasses.get(i).getName().indexOf("String") != -1) {
            getStringComparator();

        } else if (columnClasses.get(i).getName().indexOf("Date") != -1) {
            getDateComparator();

        } else if (isNumberInstance(columnClasses.get(i))) {
            comparator = getNumberComparator();
        }
        /*
         * } else if (columnClasses.get(i).getName().indexOf("Number") != -1) {
         * comparator = getNumberComparator();
         *
         * } else if (columnClasses.get(i).getName().indexOf("Byte") != -1) {
         * comparator = getNumberComparator();
         *
         * } else if (columnClasses.get(i).getName().indexOf("Short") != -1) {
         * comparator = getNumberComparator();
         *
         * } else if (columnClasses.get(i).getName().indexOf("Integer") != -1) {
         * comparator = getNumberComparator();
         *
         * } else if (columnClasses.get(i).getName().indexOf("Long") != -1) {
         * comparator = getNumberComparator();
         *
         * } else if (columnClasses.get(i).getName().indexOf("Double") != -1) {
         * comparator = getNumberComparator();
         *
         * } else if (columnClasses.get(i).getName().indexOf("Float") != -1) {
         * comparator = getNumberComparator(); }
         *
         */

        return comparator;
    }

    private Comparator getStringComparator() {
        Comparator numberComparator = new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                String so1 = "" + o1;
                String so2 = "" + o2;
                return so1.compareTo(so2);
            }
        };

        return numberComparator;
    }

    private Comparator getDateComparator() {
        Comparator numberComparator = new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                Date d1 = (Date) o1;
                Date d2 = (Date) o2;
                return d1.compareTo(d2);
            }
        };

        return numberComparator;
    }

    private Comparator getNumberComparator() {
        Comparator numberComparator = new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                Number n1 = (Number) o1;
                Number n2 = (Number) o2;
                return ((Double) n1.doubleValue()).compareTo((Double) n2.doubleValue());
            }
        };

        return numberComparator;
    }

    //@Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        MacTableRow row = rows.get(rowIndex);
        MacTableCell cell = row.getCell(columnIndex);
        return cell.isEditable();
    }

    public int getColumnCount() {
        return headers.size();
    }

    public void setWasSorted(boolean isWasSorted) {
        this.isWasSorted = isWasSorted;
        sortIndex = 0;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Object value = "";
        if (rows.size() > rowIndex) {

            MacTableRow row = rows.get(rowIndex);
            if (row.getSize() > columnIndex) {
                MacTableCell cell = row.getCell(columnIndex);

                if (columnIndex == 0 && isNumeratorEnable) {
                    if (isWasSorted) {
                        sortIndex++;
                        cell.setValue(sortIndex);
                        if (sortIndex == rows.size()) {
                            isWasSorted = false;
                        }
                    }
                    value = cell.getValue();

                } else {
                    value = cell.getValue();
                }
            }
        }

        return value;
    }

    //@Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        MacTableRow row = rows.get(rowIndex);
        MacTableCell cell = row.getCell(columnIndex);
        cell.setValue(value);
        //fireTableCellUpdated(rowIndex, columnIndex);
    }

//    public void addRow(MacTableRow row) {
//        if (isNumeratorEnable) {
//            row.addCell(0, new MacTableCell(rows.size() + 1, false));
//            rows.add(row);
//
//        } else {
//            rows.add(row);
//        }
//
//        setWasSorted(true);
//        fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
//        computeSum();
//    }
    public void removeSelectedRows() {
        Iterator<MacTableRow> i = selectedRows.iterator();
        while (i.hasNext()) {
            MacTableRow row = i.next();
            rows.remove(row);
        }
        //fireTableDataChanged();
        rebuildNumerator();
        update();
    }

    private void update() {
        computeSum();
    }

    public void removeRow(int rowIndex) {
        if (rowIndex < 0) {
            return;
        }

        if (rowIndex > rows.size() - 1) {
            return;
        }

        rows.remove(rowIndex);

        //rebuildNumerator();

        //fireTableRowsDeleted(rowIndex, rowIndex);
        computeSum();
    }

    public void removeAllRows() {
        rows.clear();
        computeSum();
//        if (rows.size() > 0) {
//            int lastrowi = rows.size() - 1;
//            rows.clear();
//
//            fireTableRowsDeleted(0, lastrowi);
//            computeSum();
//        }
    }

    public void insertRowAfter(int rowIndex, MacTableRow macTableRow) {
        if (rowIndex < 0) {
            if (isNumeratorEnable) {
                macTableRow.addCell(0, new MacTableCell(getSession(), 0, false));
                rows.add(0, macTableRow);
                rebuildNumerator();

            } else {
                rows.add(0, macTableRow);
            }

            //fireTableRowsInserted(0, 0);
            computeSum();

        } else if (rowIndex > rows.size()) {
            if (isNumeratorEnable) {
                macTableRow.addCell(0, new MacTableCell(getSession(), 0, false));
                rows.add(macTableRow);
                rebuildNumerator();

            } else {
                rows.add(macTableRow);
            }

            //fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
            computeSum();

        } else {
            if (isNumeratorEnable) {
                macTableRow.addCell(0, new MacTableCell(getSession(), 0, false));
                rows.add(rowIndex + 1, macTableRow);
                rebuildNumerator();

            } else {
                rows.add(rowIndex + 1, macTableRow);
            }

            //fireTableRowsInserted(rowIndex + 1, rowIndex + 1);
            computeSum();
        }
    }

    public void rebuildNumerator() {
        if (isNumeratorEnable) {
            int i = 1;
            for (MacTableRow row : rows) {
                MacTableCell cell = row.getCell(0);
                cell.setValue(new Numerator(i));
                i++;
            }
        }

        //fireTableDataChanged();
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getModel() {
        StringBuilder sb = new StringBuilder();
        sb.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" "
                + "class=\"").append(cssClass).append("\" id=\"").append(id).append("\" " + "identificator='").append(getIdentificator()).append("'>");

        if (getMode() == MacTableModel.MODE_EDIT) {
//            String btnEditModel = "";
//            if (isEditButtonEnable) {
//                btnEditModel = btnEditButton.getModel();
//            }

            String sButtons = "";
            if (isRemoveButtonEnable && isEditButtonEnable) {
                sButtons = "<div style='float:left' class='macTableRemovePanel'>"
                        + btnRemoveButton.getModel()
                        + btnEditButton.getModel()
                        + "</div>";
            }

            if (isRemoveButtonEnable && !isEditButtonEnable) {
                sButtons = "<div style='float:left' class='macTableRemovePanel'>"
                        + btnRemoveButton.getModel()
                        + "</div>";
            }

            if (!isRemoveButtonEnable && isEditButtonEnable) {
                sButtons = "<div style='float:left' class='macTableRemovePanel'>"
                        + btnEditButton.getModel()
                        + "</div>";
            }

            if (isRowCheckedButtonEnable) {
                sButtons += "<div style='float:left' class='macTableRowCheckerPanel'>"
                        + btnAllRowChecked.getModel()
                        + btnAllRowUncheked.getModel()
                        + "</div>";
            }

            if (!sButtons.equals("")) {
                sButtons = "<div>" + sButtons + "</div>";
            }

            sb.insert(0, sButtons + sb);

        }

        if (isNavigatorEnable) {
            if (isNavigatorShowingAlways) {
                tableNavigator.setRowsCount(rows.size());
                sb.insert(0, tableNavigator.getModel());

            } else {
                if (rows.size() > tableNavigator.getRowsOnPage()) {
                    tableNavigator.setRowsCount(rows.size());
                    sb.insert(0, tableNavigator.getModel());
                }
            }
        }

        // head
        if (headerModel != null) {
            sb.append("<thead>\n<tr>\n");

           /* if (isNumeratorEnable()) {
                sb.append("<th align='center'>#</th>\n");
            }*/

            for (MacHeaderColumn column : headerModel.getHeaders()) {
                String width = "";
                if (!column.getColumnWidth().equals("")) {
                    width = " width='" + column.getColumnWidth() + "' ";
                }

                sb.append("<th ").append(width).append(">").append(column.getText()).append("</th>\n");
            }
            sb.append("</tr></thead>\n");
        }

        // body
        int rowNumber = 0;
        sb.append("<tbody>\n");
        for (MacTableRow row : rows) {
            //System.out.println(rowNumber + " " + tableNavigator.getStartRow() + " " + tableNavigator.getEndRow());
            if (rowNumber > tableNavigator.getEndRow() - 1) {
                rowNumber++;
                continue;
            }

            if (rowNumber < tableNavigator.getStartRow()) {
                rowNumber++;
                continue;
            }

            String rowSelected = row.isSelected() ? " sel='selected' " : " sel='unselected' ";
            sb.append("<tr row='").append(rowNumber).append("' ").append(rowSelected).append(">\n");
            int column = 0;
            for (MacTableCell cell : row.getCells()) {
                String style = "style='border-right: 1px solid #666;' ";
                if (row.getCells().size() - 1 == column) {
                    style = "";
                }

                if (cell.getValue() instanceof Numerator) {
                    Numerator numerator = (Numerator) cell.getValue();

                    //String chkBox = "";
                    if (mode == MacTableModel.MODE_EDIT && canDeleteRow) {
                        final CheckBox chkBox = new CheckBox(getSession(), "" + (rowNumber + 1));
                        if (checkedRows.contains(rowNumber)) {
                            chkBox.setChecked(true);
                        }
                        chkBox.setValue("" + rowNumber);
                        chkBox.setCssClass("tblCheckBoxRemove");
                        chkBox.setName("tblCheckBoxRemove[]");
                        chkBox.addUIEventListener(new UIEventListener() {

                            @Override
                            public void event(UIEvent evt) {
                                if (chkBox.isChecked()) {
                                    checkedRows.add(Integer.parseInt(chkBox.getValue()));
                                } else {
                                    checkedRows.remove(Integer.parseInt(chkBox.getValue()));
                                }
                            }
                        });

                        sb.append("<td align='center' ").append(style).append(" "
                                + "row='").append(rowNumber).append("' column='").append(column).append("' " + "identificator='").append(cell.getIdentificator()).append("'>").append(chkBox.getModel()).append("</td>\n");
                    } else {
                        sb.append("<td align='center' ").append(style).append(" "
                                + "row='").append(rowNumber).append("' column='").append(column).append("' " + "identificator='").append(cell.getIdentificator()).append("'>").append(numerator.number).append("</td>\n");
                    }


                } else {
                    String align = "left";
                    if (cell.getValue() instanceof Number) {
                        align = "right";
                    }

                    if (cell.getValue() instanceof BigDecimal) {
                        align = "right";
                    }

                    if (mode == MacTableModel.MODE_EDIT) {
                        if (cell.isEditable()) {
                            if (cell.isSpinnerEnabled()) {
                                Spinner spinner = new Spinner(getSession(), "" + cell.getValue(), 1);
                                if (!("" + cell.getValue("spinnerStyle")).equals("null")) {
                                    spinner.setStyle("" + cell.getValue("spinnerStyle"));
                                }
                                spinner.setAttribute("row", "" + rowNumber);
                                spinner.setAttribute("column", "" + column);
                                spinner.addUIEventListener(new UIEventListener() {

                                    @Override
                                    public void event(UIEvent evt) {
                                        fireMacTableEvent(evt);
                                    }
                                });

                                sb.append("<td align='center'").append(style).append(">").append(spinner.getModel()).append("</td>");

                            } else {
                                TextField textField = new TextField(getSession(), "" + cell.getValue());
                                textField.setCssClass("macTableCellEditor");
                                textField.setStyle("text-align:" + align + ";" + cell.getStyle());
                                textField.setAttribute("row", "" + rowNumber);
                                textField.setAttribute("column", "" + column);
                                textField.addUIEventListener(new UIEventListener() {

                                    @Override
                                    public void event(UIEvent evt) {
                                        fireMacTableEvent(evt);
                                    }
                                });

                                sb.append("<td ").append(style).append(">").append(textField.getModel()).append("</td>");
                            }

                        } else {
                            sb.append("<td align='").append(align).append("' " + "valign='middle' " + "row='").append(rowNumber).append("' " + "column='").append(column).append("' " + "value='").append(cell.getValue().toString().replaceAll("\\'", "").replaceAll("\\\"", "")).append("' ").append(style).append(" class='tblCell' " + "identificator='").append(cell.getIdentificator()).append("'>").append(cell.getValue()).append("</td>\n");
                        }

                    } else {
                        String cellValue = "" + cell.getValue();
                        if (!cell.getStyle().equals("")) {
                            cellValue = "<span style='" + cell.getStyle() + "'>" + cell.getValue() + "</span>";
                        }
                        sb.append("<td align='").append(align).append("' "
                                + "valign='middle' " + "row='").append(rowNumber).append("' " + "column='").append(column).append("' " + "value='").append(cell.getValue().toString().replaceAll("\\'", "").replaceAll("\\\"", "")).append("' ").append(style).append(" class='tblCell' " + "identificator='").append(cell.getIdentificator()).append("'>").append(cellValue).append("</td>\n");
                    }
                }
                column++;

                cell.addUIEventListener(new UIEventListener() {

                    @Override
                    public void event(UIEvent evt) {
                        fireMacTableEvent(evt);
                    }
                });
            }

            sb.append("</tr>\n");
            rowNumber++;
        }
        sb.append("</tbody>\n</table>");
        Logger.getGlobal().log(Level.FINE, sb.toString());

        return sb.toString();
    }

    public ArrayList<Integer> getNumberCheckedRows() {
        ArrayList<Integer> checkedList = new ArrayList<>();
        int index = 0;
        for (MacTableRow row : rows) {
            if (checkedRows.contains(index)) {
                checkedList.add(index);
            }
            index++;
        }

        return checkedList;
    }

    public ArrayList<MacTableRow> getCheckedRows() {
        ArrayList<MacTableRow> checkedList = new ArrayList<>();
        int index = 0;
        for (MacTableRow row : rows) {
            if (checkedRows.contains(index)) {
                checkedList.add(row);
            }
            index++;
        }

        return checkedList;
    }

    public void setAllRowsChecked(boolean isChecked) {
        if (isChecked) {
            for (int i = 0; i < rows.size(); i++) {
                checkedRows.add(i);
            }
        } else {
            checkedRows.clear();
        }
    }
    
    public void setRowChecked(int indexRow){
        checkedRows.add(indexRow);
    }

    public void removeCheckedRows() {
        ArrayList<MacTableRow> removeList = new ArrayList<>();
        int index = 0;
        for (MacTableRow row : rows) {
            if (checkedRows.contains(index)) {
                removeList.add(row);
            }
            index++;
        }

        rows.removeAll(removeList);
        checkedRows.clear();

        if (isSummatorEnable()) {
            computeSum();
        }
    }

    @Override
    public String getIdentificator() {
        if (identificator == null) {
            identificator = "" + hashCode();
        } else if (identificator.equals("")) {
            identificator = "" + hashCode();
        }

        return identificator;
    }

    @Override
    public void setIdentificator(String identificator) {
        this.identificator = identificator;
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
        fireMacTableEvent(new UIEvent(json));
    }

    final class Numerator {

        public int number;
        //public JCheckBox checkBox;

        public Numerator(int number) {
            this.number = number;
            //checkBox = new JCheckBox();
            //checkBox.setText(number + "");
        }
    }

    public void setData(ArrayList<MacTableRow> dataList) {
       /* if (isNumeratorEnable) {
            rows = new ArrayList<>();
            int count = 1;
            for (MacTableRow mtr : dataList) {
                mtr.addCell(0, new MacTableCell(getSession(), new Numerator(count), true));
                rows.add(mtr);
                count++;
            }
            computeSum();

        } else {*/
            rows = dataList;
       // }
    }

    public ArrayList<MacTableRow> getData() {
//        if(isNumeratorEnable){
//            ArrayList<MacTableRow> data = new ArrayList<>();
//            int lastIndex = rows.size() - 1;
//            int index = 0;
//            for (MacTableRow mtr : rows) {
//                if(index == lastIndex){
//                    continue;
//                }
//                mtr.getCells().remove(0);
//                data.add(mtr);
//                index++;
//            }
//            return data;
//            
//        } else {
//            return rows;
//        }
        return rows;
    }

    public ArrayList<MacTableRow> getSelectedRows() {
        return selectedRows;
    }

    public MacTableModel getMe() {
        return this;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        if (mode == MacTableModel.MODE_EDIT) {
            canDeleteRow = true;
        }
        this.mode = mode;
    }

    public boolean isCanDeleteRow() {
        return canDeleteRow;
    }

    public void setCanDeleteRow(boolean canDeleteRow) {
        this.canDeleteRow = canDeleteRow;
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    @Override
    public String toString() {
        return "MacTableModel{" + "rows=" + rows + '}';
    }

    public boolean isNavigatorEnable() {
        return isNavigatorEnable;
    }

    public void setNavigatorEnable(boolean isNavigatorEnable) {
        this.isNavigatorEnable = isNavigatorEnable;
    }

    public MacTableNavigator getMacTableNavigator() {
        return tableNavigator;
    }

    public void setNavigatorDateSelectorEnabled(boolean isEnabled) {
        tableNavigator.setDateSelectorEnabled(isEnabled);
    }

    public boolean isNavigatorCalendarEnabled() {
        return tableNavigator.isDateSelectorEnabled();
    }
}
