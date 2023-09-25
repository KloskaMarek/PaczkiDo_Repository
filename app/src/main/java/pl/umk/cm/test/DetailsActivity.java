package pl.umk.cm.test;

import static pl.umk.cm.test.Utils.getOdbiorcaId;
import static pl.umk.cm.test.Utils.saveFile;
import static pl.umk.cm.test.Utils.fileExist;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;

import pl.umk.cm.test.databinding.ActivityMainBinding;

public class DetailsActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        String paczkaId;
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            paczkaId = extras.getString("paczkaId");
            //Log.i("Marek",paczkaId);
            TextView txtPaczkaId = findViewById(R.id.txtPaczkaId);
            txtPaczkaId.setText(paczkaId);
        } else {
            paczkaId = "brak";
        }
    }

    public String url = "*";

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
                if(result.getContents() == null) {
                    Toast.makeText(DetailsActivity.this, "Rezygnacja ze skanowania", Toast.LENGTH_LONG).show();
                } else {
                    url = result.getContents();
                    int begin = url.lastIndexOf("/");
                    int end = url.length();
                    String paczkaId = url.substring(begin+1, end);
                    Intent intent = new Intent(this, PaczkaActivity.class);
                    intent.putExtra("paczkaId", paczkaId);
                    startActivity(intent);
                }
            });

    public void btnDoSkanuj_onClick(View v){
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("skanuj QR kod paczki");
        options.setCameraId(0);
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(true); //umożliwia przesłanie wyniku w Intent
        options.setOrientationLocked(false);
        barcodeLauncher.launch(options);
    }

    public void btnBezKodu_onClick(View v){
        Context context = getBaseContext();
        String podanyKodOdbioru = "-1";
        TextView txtPaczkaId = findViewById(R.id.txtPaczkaId);
        String paczkaId = txtPaczkaId.getText().toString();
        try {
            if(Utils.fileExist("Wyjazd", context)){
                StringBuilder sb = new StringBuilder();
                sb = Utils.loadFile("Wyjazd", context);
                String paczkiStringJSON = sb.toString();
                String odbiorcaId = "338"; //wydano bez kodu odbioru
                ArrayList<PaczkaView> arrayList = zapiszStatusPaczki(paczkiStringJSON, paczkaId, podanyKodOdbioru, "40", odbiorcaId);
                Intent intent = new Intent(this, UwagaActivity.class);
                intent.putExtra("t_poprawnie", "WYDANA");
                intent.putExtra("t_1", "Nie podano");
                intent.putExtra("t_2", "kod odbioru paczki");
                startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(DetailsActivity.this, "Błąd zapisu w pliku.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void btnOdebrana_onClick(View v){
        Context context = getBaseContext();
        TextView txtKodOdbioru = findViewById(R.id.txtKodOdbioru);
        String podanyKodOdbioru = txtKodOdbioru.getText().toString();
        TextView txtPaczkaId = findViewById(R.id.txtPaczkaId);
        String paczkaId = txtPaczkaId.getText().toString();
        try {
            if(Utils.fileExist("Wyjazd", context)){
                StringBuilder sb = new StringBuilder();
                sb = Utils.loadFile("Wyjazd", context);
                String paczkiStringJSON = sb.toString();
                String odbiorcaId = getOdbiorcaId( paczkiStringJSON, paczkaId, podanyKodOdbioru);
                int znalezioneId = Integer.parseInt(odbiorcaId);
                if (znalezioneId > 0) {
                    ustawWPlikuStatusOdebrana(paczkiStringJSON, paczkaId, podanyKodOdbioru, "40", odbiorcaId);
//                    Intent intent = new Intent(this, PaczkaActivity.class);
//                    startActivity(intent);
                } else {
                    Intent intent = new Intent(this, UwagaActivity.class);
                    intent.putExtra("t_blednie", "BŁĄD!");
                    intent.putExtra("t_1", "Nieprawidłowy");
                    intent.putExtra("t_2", "kod odbioru paczki");
                    intent.putExtra("t_3", "Proszę odczytać kod odbioru w wiadomości e-mail wysłanej wczoraj z Kancelarii CM.");
                    startActivity(intent);
                }
            }
        } catch (Exception e) {
            Toast.makeText(DetailsActivity.this, "Wyjątek przy zapisie statusu odebrana!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public ArrayList<PaczkaView> zapiszStatusPaczki(String stringPaczki, String paczkaId, String podanyKodOdbioru, String nowyStatus, String odbiorcaId){
        Context context = getBaseContext();
        ArrayList<PaczkaView> arrayList = new ArrayList<>();
        String poprawny = stringPaczki.substring(0,9).trim();
        if(!poprawny.equals("Exception")) {
            String pusty = stringPaczki.substring(0,12).trim();
            if(!pusty.equals("{\"Paczki\":]}")) {
                // Pierwsze skanowanie paczki zmienia status na "na_samochodzie".
                arrayList = Utils.updateArrayList(arrayList, stringPaczki, paczkaId, podanyKodOdbioru, nowyStatus, odbiorcaId);
                String updatedString = Utils.setJsonContentsFileWyjazd(arrayList);
                saveFile(updatedString, "Wyjazd", context);
            }
        }
        return arrayList;
    }

    public void ustawWPlikuStatusOdebrana(String stringPaczki, String paczkaId, String podanyKodOdbioru, String nowyStatus, String odbiorcaId) {
        //progressDialog.dismiss();
        Context context = getBaseContext();

        ArrayList<PaczkaView> arrayList = zapiszStatusPaczki(stringPaczki, paczkaId, podanyKodOdbioru, nowyStatus, odbiorcaId);

        if(arrayList.size() > 0){
            Intent intent = new Intent(getApplicationContext(), UwagaActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("t_poprawnie", "POPRAWNY");
            intent.putExtra("t_1", "kod odbioru");
            intent.putExtra("t_2", "Zapisano datę i odbiorcę.");
            startActivity(intent);
        } else {
            Intent intent = new Intent(getApplicationContext(), UwagaActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("t_blednie", "BŁĄD");
            intent.putExtra("t_1", "Ustaw 'odebrana'");
            intent.putExtra("t_2", "brak pliku");
            startActivity(intent);
        }
    }


}