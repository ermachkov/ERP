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
public class Operations implements Serializable, KnowsId {

    public final long serialVersionUID = 1L;
    private long id;
    private String name = "";
    private long number;
    private String sm,rm,ut,kr,Tpz,docname,Kst,prof,Tsh,r,koid,uch,devicename,en,ceh,opername;

    public Operations() {
        //
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public String getKst() {
        return Kst;
    }

    public void setKst(String Kst) {
        this.Kst = Kst;
    }

    public String getTpz() {
        return Tpz;
    }

    public void setTpz(String Tpz) {
        this.Tpz = Tpz;
    }

    public String getTsh() {
        return Tsh;
    }

    public void setTsh(String Tsh) {
        this.Tsh = Tsh;
    }

    public String getCeh() {
        return ceh;
    }

    public void setCeh(String ceh) {
        this.ceh = ceh;
    }

    public String getDevicename() {
        return devicename;
    }

    public void setDevicename(String devicename) {
        this.devicename = devicename;
    }

    public String getDocname() {
        return docname;
    }

    public void setDocname(String docname) {
        this.docname = docname;
    }

    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en;
    }

    public String getKoid() {
        return koid;
    }

    public void setKoid(String koid) {
        this.koid = koid;
    }

    public String getKr() {
        return kr;
    }

    public void setKr(String kr) {
        this.kr = kr;
    }

    public String getOpername() {
        return opername;
    }

    public void setOpername(String opername) {
        this.opername = opername;
    }

    public String getProf() {
        return prof;
    }

    public void setProf(String prof) {
        this.prof = prof;
    }

    public String getR() {
        return r;
    }

    public void setR(String r) {
        this.r = r;
    }

    public String getRm() {
        return rm;
    }

    public void setRm(String rm) {
        this.rm = rm;
    }

    public String getSm() {
        return sm;
    }

    public void setSm(String sm) {
        this.sm = sm;
    }

    public String getUch() {
        return uch;
    }

    public void setUch(String uch) {
        this.uch = uch;
    }

    public String getUt() {
        return ut;
    }

    public void setUt(String ut) {
        this.ut = ut;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        return "Operations{" + "serialVersionUID=" + serialVersionUID + ", id=" + id + ", name=" + name + ", sm=" + sm + ", rm=" + rm + ", ut=" + ut + ", kr=" + kr + ", Tpz=" + Tpz + ", docname=" + docname + ", Kst=" + Kst + ", prof=" + prof + ", Tsh=" + Tsh + ", r=" + r + ", koid=" + koid + ", uch=" + uch + ", devicename=" + devicename + ", en=" + en + ", ceh=" + ceh + ", opername=" + opername + '}';
    }
   
}
