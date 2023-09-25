package pl.umk.cm.test;

import static pl.umk.cm.test.Utils.SERVER;
import static pl.umk.cm.test.Utils.auth;
import static pl.umk.cm.test.Utils.fileExist;
import static pl.umk.cm.test.Utils.fileRename;
import static pl.umk.cm.test.Utils.loadFile;
import static pl.umk.cm.test.Utils.saveFile;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

public class PaczkaActivity extends AppCompatActivity {
    ProgressDialog progressDialog;
    String paczkaId = "-1";
    String wyjazdId = "-1";
    Boolean dzisiejszyWyjazd = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paczka);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            paczkaId = extras.getString("paczkaId");
        }
        MyAsyncTasks myAsyncTasks = new MyAsyncTasks();
        myAsyncTasks.execute();
    }

    public class MyAsyncTasks extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(PaczkaActivity.this);
            progressDialog.setMessage("Proszę czekać...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected String doInBackground(String... params) {
            InputStream in;
            InputStreamReader isw;
            int data;
            Context context = getBaseContext();
            String paczkiStringJSON = "";
            try {
                if("-1" == paczkaId){
                    // czy jest dzisiejszy plik Wyjazd?
                    if(fileExist("Wyjazd",context)) {
                        StringBuilder sb = new StringBuilder();
                        sb = loadFile("Wyjazd", getBaseContext());
                        paczkiStringJSON = sb.toString();
                    } else {
                        Intent intent = new Intent(PaczkaActivity.this, UwagaActivity.class);
                        intent.putExtra("t_blednie", "BRAK DANYCH");
                        intent.putExtra("t_1", "Zeskanuj");
                        intent.putExtra("t_2", "pierwszą paczkę");
                        startActivity(intent);

                        return "Exception: Brak danych! Zeskanuj pierwszą paczkę.";
                    }
                }
                HttpURLConnection urlConnection = null;
                try {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);

                    String linkPaczkaWyjazd = SERVER + "/Paczki/PaczkaWyjazd/"+paczkaId;
                    URL urlPaczkaWyjazd = new URL(linkPaczkaWyjazd);
                    String coded = Base64.encodeToString(auth.getBytes(StandardCharsets.UTF_8),Base64.NO_WRAP);
                    String authHeaderValue = "Basic "+coded;

                    urlConnection = (HttpURLConnection) urlPaczkaWyjazd.openConnection();
                    urlConnection.setRequestProperty("Authorization", authHeaderValue);
                    urlConnection.setReadTimeout(15000);
                    urlConnection.setConnectTimeout(10000);
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    in =  urlConnection.getInputStream();
                    isw = new InputStreamReader(in);
                    data = isw.read();
                    while (data != -1) {
                        paczkiStringJSON += (char) data;
                        data = isw.read();
                    } // Zmienna paczkiStringJSON zawiera wszystkie dane o paczkach z bieżącego wyjazdu.

                    // Po zeskanowaniu kodu pierwszej paczki zapisywany jest plik "Wyjazd".
                    // Przy kolejnym skanowaniu program korzysta z danych zapisanych w pliku "W".
                    if(paczkiStringJSON.contentEquals("{\"Paczki\":]}")){ // było :[]}
                        Intent intent = new Intent(PaczkaActivity.this, UwagaActivity.class);
                        startActivity(intent);
                        return "Exception: Brak danych! Zeskanuj pierwszą paczkę.";
                    } else {

                        // Sprawdza, czy wyjazd zarejestrowany jest na dzień dzisiejszy.
                        // Paczka może być przypisana do wyjazdu z innego dnia. Wtedy nie ma zapisywać pliku na smartfonie.
                        saveFile(paczkiStringJSON, "Test", context);
                        StringBuilder sb = new StringBuilder();
                        sb = loadFile("Test", getBaseContext());
                        paczkiStringJSON = sb.toString();
                        ArrayList<PaczkaView> arrayList = new ArrayList<>();
                        //Poniżej nie aktualizuje statusu paczki, tylko ładuję dane do tablicy.
                        arrayList = Utils.updateArrayList(arrayList, paczkiStringJSON, paczkaId, "", "00", "0");
                        wyjazdId = Utils.getWyjazdId(arrayList);
                        //TODO Pobrać datę planowanego wyjazdu i porównać z datą bieżącą.
                        String dzisiaj = LocalDateTime.now().toString().substring(0,10);
                        String wyjazdData = Utils.getWyjazdData(arrayList);
                        String d = wyjazdData.substring(0,2);
                        String m = wyjazdData.substring(3,5);
                        String r = wyjazdData.substring(6,10);
                        String wyjazdDataFormat = r+"-"+m+"-"+d;
                        if(!wyjazdDataFormat.equals(dzisiaj)){
                            dzisiejszyWyjazd = false;
                            ContextWrapper cw = new ContextWrapper(context);
                            cw.deleteFile("Test");
                            return "Exception: Ta paczka została zaplanowana do doręczenia dnia "+wyjazdData;
                        } else {
                            dzisiejszyWyjazd = true;
                        }
                    }
                    if(!fileExist("Wyjazd", context)){
                        //TODO wstawić warunek: jeśli data wyjazdu przypisanego do paczki nie jest dzisiejsza, to nie zapisuj pliku.
                        saveFile(paczkiStringJSON, "Wyjazd", context);
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb = loadFile("Wyjazd", getBaseContext());
                        paczkiStringJSON = sb.toString();
                    }
                } catch (Exception e) { // Jeśli nie ma połączenia z internetem.
                    if(fileExist("Wyjazd", context)) {
                        paczkiStringJSON = loadFile("Wyjazd",getBaseContext()).toString();
                    } else {
                        return "Exception: " + e.getMessage();
                    }
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            } catch (Exception e) {
                return "Exception: " + e.getMessage();
            }
            return paczkiStringJSON;
        }

        @Override
        protected void onPostExecute(String paczkiStringJSON) {
            progressDialog.dismiss();
            Context context = getBaseContext();

            //TODO zunifikować zapis razem z DetailsActivity

            ArrayList<PaczkaView> arrayList = new ArrayList<>();
            String wyjatek = paczkiStringJSON.substring(0,9).trim();
            if(!wyjatek.equals("Exception")) {
                String pusty = paczkiStringJSON.substring(0,12).trim();
                if(!pusty.equals("{\"Paczki\":]}")) {
                    // Pierwsze skanowanie paczki zmienia status na "na_samochodzie".
                    arrayList = Utils.updateArrayList(arrayList, paczkiStringJSON, paczkaId, "", "30", "0");
                    wyjazdId = Utils.getWyjazdId(arrayList);
                    if(wyjazdId.equals("0")) {
                        Intent intent = new Intent(getApplicationContext(), UwagaActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("t_blednie", "BŁĄD");
                        intent.putExtra("t_1", "Brak WyjazdId");
                        intent.putExtra("t_3", "Błąd przy odczycie listy paczek. Brak możliwości zapisu statusu 'na_samochodzie' dla PaczkaId="+paczkaId.toString());
                        startActivity(intent);
                    } else {
                        String updatedString = Utils.setJsonContentsFileWyjazd(arrayList);
                        saveFile(updatedString, "Wyjazd", context);
                        Utils.updateWyjazd(updatedString, getApplicationContext()); //Tutaj JEST aktualizacja bazy danych!
                    }
                    if(arrayList.size() > 0){
                        // wyświetlenie listy paczek
                        PaczkaViewAdapter adapter = new PaczkaViewAdapter(PaczkaActivity.this, arrayList);
                        ListView numbersListView = findViewById(R.id.listView);
                        numbersListView.setClickable(true);
                        numbersListView.setAdapter(adapter);
                    } else {
                        Intent intent = new Intent(getApplicationContext(), UwagaActivity.class);
                        intent.putExtra("t_blednie", "BRAK DANYCH");
                        intent.putExtra("t_3", "Brak dostępu do internetu\n i brak pliku z danymi!\n\n 1. Musisz być w zasięgu dowolnej sieci telefonii komórkowej lub WiFi. \n\n2. Uruchom najpierw program Forti Client, a potem PaczkiDo.");
                        startActivity(intent);
                    }
                }
            } else {
                // wyjątek
                Intent intent = new Intent(getApplicationContext(), UwagaActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if(!dzisiejszyWyjazd) {
                    intent.putExtra("t_blednie", "PACZKA");
                    intent.putExtra("t_1", "z innego dnia");
                    intent.putExtra("t_3", paczkiStringJSON.substring(11));
                } else {
                    intent.putExtra("t_blednie", "BRAK DANYCH");
                    intent.putExtra("t_3", "Brak dostępu do internetu\n i brak pliku z danymi!\n\n 1. Musisz być w zasięgu dowolnej sieci telefonii komórkowej lub WiFi. \n\n2. Uruchom najpierw program Forti Client, a potem PaczkiDo.");
                }
                startActivity(intent);
            }
        }
    }

}