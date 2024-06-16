package com.samsung.health.multisensortracking;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

//StartActivity gestiona la pantalla de inicio de la aplicación con opciones para enviar datos o iniciar un temporizador de cuenta regresiva.
public class StartActivity extends AppCompatActivity {
    private static final String APP_TAG = "MainActivity";

    private TextView textView;
    private Button but_enviar;
    private Button but_dormir;
    private File logs_start;
    private String fileString_log_start = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Inicializar componentes de la interfaz de usuario
        textView = findViewById(R.id.textView);
        but_dormir = findViewById(R.id.but_sleep);
        but_enviar = findViewById(R.id.but_send);
        textView.setText("    ");

        Log.i(APP_TAG, "ON CREATE START ACTIVITY");

        createFile();

        // Registrar la marca de tiempo en la creación del archivo
        SimpleDateFormat time_now = new SimpleDateFormat("HH_mm_ss");
        String timestamp = time_now.format(new Date());
        fileString_log_start += timestamp + ", " + "onCreate() START ACTIVITY" + "\n";
        FileWriters(fileString_log_start, logs_start);
        fileString_log_start = "";
    }

    //Activa FirebaseActivity para enviar datos.
    public void sendData(View v) {
        Intent mainIntent = new Intent(StartActivity.this, FirebaseActivity.class);
        startActivity(mainIntent);
    }

    //Inicia el temporizador de cuenta regresiva en la UI.
    public void startSleep(View v) {
        but_dormir.setText("INICIANDO ...");
        but_enviar.setText("");
        but_dormir.setEnabled(false);
        but_enviar.setEnabled(false);

        setTimer();
    }

    //Escribe la cadena especificada en el archivo dado.
    public void FileWriters(String str, File file) {
        try {
            if (!file.exists()) {
                file.createNewFile();
                try (FileOutputStream fOut = new FileOutputStream(file, true)) {
                    fOut.write("timestamp, activity, event\n".getBytes());
                }
            }
            try (FileOutputStream fOut = new FileOutputStream(file, true)) {
                fOut.write(str.getBytes());
                fOut.flush();
            }
        } catch (IOException e) {
            Log.e("Exception", "Error al escribir el archivo");
            fileString_log_start += new SimpleDateFormat("HH_mm_ss").format(new Date()) + ", " + "start" + ", " + "excepción de escritura de archivo" + "\n";
            FileWriters(fileString_log_start, logs_start);
            fileString_log_start = "";
        }
    }

    //Registra eventos del ciclo de vida.
    @Override
    protected void onStart() {
        super.onStart();
        logLifecycleEvent("onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        logLifecycleEvent("onResume()");
        resetUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        logLifecycleEvent("onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        logLifecycleEvent("onStop()");
    }

    //Establece un temporizador de cuenta regresiva de 10 segundos en la UI.
    public void setTimer() {
        new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                // Formatear el tiempo para estar en formato de 2 dígitos
                NumberFormat f = new DecimalFormat("00");
                long hour = (millisUntilFinished / 3600000) % 24;
                long min = (millisUntilFinished / 60000) % 60;
                long sec = (millisUntilFinished / 1000) % 60;
                textView.setText(f.format(hour) + ":" + f.format(min) + ":" + f.format(sec));
            }

            public void onFinish() {
                finishTimer();
            }
        }.start();
    }

    //Maneja la finalización del temporizador, reproduce una alarma, registra el evento y comienza MainActivity.
    private void finishTimer() {
        Log.i(APP_TAG, "fin del temporizador en ACTIVIDAD");
        playAlarm();
        logLifecycleEvent("fin del temporizador");
        textView.setText("00:00:00");
        startActivity(new Intent(StartActivity.this, MainActivity.class));
    }

    //Reproduce el tono de alarma predeterminado.
    private void playAlarm() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Crea un archivo de registro en el almacenamiento externo.
    private void createFile() {
        SimpleDateFormat sdf_filename = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
        String file_name = sdf_filename.format(new Date());
        File path = new File(Objects.requireNonNull(this.getExternalFilesDir(null)).getAbsolutePath() + "/temp_data");

        if (!path.exists()) {
            path.mkdirs();
        }
        logs_start = new File(path, "LOGS_START_" + file_name + ".csv");
        logLifecycleEvent("archivo creado");
    }

    //Registra eventos del ciclo de vida de la actividad y los escribe en un archivo.
    private void logLifecycleEvent(String event) {
        SimpleDateFormat time_now = new SimpleDateFormat("HH_mm_ss");
        String timestamp = time_now.format(new Date());
        fileString_log_start += timestamp + ", " + "start" + ", " + event + "\n";
        FileWriters(fileString_log_start, logs_start);
        fileString_log_start = "";
    }

    //Restablece los botones y el texto de la UI cuando se reanuda la actividad.
    private void resetUI() {
        but_enviar.setText("ENVIAR");
        but_dormir.setText("DORMIR");
        textView.setText("");
        but_enviar.setEnabled(true);
        but_dormir.setEnabled(true);
    }
}