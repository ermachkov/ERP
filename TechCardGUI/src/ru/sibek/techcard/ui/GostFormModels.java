/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.techcard.ui;
import java.io.*;
import java.util.ArrayList;
/**
 *
 * @author toor
 */
public class GostFormModels {

    
private int i = 0,j=0,k=0,operationCount=5;
private ArrayList<String> okpdtr = new ArrayList();
String mas="[";
public GostFormModels() {
    okpdtr.add("12313"+"-"+"Worker1");
    okpdtr.add("25453"+"-"+"Worker2");
    okpdtr.add("36663"+"-"+"Worker3");
    //data-source='[&quot;Alabama&quot;,&quot;Ямайка&quot;]'>"
    for (String ok:okpdtr)
    {
        mas+="&quot;"+ok+"&quot;"+",";
    }
    mas+="]";
    mas.replace(",]", "]");
    
    }
    public String getForm1Model() {
       i=0;j=0;k=0;
        String model = ""
                + "<h4>Введите наименование конторы:</h4>"
                + "<input type='text' class='input-xlarge' name='firmname' id='input_"+ i++ +"'>"
                + "<h4>Введите номер 1:</h4>"
                + "<input type='text' class='input-xlarge' name='number1' id='input_"+ i++ +"'>"
                + "<h4>Введите номер 2:</h4>"
                + "<input type='text' class='input-xlarge' name='number2' id='input_"+ i++ +"'>"
                + "<h4>Введите наименование детали:</h4>"
                + "<input type='text' class='input-xlarge' name='partname' id='input_"+ i++ +"'>"
                + "<h4>Наимеование материала(М01):</h4>"
                + "<input type='text' class='input-xlarge' name='matname' id='input_"+ i++ +"'>"
                + "<h4>Информация о применяемых вспомогательных и комплектующих материалах(М02):</h4>"
                + "<table  class='macTable table table-striped table-bordered' id='m02'>"
                + "<thead>"
                + "<tr>"
                + "<th align='center'>КОД</th>"
                + "<th align='center'>ЕВ</th>"
                + "<th align='center'>МД</th>"
                + " <th align='center'>ЕН</th>"
                + "<th align='center'>Н.Расх</th>"
                + "<th align='center'>КИМ</th>"
                + "<th align='center'>Код заготовки</th>"
                + "<th align='center'>Профиль и размеры</th>"
                + "<th align='center'>МЗ</th>"
                + "<th align='center'>КД</th>"
                + "</tr>"
                + "</thead>"
                + "<tbody>"
                + "<tr>";
        for (k= i; k < i+10; k++) {
            model += "<td align='center'>"
                    + "<input type='text' class='input-mini' id='input_"+k+"'>"
                    + "</td>";
        }
            i=k;
        model += "</tr>"
                + "</tbody>"
                + "</table>"
                + "<h4>Операции</h4>"
                + "<table  class='macTable table table-striped table-bordered' id='operationsTable'>"
                + "<thead>"
                + "<tr>"
                + "<th align='center'>Цех</th>"
                + "<th align='center'>УЧ</th>"
                + "<th align='center'>РМ</th>"
                + "<th align='center'>ОПЕР</th>"
                + "<th align='center'>Наименование операции</th>"
                + "<th align='center'>Обозначение документа</th>"
                + "<th align='center'>Наименование оборудования</th>"
                + "<th align='center'>СМ</th>"
                + "<th align='center'>ПРОФ</th>"
                + "<th align='center'>Р</th>"
                + "<th align='center'>УТ</th>"
                + "<th align='center'>КР</th>"
                + "<th align='center'>КОИД</th>"
                + "<th align='center'>ЕН</th>"
                + "<th align='center'>Кшт</th>"
                + "<th align='center'>Тпз</th>"
                + "<th align='center'>Тшт</th>"
                + "</tr>"
                + "</thead>"
                + "<tbody>"
                + "<tr>" //FROM
                + "<td align='center'>"
                + "<input type='text' class='input-mini' name='ceh' id='input_"+i+++"'>"
                + "</td>"
                + "<td align='center' >"
                + "<input type='text' class='input-mini' name='uch' id='input_"+i+++"'>"
                + "</td>"
                + "<td align='center' >"
                + "<input type='text' class='input-mini' name='rm' id='input_"+i+++"'>"
                + "</td>"
                + "<td align='center' >"
                + "<span style='white-space:nowrap;' name='oper' id='numberinput__"+i+++"'>"+operationCount+"</span>    "
                + "</td>"
                + "<td align='left'>"
                + "<span style='white-space:nowrap;'>"
                + "<input type='text' id='input_"+i+++"' name='opername' class='input-mini' style='margin: 0 auto;' data-provide='typeahead' data-items='4' data-source='[&quot;Alabama&quot;,&quot;Ямайка&quot;]'>"
                + "</span>"
                + "</td>"
                + "<td>"
                + "<span style='white-space:nowrap;'>"
                + "<input type='text' id='input_"+i+++"' name='docname' class='input-mini' style='margin: 0 auto;' data-provide='typeahead' data-items='4' data-source='[&quot;Alabama&quot;,&quot;Ямайка&quot;]'>"
                + "</span>"
                + "</td>"
                + "<td align='left'>"
                + "<span style='white-space:nowrap;'>"
                + "<input type='text' id='input_"+i+++"' name='devicename' class='input-mini' style='margin: 0 auto;' data-provide='typeahead' data-items='4' data-source='[&quot;Alabama&quot;,&quot;Ямайка&quot;]'>"
                + "</span>"
                + "</td>"
                + "<td align='center' >"
                + "<input type='text' id='input_"+i+++"' name='sm' class='input-mini' id='input'>"
                + "</td>"
                + "<td align='center' >"
                + "<span style='white-space:nowrap;'>"
                + "<input type='text' id='input_"+i+++"' name='prof' class='input-mini' style='margin: 0 auto;' data-provide='typeahead' data-items='4' data-source='[&quot;Alabama&quot;,&quot;Ямайка&quot;]'>"
                + "</span>"
                + "</td>"; 
  
        String[] temp={"r","ut","kr","koid","en","Kst","Tpz","Tsh"};
        short cnt=0;
        for (j = i; j < i+8; j++) {
            //if (cnt<8) cnt++;
            model += "<td align='center'>"
                    + "<input type='text' class='input-mini' name='"+temp[cnt++]+"' id='input_" + j + "'>"
                    + "</td>";
        }
i=j;
 

        model += "</tr>" //WHO
                + " </tbody>"
                + " </table>";
        /*
         * <a style='float: left; margin-top:10px;margin-right:10px'
         * href='#'>добавить операцию</a>
         */

        // <input type='radio' id='radio1' checked=''>по умолчанию<input type='radio' id='radios2''>с номером <input type='text' style='margin-top:2px;margin-left:3px;width:35px;' class='input-small'><hr>


        return model;


    }
    
     public String getForm1operationRow(int numberOperation) {
         if (numberOperation!=0) operationCount=numberOperation;
         else operationCount+=5;
     String row=""
         
                + "<tr>" 
                + "<td align='center'>"
                + "<input type='text' class='input-mini' name='ceh' id='input_"+i+++"'>"
                + "</td>"
                + "<td align='center' >"
                + "<input type='text' class='input-mini' name='uch' id='input_"+i+++"'>"
                + "</td>"
                + "<td align='center' >"
                + "<input type='text' class='input-mini' name='rm' id='input_"+i+++"'>"
                + "</td>"
                + "<td align='center' >"
                + "<span name='oper' id='numberinput_"+i+++"' style='white-space:nowrap;'>"+  operationCount +"</span>    "
                + "</td>"
                + "<td align='left'>"
                + "<span style='white-space:nowrap;'>"
                + "<input type='text' name='opername' id='input_"+i+++"' class='input-mini' style='margin: 0 auto;' data-provide='typeahead' data-items='4' data-source='[&quot;Alabama&quot;,&quot;Ямайка&quot;]'>"
               // + "</select>"
                + "</span>"
                + "</td>"
                + "<td>"
                + "<span style='white-space:nowrap;'>"
                + "<input id='input_"+i+++"' name='docname' type='text' class='input-mini' style='margin: 0 auto;' data-provide='typeahead' data-items='4' data-source='[&quot;Alabama&quot;,&quot;Ямайка&quot;]'>"
                + "</span>"
                + "</td>"
                + "<td align='left'>"
                + "<span style='white-space:nowrap;'>"
                + "<input id='input_"+i+++"' type='text' name='devicename' class='input-mini' style='margin: 0 auto;' data-provide='typeahead' data-items='4' data-source='[&quot;Alabama&quot;,&quot;Ямайка&quot;]'>"
                + "</span>"
                + "</td>"
                + "<td align='center' >"
                + "<input type='text' name='sm' class='input-mini' id='input_"+i+++"'>"
                + "</td>"
                + "<td align='center' >"
                + "<span style='white-space:nowrap;'>"
                + "<input name='prof' id='input_"+i+++"' type='text' class='input-mini' style='margin: 0 auto;' data-provide='typeahead' data-items='4' data-source='[&quot;Alabama&quot;,&quot;Ямайка&quot;]'>"
                + "</span>"
                + "</td>"; 
     String[] temp={"r","ut","kr","koid","en","Kst","Tpz","Tsh"};
        short cnt=0;
        for ( j = i; j < i+8; j++) {
            
            row += "<td align='center'>"
                    + "<input type='text' class='input-mini' name='"+temp[cnt++]+"' id='input_" + j + "'>"
                    + "</td>";
        }

i=j;
        row += "</tr>"; 
         return row;
     }
}
