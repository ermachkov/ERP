package ru.sibek.business.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.ubo.employee.Crew;
import org.ubo.employee.Employee;
import org.ubo.goods.Goods;
import org.ubo.service.Service;
import org.uui.db.DataBase;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class CrewUsedInfo {

    private ArrayList<Service> usedServices = new ArrayList<>();
    private ArrayList<Goods> usedGoodses = new ArrayList<>();
    private Crew crew;
    private DataBase dataBase;

    public CrewUsedInfo(Crew crew, ArrayList<Service> usedServices, ArrayList<Goods> usedGoodses) {
        this.crew = crew;
        this.usedServices = usedServices;
        this.usedGoodses = usedGoodses;
        dataBase = CarssierDataBase.getDataBase();
    }

    public Crew getCrew() {
        return crew;
    }
    
    public ArrayList<Employee> getEmployees(){
        ArrayList<Employee> list = dataBase.getAllObjectsList(Employee.class.getName());
        Set<Employee> resultSet = new HashSet<>();
        for(Employee e : list){
            if(e.getDefaultCrew() == null){
                continue;
            }
            
            if(e.getDefaultCrew().getId() == crew.getId()){
                resultSet.add(e);
            }
            
            if(e.getAdditionInfoByKey("crewList") != null){
                ArrayList<Long> crewIds = (ArrayList<Long>)e.getAdditionInfoByKey("crewList");
                for(long id : crewIds){
                    if(id == crew.getId()){
                        resultSet.add(e);
                    }
                }
            }
        }
        
        ArrayList<Employee> resultList = new ArrayList<>();
        resultList.addAll(resultSet);
        
        return resultList;
    }
    
    public ArrayList<Service> getUsedServices() {
        return usedServices;
    }

    public ArrayList<Goods> getUsedGoodses() {
        return usedGoodses;
    }

    public boolean isCrewUsed() {
        boolean result = false;

        if (!usedServices.isEmpty()) {
            result = true;
        }

        if (!usedGoodses.isEmpty()) {
            result = true;
        }

        if (!crew.getIdsEmployees().isEmpty()) {
            result = true;
        }
        
        ArrayList<Employee> list = dataBase.getAllObjectsList(Employee.class.getName());
        for(Employee e : list){
            if(e.getDefaultCrew() == null){
                continue;
            }
            
            if(e.getDefaultCrew().getId() == crew.getId()){
                result = true;
            }
            
            if(e.getAdditionInfoByKey("crewList") != null){
                ArrayList<Long> crewIds = (ArrayList<Long>)e.getAdditionInfoByKey("crewList");
                for(long id : crewIds){
                    if(id == crew.getId()){
                        result = true;
                    }
                }
            }
        }

        return result;
    }
}
