package pl.umk.cm.test;

import java.util.ArrayList;
import java.util.List;

public class PaczkaView {
    private String paczkaId;
    private String statusId;
    private String statusNazwa;
    private String odbiorcaId;
    private String dataOdbioru;
    private String kodOdbioru;
    private String gabarytNazwa;
    private String jednostkaNazwa;
    private String adresNazwa;
    private String wyjazdId;
    private String wyjazdData;
    private ArrayList<AdresatView> adresatViewList;

    public PaczkaView(String PaczkaId,
                      String StatusId,
                      String StatusNazwa,
                      String OdbiorcaId,
                      String DataOdbioru,
                      String KodOdbioru,
                      String GabarytNazwa,
                      String JednostkaNazwa,
                      String AdresNazwa,
                      String WyjazdId,
                      String WyjazdData,
                      ArrayList<AdresatView> AdresatViewList
    ) {
        this.paczkaId = PaczkaId;
        this.statusId = StatusId;
        this.statusNazwa = StatusNazwa;
        this.odbiorcaId = OdbiorcaId;
        this.dataOdbioru = DataOdbioru;
        this.kodOdbioru = KodOdbioru;
        this.gabarytNazwa = GabarytNazwa;
        this.jednostkaNazwa = JednostkaNazwa;
        this.adresNazwa = AdresNazwa;
        this.wyjazdId = WyjazdId;
        this.wyjazdData = WyjazdData;
        this.adresatViewList = AdresatViewList;
    }

    public String getPaczkaId(){
        return paczkaId;
    }
    public String getStatusId() {
        return statusId;
    }
    public String getStatusNazwa() {
        return statusNazwa;
    }
    public String getOdbiorcaId(){
        return odbiorcaId;
    }
    public String getDataOdbioru(){
        return dataOdbioru;
    }
    public String getKodOdbioru(){
        return kodOdbioru;
    }
    public String getGabarytNazwa() {
        return gabarytNazwa;
    }
    public String getJednostkaNazwa(){
        return jednostkaNazwa;
    }
    public String getAdresNazwa(){
        return adresNazwa;
    }
    public String getWyjazdId() { return wyjazdId; }
    public String getWyjazdData() { return wyjazdData; }
    public String getAdresatViewList() {
        String aVL = "";
        for (int i = 0; i < adresatViewList.size(); i++) {
            if(i>0){
                aVL +=",";
            }
            AdresatView adresatView = adresatViewList.get(i);
            aVL += "{";
            aVL += "\"AdresatId\":\""+adresatView.getAdresatId()+"\",";
            aVL += "\"Kod\":\""+adresatView.getKod()+"\",";
            aVL += "\"Imie\":\""+adresatView.getImie()+"\",";
            aVL += "\"Nazwisko\":\""+adresatView.getNazwisko()+"\",";
            aVL += "\"EmailAdres\":\""+adresatView.getEmailAdres()+"\"";
            aVL += "}";
        }
            return aVL;
    }
}

