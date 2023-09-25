package pl.umk.cm.test;

import static pl.umk.cm.test.Utils.loadFile;

import android.content.Context;
import android.content.Intent;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public String url = "*";

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
                    Toast.makeText(MainActivity.this, "Rezygnacja ze skanowania", Toast.LENGTH_LONG).show();
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

    public void btnSkanuj_onClick(View view){
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("skanuj QR kod paczki");
        options.setCameraId(0);
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(true); //umożliwia przesłanie wyniku w Intent
        options.setOrientationLocked(false);
        barcodeLauncher.launch(options);
    }

    public void btnLista_onClick(View view){
        Intent intent = new Intent(this, PaczkaActivity.class);
        startActivity(intent);
    }

    public void btnExport_onClick(View view){
        int wynik = 0;
        Context context = getBaseContext();
        if(Utils.fileExist("Wyjazd", context)){
            StringBuilder sb = new StringBuilder();
            sb = loadFile("Wyjazd", getBaseContext());
            String zPliku = sb.toString();
            String paczkiStringJSON = zPliku;
            try {
                if(Utils.updateWyjazd(paczkiStringJSON, getApplicationContext())){
                    wynik = 2; //Tutaj JEST aktualizacja bazy danych!
                } else {
                    wynik = -3;
                }
            } catch (Exception e) {
                wynik = -2;
            }
        } else {
            wynik = -1;
        }

        Intent intent = new Intent(this, UwagaActivity.class);
        switch (wynik){
            case 2:
            {
                intent.putExtra("t_poprawnie", "POPRAWNIE");
                intent.putExtra("t_1", "Przesłano");
                intent.putExtra("t_2", "dane na serwer");
            }
            break;
            case -1:
            {
                intent.putExtra("t_blednie", "BŁĄD!");
                intent.putExtra("t_2", "Export danych");
                intent.putExtra("t_3", "W smartfonie brak pliku z danymi!");
            }
            break;
            case -2:
            {
                intent.putExtra("t_blednie", "BŁĄD!");
                intent.putExtra("t_2", "Export danych");
                intent.putExtra("t_3", "Brak dostępu do internetu\n\n 1. Musisz być w zasięgu dowolnej sieci telefonii komórkowej lub WiFi. \n\n2. Uruchom najpierw program Forti Client, a potem uruchom opcję Export w aplikacji PaczkiDo.\n\n(wyjątek w procedurze updateWyjazd)");
            }
            break;
            case -3:
            {
                intent.putExtra("t_blednie", "BŁĄD!");
                intent.putExtra("t_2", "Export danych");
                intent.putExtra("t_3", "Brak dostępu do internetu\n i brak pliku z danymi!\n\n 1. Musisz być w zasięgu dowolnej sieci telefonii komórkowej lub WiFi. \n\n2. Uruchom najpierw program Forti Client, a potem PaczkiDo.");
            }
            break;
            case 0:
            default:
            {
                intent.putExtra("t_blednie", "BŁĄD!");
                intent.putExtra("t_2", "Export danych");
                intent.putExtra("t_3", "To jest kod błędu ustawiony na wejściu do procedury.");
            }
            break;
        }
        startActivity(intent);
    }

    public void btnWersja_onClick(View view){

        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String versionTime = formatter.format(BuildConfig.BUILD_TIME);

        Intent intent = new Intent(this, UwagaActivity.class);
        intent.putExtra("t_1", "wersja "+versionName);
        intent.putExtra("t_2", "z dnia "+versionTime);
        intent.putExtra("t_3", "Aplikacja umożliwia wpisanie i sprawdzenie kodu odbioru paczki przy wydaniu."
                +" Tymczasowo możliwe jest też wydanie paczki bez podania kodu odbioru."
                +" Opcja ta zostanie usunięta po okresie wdrożenia."
                +"\n\nNależy pamiętać o uruchomieniu opcji Export danych na serwer"
                +" po powrocie do kampusa Collegium Medicum.");
        startActivity(intent);
    }
}