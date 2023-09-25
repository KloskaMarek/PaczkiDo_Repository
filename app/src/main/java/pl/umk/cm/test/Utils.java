package pl.umk.cm.test;

import static android.content.Context.MODE_PRIVATE;
import static android.system.Os.rename;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.StrictMode;
import android.text.format.DateFormat;
import android.util.Base64;
import android.widget.DatePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Utils {
    public static final String TAG = "Marek";
    public static final String SERVER = "https://paczki.cm.umk.pl";  // produkcyjny
//    public static final String SERVER = "http://192.168.4.197/PaczkiDo";  // testowy lokalny
    public static final String auth = "paczki@cm.umk.pl:Paczki2022";
    //    public static final String auth = "marek.kloska@cm.umk.pl:N0Ve#2o22!";

    public static boolean fileRename(File oldFile, File fileNew, Context context) {
        ContextWrapper cw = new ContextWrapper(context);
        //java.io.File oldFile = new java.io.File(cw.getFileStreamPath(fileOld).toString());
        if (oldFile.renameTo(fileNew)) {
            return true;
        }
        return false;
    }

    public static boolean fileExist(String fileName, Context context) {
        ContextWrapper cw = new ContextWrapper(context);
        String dir = String.valueOf(cw.getFilesDir());
//        java.io.File file = new java.io.File(cw.getFileStreamPath(fileName).toString());
        File file = new File(dir + "/" + fileName);
        if(file.exists()) {
            // date of file
            long lastModified = file.lastModified();
            String dateString = DateFormat.format("yyyy-MM-dd", new Date(lastModified)).toString();

            // local date & time
            String d = LocalDateTime.now().toString();
            String dt = d.replace("-",""); // data i czas full
            String dts = dt.replace(":",""); // bez dwukropków w czasie
            String dtp = dts.replace("T","_"); // bez litery T
            String dtm = dtp.substring(0,15); // bez ułamków sekund

            // new file name
            File newFile = new File(dir + "/" + fileName + dtm);


            String dateNow = d.substring(0, 10);
            //TODO tylko do testu. PRODUKCYJNIE musi być negacja!
            if (!dateString.equals(dateNow)) {
//                if (cw.deleteFile(fileName)) {
//                    return false;
//                }
                //TODO nie usuwać tylko zmienić nazwę
                if (fileRename(file, newFile, context)) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return file.exists();
    }

    public static StringBuilder loadFile(String fileName, Context context) {
        FileInputStream fis = null;
        StringBuilder sb = new StringBuilder();
        try {
            ContextWrapper cw = new ContextWrapper(context);
            fis = cw.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String text;

            while ((text = br.readLine()) != null){
                sb.append(text); //.append("\n");
            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb;
    }

    public static void saveFile(String jsonFileContent, String fileName, Context context) {
        FileOutputStream fos = null;
        try {
            ContextWrapper cw = new ContextWrapper(context);
            fos = cw.openFileOutput(fileName, MODE_PRIVATE);
            fos.write(jsonFileContent.getBytes());
            FileDescriptor fd = fos.getFD();
            String sfd = fd.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getOdbiorcaId(String wyjazdJson, String paczkaId, String kodOdbioru){
        String odbiorcaId = "0";
        ArrayList<PaczkaView> arrayList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(wyjazdJson);
            JSONArray jsonArray = jsonObject.getJSONArray("Paczki");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                String actualPaczkaId = json.getString("PaczkaId");
                if(paczkaId.equals(actualPaczkaId)) {
                    String odbiorcy = json.getString("Odbiorcy");
                    odbiorcy = "{\"Odbiorcy\":" + odbiorcy + "}";
                    JSONObject jsonOdbiorcyObject = new JSONObject(odbiorcy);
                    JSONArray jsonOdbiorcyArray = jsonOdbiorcyObject.getJSONArray("Odbiorcy");
                    for (int j = 0; j < jsonOdbiorcyArray.length(); j++) {
                        JSONObject jsonOdbiorcy = jsonOdbiorcyArray.getJSONObject(j);
                        String actualKodOdbioru = jsonOdbiorcy.getString("Kod");
                        if(kodOdbioru.equals(actualKodOdbioru)) {
                            odbiorcaId = jsonOdbiorcy.getString("AdresatId");
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            odbiorcaId = "-1";
            e.printStackTrace();
        }
        return odbiorcaId;
    }

    /// Wypełnienie tablicy z listą paczek danymi z pliku Wyjazd.
    public ArrayList<PaczkaView> getArrayList(String s){
        ArrayList<PaczkaView> arrayList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONArray jsonArray = jsonObject.getJSONArray("Paczki");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);

                String paczkaId = json.getString("PaczkaId");
                String statusId = json.getString("StatusId");
                String statusNazwa = json.getString("StatusNazwa");
                String odbiorcaId = json.getString("OdbiorcaId");
                String dataOdbioru = json.getString("DataOdbioru");
                String kodOdbioru = json.getString("KodOdbioru");
                String gabarytNazwa = json.getString("GabarytNazwa");
                String jednostkaNazwa = json.getString("JednostkaNazwa");
                String adresNazwa = json.getString("AdresNazwa");
                String wyjazdId = json.getString("WyjazdId");
                String wyjazdData = json.getString("WyjazdData");
                String odbiorcy = json.getString("Odbiorcy");

                ArrayList<AdresatView> arrayListAdresaci = new ArrayList<>();

                JSONObject jsonOdbiorcyObject = new JSONObject(odbiorcy);
                JSONArray jsonOdbiorcyArray = jsonOdbiorcyObject.getJSONArray("Odbiorcy");
                for (int j = 0; j < jsonOdbiorcyArray.length(); j++) {
                    JSONObject jsonOdbiorcy = jsonOdbiorcyArray.getJSONObject(j);
                    String adresatId = jsonOdbiorcy.getString("AdresatId");
                    String emailAdres = jsonOdbiorcy.getString("EmailAdres");
                    String imie = jsonOdbiorcy.getString("Imie");
                    String nazwisko = jsonOdbiorcy.getString("Nazwisko");
                    String kod = jsonOdbiorcy.getString("Kod");
                    arrayListAdresaci.add(new AdresatView(adresatId, kod, imie, nazwisko, emailAdres));
                }

                arrayList.add(new PaczkaView(paczkaId, statusId, statusNazwa,
                        odbiorcaId, dataOdbioru, kodOdbioru, gabarytNazwa,
                        jednostkaNazwa, adresNazwa, wyjazdId, wyjazdData, arrayListAdresaci));
            }
        } catch (JSONException e) {
            //          e.printStackTrace();
        }
        return arrayList;
    }

    /// Z tablicy zawierającej listę paczek, tworzy ciąg znakowy do przekazania na serwer.
    public static String setJsonContentsFileWyjazd(ArrayList<PaczkaView> arrayList){
        String wyjazdJSON = "{\"Paczki\":[";
        String paczkaJSON;
        for (int i = 0; i < arrayList.size(); i++) {
            PaczkaView paczkaView = arrayList.get(i);
            paczkaJSON = "{";
            paczkaJSON += "\"PaczkaId\":\""+paczkaView.getPaczkaId()+"\",";
            paczkaJSON += "\"StatusId\":\""+paczkaView.getStatusId()+"\",";
            paczkaJSON += "\"StatusNazwa\":\""+paczkaView.getStatusNazwa()+"\",";
            paczkaJSON += "\"OdbiorcaId\":\""+paczkaView.getOdbiorcaId()+"\",";
            paczkaJSON += "\"DataOdbioru\":\""+paczkaView.getDataOdbioru()+"\",";
            paczkaJSON += "\"KodOdbioru\":\""+paczkaView.getKodOdbioru()+"\",";
            paczkaJSON += "\"GabarytNazwa\":\""+paczkaView.getGabarytNazwa()+"\",";
            paczkaJSON += "\"JednostkaNazwa\":\""+paczkaView.getJednostkaNazwa()+"\",";
            paczkaJSON += "\"AdresNazwa\":\""+paczkaView.getAdresNazwa()+"\",";
            paczkaJSON += "\"WyjazdId\":\""+paczkaView.getWyjazdId()+"\",";
            paczkaJSON += "\"WyjazdData\":\""+paczkaView.getWyjazdData()+"\",";
            paczkaJSON += "\"Odbiorcy\":["+paczkaView.getAdresatViewList();
            paczkaJSON += "]},";
            wyjazdJSON += paczkaJSON;
        }
        wyjazdJSON = wyjazdJSON.substring(0,wyjazdJSON.length()-1); // usunięcie ostatniego przecinka
        wyjazdJSON += "]}";
        return wyjazdJSON;
    }

    public static String statusEnum(String status){
        String nazwa = "";
        switch(status){
            case "10":
                nazwa = "nowa";
                break;
            case "20":
                nazwa = "zaplanowana";
                break;
            case "30":
                nazwa = "w_samochodzie";
                break;
            case "40":
                nazwa = "odebrana";
                break;
            case "50":
                nazwa = "wycofana";
                break;
            default:
                nazwa = "statusError";
        }
        return nazwa;
    }

    /// Aktualizacja statusu paczki tylko w ArrayList
    /// Po tej operacji należy wywołać updateWyjazd().
    public static ArrayList<PaczkaView> updateArrayList(ArrayList<PaczkaView> arrayListPaczka, String s, String id, String podanyKodOdbioru, String nowyStatus, String odbId){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime datetime = LocalDateTime.now();
        String teraz = formatter.format(datetime);
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONArray jsonArray = jsonObject.getJSONArray("Paczki");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);

                String paczkaId = json.getString("PaczkaId");
                String statusId = json.getString("StatusId");
                String statusNazwa = json.getString("StatusNazwa");
                String odbiorcaId = json.getString("OdbiorcaId");
                String dataOdbioru = json.getString("DataOdbioru");
                String kodOdbioru = json.getString("KodOdbioru");
                String gabarytNazwa = json.getString("GabarytNazwa");
                String jednostkaNazwa = json.getString("JednostkaNazwa");
                String adresNazwa = json.getString("AdresNazwa");
                String wyjazdId = json.getString("WyjazdId");
                String wyjazdData = json.getString("WyjazdData");

                JSONArray jsonOdbiorcyArray = json.getJSONArray("Odbiorcy");

                ArrayList<AdresatView> arrayListAdresaci = new ArrayList<>();
                for (int j = 0; j < jsonOdbiorcyArray.length(); j++) {
                    JSONObject jsonOdbiorcy = jsonOdbiorcyArray.getJSONObject(j);
                    String adresatId = jsonOdbiorcy.getString("AdresatId");
                    String emailAdres = jsonOdbiorcy.getString("EmailAdres");
                    String imie = jsonOdbiorcy.getString("Imie");
                    String nazwisko = jsonOdbiorcy.getString("Nazwisko");
                    String kod = jsonOdbiorcy.getString("Kod");
                    arrayListAdresaci.add(new AdresatView(adresatId, kod, imie, nazwisko, emailAdres));
                }

//#region Logika
                if(paczkaId.equals(id)) {
                    if(nowyStatus.equals("30")){ // Ustaw "na_samochodzie"
                        if(statusId.equals("20")){
                            statusId = "30";
                            statusNazwa = Utils.statusEnum(statusId);
                        }
                    }
                    if(nowyStatus.equals("40")){ // Ustaw "odebrana"
                        if(statusId.equals("30")){
                            statusId = "40";
                            statusNazwa = Utils.statusEnum(statusId);
                            dataOdbioru = teraz;
                            kodOdbioru = podanyKodOdbioru;
                            odbiorcaId = odbId;
                        }
                    }
                }
//#endregion Logika

                arrayListPaczka.add(new PaczkaView(paczkaId, statusId, statusNazwa, odbiorcaId, dataOdbioru, kodOdbioru, gabarytNazwa, jednostkaNazwa, adresNazwa, wyjazdId, wyjazdData, arrayListAdresaci));
            }
        } catch (JSONException e) {
//          e.printStackTrace();
        }
        return arrayListPaczka;
    }

    public static String getWyjazdId(ArrayList<PaczkaView> arrayList){
        String wyjazdId = "";
        PaczkaView paczkaView = arrayList.get(0);
        wyjazdId = paczkaView.getWyjazdId();
        return wyjazdId;
    }
    public static String getWyjazdData(ArrayList<PaczkaView> arrayList){
        String wyjazdData = "";
        PaczkaView paczkaView = arrayList.get(0);
        wyjazdData = paczkaView.getWyjazdData();
        return wyjazdData;
    }

    public static boolean updateWyjazd(String jsonWyjazd, Context context) {
        boolean rezultat = false;
        HttpURLConnection urlConnection = null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            String linkPaczkaWyjazd = SERVER + "/Paczki/UpdateWyjazd/";
            URL urlPaczkaWyjazd = new URL(linkPaczkaWyjazd);
            String coded = Base64.encodeToString(auth.getBytes(StandardCharsets.UTF_8),Base64.NO_WRAP);
            String authHeaderValue = "Basic "+coded;

            urlConnection = (HttpURLConnection) urlPaczkaWyjazd.openConnection();
            urlConnection.setRequestProperty("Authorization", authHeaderValue);
            urlConnection.setRequestProperty("Content-Type","application/json");     // ważne
            urlConnection.setRequestProperty("Accept-Encoding","gzip, deflate, br"); // ważne
            urlConnection.setRequestProperty("Connection","keep-alive");
            urlConnection.setRequestMethod("POST");
            urlConnection.setReadTimeout(15000);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setChunkedStreamingMode(1024); // wysyła stream w paczkach po 1kB
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true); // ważne
            OutputStream os = urlConnection.getOutputStream();

            urlConnection.connect();

            String strDoUsuniecia = "{\"Paczki\":";
            int ileDoUsuniecia = strDoUsuniecia.length();
            String przedrostek = jsonWyjazd.substring(0,ileDoUsuniecia);
            if (przedrostek.equals(strDoUsuniecia)) {
                //substring(od którego znaku, do którego znaku)
                jsonWyjazd = jsonWyjazd.substring(ileDoUsuniecia,jsonWyjazd.length()-1); // bez {"Paczki": i ostatniej klamry. Tylko same paczki.
            }
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(jsonWyjazd); // nie dodajemy dodatkowych cudzysłowów!
            writer.flush();
            writer.close();
            os.close();

            int responseCode=urlConnection.getResponseCode();
            rezultat = (responseCode == HttpURLConnection.HTTP_OK);

        } catch (Exception e) { // Jeśli nie ma połączenia z internetem.
            rezultat = false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return rezultat;
    }
}
