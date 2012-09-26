
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.techcard.db;
import java.io.Serializable;
import org.jssdb.core.proxy.KnowsId;
import java.io.Serializable;
import org.jssdb.core.proxy.KnowsId;
import java.util.ArrayList;
/**
 *
 * @author developer
 */
public class MarshrutCard implements Serializable, KnowsId {

    public final long serialVersionUID = 5L;
    private long id;
    private String firmname = "";
    private String number1 = "";
    private String number2 = "";
    private String partname = "";
    private String formtype = "";
    private String matherialname="";
    private String kod,ev,md,en,nrash,kim,kodzagotovki,profile_size,mz,kd;
    private ArrayList<Long> operations = new ArrayList();
    
    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en;
    }

    public String getFormtype() {
        return formtype;
    }

    public void setFormtype(String formtype) {
        this.formtype = formtype;
    }

    public String getEv() {
        return ev;
    }

    public void setEv(String ev) {
        this.ev = ev;
    }

    public String getFirmname() {
        return firmname;
    }

    public void setFirmname(String firmname) {
        this.firmname = firmname;
    }


    public String getKd() {
        return kd;
    }

    public void setKd(String kd) {
        this.kd = kd;
    }

    public String getKim() {
        return kim;
    }

    public void setKim(String kim) {
        this.kim = kim;
    }

    public String getKod() {
        return kod;
    }

    public void setKod(String kod) {
        this.kod = kod;
    }

    public String getKodzagotovki() {
        return kodzagotovki;
    }

    public void setKodzagotovki(String kodzagotovki) {
        this.kodzagotovki = kodzagotovki;
    }

    public String getMatherialname() {
        return matherialname;
    }

    public void setMatherialname(String matherialname) {
        this.matherialname = matherialname;
    }

    public String getMd() {
        return md;
    }

    public void setMd(String md) {
        this.md = md;
    }

    public String getMz() {
        return mz;
    }

    public void setMz(String mz) {
        this.mz = mz;
    }

    public String getNrash() {
        return nrash;
    }

    public void setNrash(String nrash) {
        this.nrash = nrash;
    }

    public String getNumber1() {
        return number1;
    }

    public void setNumber1(String number1) {
        this.number1 = number1;
    }

    public String getNumber2() {
        return number2;
    }

    public void setNumber2(String number2) {
        this.number2 = number2;
    }

    public ArrayList<Long> getOperations() {
        return operations;
    }

    public void setOperations(ArrayList<Long> operations) {
        this.operations = operations;
    }

    public void addOperation(long id) {
        this.operations.add(id);
    }
    public String getPartname() {
        return partname;
    }

    public void setPartname(String partname) {
        this.partname = partname;
    }

    public String getProfile_size() {
        return profile_size;
    }

    public void setProfile_size(String profile_size) {
        this.profile_size = profile_size;
    }
    

   
   
    public MarshrutCard() {
        //
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
        return "MarshrutCard{" + "serialVersionUID=" + serialVersionUID + ", id=" + id + ", firmname=" + firmname + ", number1=" + number1 + ", number2=" + number2 + ", partname=" + partname + ", formtype=" + formtype + ", matherialname=" + matherialname + ", kod=" + kod + ", ev=" + ev + ", md=" + md + ", en=" + en + ", nrash=" + nrash + ", kim=" + kim + ", kodzagotovki=" + kodzagotovki + ", profile_size=" + profile_size + ", mz=" + mz + ", kd=" + kd + ", operations=" + operations + '}';
    }



    

 
}
