/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.sibek.plugin.staff.crew;

import javax.swing.JButton;
import org.ubo.employee.Crew;
import org.ubo.employee.Specialty;
import org.ubo.tree.TreeLeafBasic;
import org.uui.component.RightPanel;
import org.uui.db.DataBase;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author anton
 */
public class CrewItemPanel extends RightPanel {

//    private RoundedTextField name;
//    private RoundedTextField count;
    private JButton save;
    private StaffCrewPanel staffCrewPanel;
    private DataBase db;
    private TreeLeafBasic curLeaf;
    private Crew curCrew;
    private Specialty curSpec;

    public CrewItemPanel(String sessionId){
        super(sessionId);
        db = CarssierDataBase.getDataBase();
        
//        gp = new GradientPanel();
//        name = new RoundedTextField();
//        gp.add(new JLabel("Name"), "w 20%");
//        gp.add(name, "w 80%");
//
//        add(gp, "w 100%, wrap");
//
//        gp = new GradientPanel();
//        count = new RoundedTextField();
//        gp.add(new JLabel("Phone"), "w 20%");
//        gp.add(count, "w 80%");
//
//        add(gp, "w 100%, wrap");
//        
//        save = new JButton("Save");
//        save.setEnabled(false);
//        add(save, "w 100%, sx 2");
//
//        save.addActionListener(new ActionListener(){
//            public void actionPerformed(ActionEvent e) {
//                Save();
//            }
//        });
    }

    public void setStaffCrewPanel(StaffCrewPanel staffCrewPanel) {
        this.staffCrewPanel = staffCrewPanel;
    }

    @Override
    public String getName() {
        return "Crew";
    }

    void setCrewItem(long crewId, TreeLeafBasic treeLeaf) {
//        curLeaf = treeLeaf;
//        curCrew = (Crew)db.getObject(Crew.class.getName(), crewId);
//        curSpec = (Specialty)db.getObject(Specialty.class.getName(), (Long)curLeaf.getValue());
//        curCrewItem = curCrew.getCrewItem(curSpec.getId());
//
//        name.setText(curSpec.getName());
//        count.setText(""+curCrewItem.getCount());
//        save.setEnabled(true);
    }

    private void Save(){
        //curCrew;
//        curSpec = (Specialty)db.getObject(Specialty.class.getName(), (Long)curLeaf.getValue());
//        curCrewItem = curCrew.getCrewItem(curSpec.getId());
//
//        curLeaf = treeLeaf;
    }

    @Override
    public void setModel(String html) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getModel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getIdentificator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setIdentificator(String identificator) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void fireEvent(String json) {if(json == null){             return;         }                  if(json.equals("")){             return;         }                  if(json.equals("{}")){             return;         }
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
