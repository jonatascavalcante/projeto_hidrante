package com.hidrantes.cbmmg.hidrantes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import util.MaskEditUtil;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    OkHttpClient client = new OkHttpClient();
    Response response = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText nBm = findViewById((R.id.nBm));
        nBm.addTextChangedListener(MaskEditUtil.mask(nBm, "###.###-#"));

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
    }

    public void logar(View v) {

        final ProgressDialog progressDialog = ProgressDialog.show(this, "Entrando no sistema", "Carregando dados pessoais", false, false);

        new Thread(new Runnable() {

            EditText nBm = findViewById(R.id.nBm);
            EditText senha = findViewById(R.id.senha);

            String numeroBM;
            String password;

            @Override
            public void run() {

                numeroBM = MaskEditUtil.unmask(nBm.getText().toString());
                password = senha.getText().toString();

                String url = "http://intranet.bombeiros.mg.gov.br/api/auser";
                String credential = Credentials.basic(numeroBM, password);
                Request request = new Request.Builder().header("Authorization", credential).url(url).build();

                try {
                    response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() { //Aqui seria o: On post execute da AsyncTask. HANDLER implicito, portanto, assim que terminar o run de cima(na Thread paralela, ele ativa essa)

                    @Override
                    public void run() {
                        progressDialog.dismiss();

                        if (response == null) {
                            Toast.makeText(LoginActivity.this, "Servidor está fora do ar. Tente novamente mais tarde", Toast.LENGTH_SHORT);
                        } else {
                            if (response.isSuccessful()) {
                                editor.putString("nbm", numeroBM);
                                editor.putString("password", password);
                                editor.apply();
                                realizaLogin(true);
                            } else {
                                realizaLogin(false);
                            }
                        }
                    }
                });
            }
        }).start();
    }

    public void realizaLogin(boolean usuarioValido) {
        if(usuarioValido) {
            Intent it = new Intent(this, MapsActivity.class);
            startActivity(it);
        } else {
            this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "Login inválido ou não autorizado.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
