package pl.umk.cm.test;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class PaczkaViewAdapter extends ArrayAdapter<PaczkaView> {

    private Context c;

    public PaczkaViewAdapter(@NonNull Context context, ArrayList<PaczkaView> arrayList) {
        super(context, 0, arrayList);
        this.c = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_view,parent,false);
        }
        PaczkaView currentNumberPosition = getItem(position);

        androidx.appcompat.widget.AppCompatButton btnPaczka = convertView.findViewById(R.id.btnPaczka);
        String paczkaId = currentNumberPosition.getPaczkaId();
        btnPaczka.setText(paczkaId);

        TextView textView1 = convertView.findViewById(R.id.txtAdresNazwa);
        textView1.setText(currentNumberPosition.getAdresNazwa());

        TextView textView2 = convertView.findViewById(R.id.txtJednostka);
        textView2.setText(currentNumberPosition.getJednostkaNazwa());

        TextView textView3 = convertView.findViewById(R.id.txtGabaryt);
        textView3.setText(currentNumberPosition.getGabarytNazwa());

        TextView textView4 = convertView.findViewById(R.id.txtStatus);
        textView4.setText(currentNumberPosition.getStatusNazwa());

        String strStatus = currentNumberPosition.getStatusNazwa();

        btnPaczka.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String paczkaId = (String) btnPaczka.getText();
                android.content.Intent intent = new Intent(c, DetailsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("paczkaId",paczkaId);
                if(strStatus.contentEquals("w_samochodzie")) {
                    c.startActivity(intent);
                }
            }
        });

        String stat = currentNumberPosition.getStatusNazwa();
        switch (stat){
            case "nowa":
            {
                btnPaczka.setBackgroundResource(R.drawable.btn_paczka_plan);
                break;
            }
            case "zaplanowana":
            {
                btnPaczka.setBackgroundResource(R.drawable.btn_paczka_plan);
                break;
            }
            case "w_samochodzie":
            {
                btnPaczka.setBackgroundResource(R.drawable.btn_paczka_auto);
                break;
            }
            case "odebrana":
            {
                btnPaczka.setBackgroundResource(R.drawable.btn_paczka_odebrana);
                break;
            }
            case "wycofana":
            {
                btnPaczka.setBackgroundResource(R.drawable.btn_paczka_wycofana);
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + stat);
        }

        return convertView;
    }
}

