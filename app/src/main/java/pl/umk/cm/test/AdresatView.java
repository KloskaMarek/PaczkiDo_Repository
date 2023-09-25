package pl.umk.cm.test;

public class AdresatView {
    private String adresatId;
    private String kod;
    private String imie;
    private String nazwisko;
    private String emailAdres;

    public AdresatView(String AdresatId,
                        String Kod,
                        String Imie,
                        String Nazwisko,
                        String EmailAdres)
    {
        this.adresatId = AdresatId;
        this.kod = Kod;
        this.imie = Imie;
        this.nazwisko = Nazwisko;
        this.emailAdres = EmailAdres;
    }

    public String getAdresatId(){
        return adresatId;
    }
    public String getKod(){
        return kod;
    }
    public String getImie(){
        return imie;
    }
    public String getNazwisko(){
        return nazwisko;
    }
    public String getEmailAdres() { return emailAdres; }
}
