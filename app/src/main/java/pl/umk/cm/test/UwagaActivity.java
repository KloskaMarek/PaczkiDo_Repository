package pl.umk.cm.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class UwagaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uwaga);

        Bundle extras = getIntent().getExtras();
        if(null != extras ) {
            TextView txtBlednie = findViewById(R.id.txtBlednie);
            TextView txtPoprawnie = findViewById(R.id.txtPoprawnie);
            TextView txt0 = findViewById(R.id.txt0);
            TextView txt1 = findViewById(R.id.txt1);
            TextView txt2 = findViewById(R.id.txt2);
            TextView txt3 = findViewById(R.id.txt3);
            String t_blednie = extras.getString("t_blednie");
            if(null != t_blednie && t_blednie.length()>0) {
                txtBlednie.setText(t_blednie);
                txtBlednie.setVisibility(View.VISIBLE);
            } else {
                txtBlednie.setVisibility(View.INVISIBLE);
            }
            String t_poprawnie = extras.getString("t_poprawnie");
            if(null != t_poprawnie && t_poprawnie.length()>0) {
                txtPoprawnie.setText(t_poprawnie);
                txtPoprawnie.setVisibility(View.VISIBLE);
            } else {
                txtPoprawnie.setVisibility(View.INVISIBLE);
            }
            String t_0 = extras.getString("t_0");
            if(null != t_0 && t_0.length()>0){
                txt0.setText(t_0);
                txt0.setVisibility(View.VISIBLE);
            } else {
                txt0.setVisibility(View.INVISIBLE);
            }
            String t_1 = extras.getString("t_1");
            if(null != t_1 && t_1.length()>0){
                txt1.setText(t_1);
                txt1.setVisibility(View.VISIBLE);
            } else {
                txt1.setVisibility(View.INVISIBLE);
            }
            String t_2 = extras.getString("t_2");
            if(null != t_2 && t_2.length()>0){
                txt2.setText(t_2);
                txt2.setVisibility(View.VISIBLE);
            } else {
                txt2.setVisibility(View.INVISIBLE);
            }
            String t_3 = extras.getString("t_3");
            if(null != t_3 && t_3.length()>0){
                txt3.setText(t_3);
                txt3.setVisibility(View.VISIBLE);
            } else {
                txt3.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void btnUwagaDoMenu_onClick(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}