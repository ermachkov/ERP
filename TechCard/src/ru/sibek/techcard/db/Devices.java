/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.techcard.db;

import java.io.Serializable;
import org.jssdb.core.proxy.KnowsId;
import java.util.ArrayList;

/**
 *
 * @author developer
 */
// Для автоматического форматирования текста всегда используй Alt+Shift+F
// Для генерации метода toString -> правая кнопка мыши, Вставка кода, toString
// Для обозначения имен полей и названий методов используем венгерскую нотацию
// WRONG -> get_person, person_name, person_Name
// CORRECT -> getPerson, personName
// *** Самодокументирование ***
// Сокращения запрещены, имена полей и методов должны на простом человеческом языке 
// доносить что они обозначают, что они возвращает или что они делает
// ВАЖНО! То что ты пишешь внутри метода также должно соотвествовать принципам приведенным выше
// WRONG -> get_pn, getPN
// CORRECT -> getPersonName
// Mетоды которые могут вернуть null ДОЛЖНЫ БЫТЬ документированы таким образом
//    /**
//     * 
//     * @return Person or null
//     */
//    public Person getPerson() {
//    ...
//    }
// 
//
// классы нужно помещать в пакеты которые отражают принадлежность к компании
// например не techcard.db, а ru.sibek.techcard.db
// Резюме:
// Главный посыл - код пишется для идиотов и имбецилов. 
// Это значит, что любой идиот (например я) который заглянет в твой код, должен понять все без доп объяснений.
// Если ты видишь, что для какого-то метода необходимо написать комментарий см. пункт *** Самодокументирование *** 
// P.S. Да букв придется писать больше, но тебе зато не придется писать для кода документацию
// Про этот класс 
// Я например сходу не понял что значит int_ и ext_
// возможно это Internal и External
public class Devices implements Serializable, KnowsId {

    public final long serialVersionUID = 3L;
    private long id;
    private String name = "";
    private String internalPartNumber = "";
    private String externalPartNumber = "";

    public Devices() {
        //
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInternalPartNumber() {
        return internalPartNumber;
    }

    public void setInternalPartNumber(String int_partnumber) {
        this.internalPartNumber = int_partnumber;
    }

    public String getExternalPartNumber() {
        return externalPartNumber;
    }

    public void setExternalPartNumber(String ext_partnumber) {
        this.externalPartNumber = ext_partnumber;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Devices{" + "serialVersionUID=" + serialVersionUID + ", id=" + id + ", name=" + name + ", int_partnumber=" + internalPartNumber + ", ext_partnumber=" + externalPartNumber + '}';
    }

  
}
