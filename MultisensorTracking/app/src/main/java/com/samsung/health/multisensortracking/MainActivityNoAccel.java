/*
 * Copyright 2022 Samsung Electronics Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.samsung.health.multisensortracking;

import static android.content.pm.PackageManager.PERMISSION_DENIED;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.os.PowerManager;

import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.wear.input.WearableButtons;

import com.samsung.android.service.health.tracking.HealthTrackerException;
import com.samsung.android.service.health.tracking.HealthTrackingService;
import com.samsung.health.multisensortracking.databinding.ActivityMainBinding;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

// Esta clase es la actividad principal que gestiona la interacción con los sensores de salud y realiza seguimiento de mediciones como frecuencia cardíaca, SpO2 y temperatura. Sin la aceleracion.

public class MainActivityNoAccel extends FragmentActivity {

    // Constante para etiquetas de log
    private final static String APP_TAG = "MainActivity";

    // Permisos necesarios para la aplicación
    private final String[] permissions = {"android.permission.ACTIVITY_RECOGNITION, android.permission.BODY_SENSORS_BACKGROUND,android.permission.BODY_SENSORS"};

    // Estado de la medición
    public boolean isMeasurementRunning = false;

    // Gestor de conexiones
    private ConnectionManager connectionManager;

    // Listeners para distintos sensores
    private HeartRateListener heartRateListener = null;
    private SpO2Listener spO2Listener = null;
    private TemperatureListener tempListener = null;

    // Variables para almacenar el estado previo y los valores de los sensores
    private int previousStatus = SpO2Status.INITIAL_STATUS;
    public String hr_value = "";
    public String spo2_value = "";
    public String temp_value ="";
    public String temp_value_ambient ="";
    public String hribi_value = "";
    public int acx=0;
    public int acy=0;
    public int acz=0;
    public int flag =0;
    public int flag2 =0;
    public int data_counter = 0;


    // Formateadores de fecha y variables para archivos
    SimpleDateFormat sdf;
    String fileString = "";
    SimpleDateFormat sdf_filename;
    File path;
    File file_sleep;
    File logs;
    String fileString_log="";

    // Servicio de seguimiento de salud
    private HealthTrackingService healthTrackingService = null;

    // Botón de inicio
    private Button butStart;

    // WakeLock para mantener la pantalla encendida
    protected PowerManager.WakeLock mWakeLock;


    // Inicia la actividad. Configura la vista, verifica los botones disponibles, solicita permisos y crea archivos para guardar datos.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(APP_TAG, "MAIN ACTIVITY oncreate ");

        // Enlazar la vista usando el binding
        final ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Comprobar si hay botones multifunción disponibles
        int count = WearableButtons.getButtonCount(this);
        if (count > 1) {
            // There are multifunction buttons available
            Log.i(APP_TAG, "There are multifunction buttons available: "+ count);

        }

        // Comprobar la disponibilidad del botón KEYCODE_STEM_1
        WearableButtons.ButtonInfo buttonInfo =
                WearableButtons.getButtonInfo(this, KeyEvent.KEYCODE_STEM_1);
        if (buttonInfo == null) {
            // KEYCODE_STEM_1 is unavailable
            Log.i(APP_TAG, "KEYCODE_STEM_1 is unavailable ");
        } else {
            // KEYCODE_STEM_1 is present on the device
            Log.i(APP_TAG, " KEYCODE_STEM_1 is present on the device\n ");
        }

        // Comprobar la disponibilidad del botón KEYCODE_STEM_PRIMARY
        WearableButtons.ButtonInfo buttonInfo4=
                WearableButtons.getButtonInfo(this, KeyEvent.KEYCODE_STEM_PRIMARY);
        if (buttonInfo4 == null) {
            // KEYCODE_STEM_1 is unavailable
            Log.i(APP_TAG, "KEYCODE_STEM_PRIMARY is unavailable ");
        } else {
            // KEYCODE_STEM_1 is present on the device
            Log.i(APP_TAG, " KEYCODE_STEM_PRIMARY is present on the device\n ");
        }

        // Crear archivos para guardar datos
        createFile();

        // Registrar el evento onCreate en los logs
        SimpleDateFormat time_now = new SimpleDateFormat("HH_mm_ss");
        String timestamp = time_now.format(new Date());
        fileString_log= fileString_log + timestamp+", "+"main"+", "+"onCreate"+"\n";
        FileWritersLog(fileString_log,logs);
        fileString_log = "";

        // Inicializar el estado de la medición
        isMeasurementRunning = false;

        // Solicitar permisos si no están concedidos
        // butStart = binding.butStart;
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), "android.permissions.BODY_SENSORS") == PackageManager.PERMISSION_DENIED)
            requestPermissions(new String[]{Manifest.permission.BODY_SENSORS}, 0);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), "android.permissions.ACTIVITY_RECOGNITION") == PackageManager.PERMISSION_DENIED)
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);

        //https://developer.android.com/reference/android/os/PowerManager
    }

    /*    @Override
        public void onBackPressed() {
    // super.onBackPressed();
    // Not calling **super**, disables back button in current screen.
            Log.i(APP_TAG, "*********ON BACK PRESSEDD 2!!************ ");

        }*/

    // Método para manejar la selección de ítems en el menú. Devuelve True si el ítem seleccionado es el de inicio, de lo contrario llama al método padre.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.i(APP_TAG, "********HOOME!************ "+ item.getItemId());
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Método para manejar eventos de teclas. Devuelve True si se maneja el evento de la tecla, de lo contrario llama al método padre.
    @Override
    // Activity
    public boolean onKeyDown(int keyCode, KeyEvent event){
        Log.i(APP_TAG, "onkeydown called ");

        // Manejar eventos de botones específicos
        //   if (event.getRepeatCount() == 0) {
        if (keyCode == KeyEvent.KEYCODE_STEM_1) {
            Log.i(APP_TAG, "BOTON 1 ");
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_STEM_2) {
            Log.i(APP_TAG, "BOTON 2 ");
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_STEM_3) {
            Log.i(APP_TAG, "BOTON 3 ");
            return true;
        }
        //  }
        return super.onKeyDown(keyCode, event);
    }

    // Método para iniciar el seguimiento del sueño. Configura el gestor de conexión y adquiere el WakeLock para mantener la pantalla encendida.
    public void startSleep(){
        // Crear el gestor de conexión y adquirir el WakeLock
        createConnectionManager();
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,"myapp:mywakelocktag");
        // this.mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"myapp:mywakelocktag");

        //android:keepScreenOn="true"
        // Acquire wake lock


    }

    // Método para escribir logs en el archivo especificado.
    public void FileWritersLog(String str, File file){
        // Escribir logs en el archivo
        try {
            if(!file.exists()){
                file.createNewFile();
                FileOutputStream fOut = new FileOutputStream(file, true);
                fOut.write(" timestamp, activity, event \n".getBytes());
//                fOut.flush();
                fOut.close();
            }
            FileOutputStream fOut = new FileOutputStream(file, true);
            fOut.write(str.getBytes());
            fOut.flush();
            fOut.close();

        } catch (IOException e){
            Log.e("Exception", "File write failed");
        }
    }

    // Método para escribir datos en el archivo especificado.
    public void FileWriters(String str, File file){
        // Escribir datos en el archivo
        try {
            if(!file.exists()){
                file.createNewFile();
                FileOutputStream fOut = new FileOutputStream(file, true);
                fOut.write(" timestamp, hr, ibi, sop2, temp, ambientT, sensor \n".getBytes());
//                fOut.flush();
                fOut.close();
            }
            FileOutputStream fOut = new FileOutputStream(file, true);
            fOut.write(str.getBytes());
            fOut.flush();
            fOut.close();

        } catch (IOException e){

            Log.e("Exception", "File write failed");
            SimpleDateFormat time_now = new SimpleDateFormat("HH_mm_ss");
            String timestamp = time_now.format(new Date());
            fileString_log= fileString_log + timestamp+", "+"main"+", "+"file write exception"+"\n";
            FileWritersLog(fileString_log,logs);
            fileString_log = "";
        }
    }

    // Método para manejar el resultado de actividades iniciadas.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //  Log.i("TAG", "onActivityResult requestCode = " + requestCode + " resultCode = " + resultCode);

        // Manejar el resultado de actividades iniciadas
        if (requestCode == 100) {
            if (resultCode == -1) {
                //setUp();

            } else {
                finish();
            }
        }
    }

    // Método para crear archivos necesarios para la aplicación.
    private void createFile() {
        // Crear archivos para guardar datos y logs
        sdf_filename = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
        String file_name = sdf_filename.format(new Date());

        path = new File( Objects.requireNonNull(this.getExternalFilesDir(null)).getAbsolutePath() + "/temp_data");

        if (!path.exists()){
            path.mkdirs();
        }
        file_sleep = new File(path, "data_sleep_NA_"+file_name+".csv");
        logs = new File(path, "LOGS_"+file_name+".csv");

    }

    // Método para cerrar la aplicación de manera ordenada. Detiene los trackers y finaliza la actividad.
    public void closeApplication() {
        // Detener los trackers y cerrar la aplicación
        stopTrackers();
        // Finalmente, cierra la actividad
        //finish();
    }

    // Método para reiniciar el tracker de SpO2. Detiene el tracker actual e inicia uno nuevo.
    public void restart_spo2tracker(){
        // Reiniciar el tracker de SpO2
        if (spO2Listener!=null) {
            spO2Listener.stopTracker();
        }

        previousStatus = SpO2Status.INITIAL_STATUS;
        Log.i(APP_TAG, "SP02 - START TRACKER");
        connectionManager.initSpO2(spO2Listener);

        spO2Listener.startTracker();
    }

    // Objeto Timer para manejar tareas programadas
    private Timer timer;

    // Método para iniciar un Timer que reinicia el tracker de SpO2 cada 60 segundos.
    public void startTimer() {
        // Crear y programar un Timer para reiniciar el tracker de SpO2 cada 60 segundos
        timer = new Timer();

        Log.i(APP_TAG, "-----START TIMER ------");

        // Crea un TimerTask para definir la tarea a realizar
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.i(APP_TAG, "-----TIMER 60 sec EXPIRED------");
                //  finish();
                restart_spo2tracker();
                // Se ejecutara cada 60 segundos
            }
        };

        // Programa la TimerTask para ejecutarla cada 60 segundos (1000 milliseconds * 60 seconds)
        timer.schedule(timerTask, 0, 1000 * 60);
    }

    // Observador para los datos de los trackers. Maneja los cambios en los datos de frecuencia cardíaca, temperatura y SpO2.
    final TrackerDataObserver trackerDataObserver = new TrackerDataObserver() {

        @Override
        public void onHeartRateTrackerDataChanged(HeartRateData hrData) {

            hr_value = String.valueOf(hrData.hr);
            hribi_value = String.valueOf(hrData.ibi);
            String timestamp = hrData.timeStamp;

            //Discard write data if both variables are 0
            if (hrData.hr!=0 || hrData.ibi!=0) {
                fileString = fileString+ timestamp+ ", " + hr_value + ", " + hribi_value + ", " + 0+ ", " + 0 + ", " +0+  ", " + "H"+"\n";
                Log.i(APP_TAG, "HR: "+hrData.hr+", "+ hrData.ibi);
                data_counter = data_counter+1;

                if (data_counter >= 60){ //escribir en fichero cada 1.5 minuto approx
                    data_counter = 0;
                    FileWriters(fileString,file_sleep);
                    fileString = "";
                    Log.i(APP_TAG, "FILE WRITTEN: ");
                }
            }
        }

        @Override
        public void onTempTrackerDataChanged(int status, float rec_tempValue, float ambientT, String timestamp) {
            temp_value = String.valueOf(rec_tempValue);
            temp_value_ambient = String.valueOf(ambientT);

            Log.i(APP_TAG, "dato TEMPERATURE : "+temp_value);
            fileString = fileString+ timestamp+ ", " + 0 + ", " + 0 + ", " + 0+ ", " +temp_value+ ", " +temp_value_ambient+ ", " + "T"+"\n";
        }

        @Override
        public void onSpO2TrackerDataChanged(int status, int spO2Value, String timestamp) {
            //  Log.i(APP_TAG, "--- SPO2 - status: "+ status);

            if(status == previousStatus) {
                return;
            }
            previousStatus = status;
            switch (status) {
                case SpO2Status.CALCULATING:
                    Log.i(APP_TAG, "SPO2 - Calculating measurement");
                    break;
                case SpO2Status.DEVICE_MOVING:
                    Log.i(APP_TAG, "SPO2 Device is moving");
                    break;

                case SpO2Status.LOW_SIGNAL:
                    Log.i(APP_TAG, "SPO2 Low signal quality");
                    stop_sp02tracker();
                    break;

                case SpO2Status.MEASUREMENT_COMPLETED:
                    Log.i(APP_TAG, "*********** SPO2 Measurement completed *******************");
                    spo2_value = String.valueOf(spO2Value);
                    Log.i(APP_TAG, "SPO2 value "+spo2_value);
                    fileString = fileString+ timestamp+ ", " + 0 + ", " + 0 + ", " + spo2_value+ ", " +0+ ", " +0+  ", " + "S"+"\n";
                    //restart_spo2tracker();
                    stop_sp02tracker();

                    break;

                default:
                    Log.i(APP_TAG, "--------SPO2 TIMEOUT--------");
                    //restart_spo2tracker();
                    stop_sp02tracker();
                    break;
            }
        }

        @Override
        public void onAccTrackerDataChanged(List<AccData> accData) {

        }

        // Método para detener el tracker de SpO2.
        public void stop_sp02tracker(){
            if (spO2Listener!=null) {
                spO2Listener.stopTracker();
            }

            Log.i(APP_TAG, "SP02 - STOP TRACKER");

        }
        @Override
        public void onError(int errorResourceId) {
            runOnUiThread(() ->
                    Toast.makeText(getApplicationContext(), getString(errorResourceId), Toast.LENGTH_LONG));
        }
    };

    // Observador para la conexión del gestor. Maneja los resultados de la conexión y errores.
    private final ConnectionObserver connectionObserver = new ConnectionObserver() {
        @Override
        public void onConnectionResult(int stringResourceId) {
/*            runOnUiThread(() -> Toast.makeText(getApplicationContext(), getString(stringResourceId)
                    , Toast.LENGTH_LONG).show());*/

            if (stringResourceId != R.string.ConnectedToHs) {
                finish();
            }

            TrackerDataNotifier.getInstance().addObserver(trackerDataObserver);

            spO2Listener = new SpO2Listener();
            heartRateListener = new HeartRateListener();
            tempListener = new TemperatureListener();

            if (mWakeLock != null) {
                mWakeLock.acquire();
            }

            connectionManager.initSpO2(spO2Listener);
            connectionManager.initHeartRate(heartRateListener);
            connectionManager.initTemperature(tempListener);
            startTimer();

            measure();
        }

        @Override
        public void onError(HealthTrackerException e) {
            if (e.getErrorCode() == HealthTrackerException.OLD_PLATFORM_VERSION || e.getErrorCode() == HealthTrackerException.PACKAGE_NOT_INSTALLED)
                runOnUiThread(() -> Toast.makeText(getApplicationContext()
                        , getString(R.string.HealthPlatformVersionIsOutdated), Toast.LENGTH_LONG).show());
            if (e.hasResolution()) {
                e.resolve(MainActivityNoAccel.this);
            } else {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), getString(R.string.ConnectionError)
                        , Toast.LENGTH_LONG).show());
                Log.e(APP_TAG, "Could not connect to Health Tracking Service: " + e.getMessage());
            }
            finish();
        }
    };

    // Se llama al metodo cuando la actividad vuelve a primer plano.
    @Override
    protected void onResume() {
        super.onResume();

        if (!isMeasurementRunning){
            isMeasurementRunning = true;
            startSleep();
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        flag= flag+1;
        SimpleDateFormat time_now = new SimpleDateFormat("HH_mm_ss");
        String timestamp = time_now.format(new Date());
        fileString_log= fileString_log + timestamp+", "+"main"+", "+"onResume()1"+"\n";
        FileWritersLog(fileString_log, logs);
        fileString_log = "";
        Log.i(APP_TAG, "ENTERING  ON-RESUME 1");

        if (flag >=2) {
            flag= 0;
            fileString_log= fileString_log + timestamp+", "+"main"+", "+"onResume()2"+"\n";
            FileWritersLog(fileString_log, logs);
            fileString_log = "";
            Log.i(APP_TAG, "ENTERING  ON-RESUME 2");
        }
    }

    //Se llama al metodo cuando la actividad está parcialmente oculta.
    @Override
    protected void onPause() {
        super.onPause();
        // Acquire wake lock

        SimpleDateFormat time_now = new SimpleDateFormat("HH_mm_ss");
        String timestamp = time_now.format(new Date());
        fileString_log= fileString_log + timestamp+", "+"main"+", "+"onPause()"+"\n";
        FileWritersLog(fileString_log,logs);
        fileString_log = "";
        Log.i(APP_TAG,"ENTERING  ON-PAUSE");
    }

    // Se llama al metodo cuando la actividad ya no es visible.
    @Override
    protected void onStop() {
        super.onStop();

        flag2= flag2+1;
        SimpleDateFormat time_now = new SimpleDateFormat("HH_mm_ss");
        String timestamp = time_now.format(new Date());
        fileString_log= fileString_log + timestamp+", "+"main"+", "+"onStop()1"+"\n";
        FileWritersLog(fileString_log, logs);
        fileString_log = "";
        Log.i(APP_TAG, "ENTERING  STOP 1");

        if (flag2 >=2) {
            flag2= 0;
            fileString_log= fileString_log + timestamp+", "+"main"+", "+"onStop()2"+"\n";
            FileWritersLog(fileString_log, logs);
            fileString_log = "";
            Log.i(APP_TAG, "ENTERING STOP 2");

            if (mWakeLock!=null){
                mWakeLock.release();
            }
            stopTrackers();
            finish();
        }
    }

    // Se llama al metodo cuando la actividad está a punto de ser visible.
    @Override
    protected void onStart() {
        super.onStart();

        SimpleDateFormat time_now = new SimpleDateFormat("HH_mm_ss");
        String timestamp = time_now.format(new Date());
        fileString_log= fileString_log + timestamp+", "+"main"+", "+"onStart()"+"\n";
        FileWritersLog(fileString_log,logs);
        fileString_log = "";
        Log.i(APP_TAG,"ENTERING  ON-START");
    }

    // Se llama al metodo antes de que la actividad sea destruida.
    @Override
    protected void onDestroy() {
        if (mWakeLock != null) {
            mWakeLock.release();
        }

        super.onDestroy();
        SimpleDateFormat time_now = new SimpleDateFormat("HH_mm_ss");
        String timestamp = time_now.format(new Date());
        fileString_log= fileString_log + timestamp+", "+"main"+", "+"onDestroy()"+"\n";
        FileWritersLog(fileString_log,logs);
        fileString_log = "";
        Log.i(APP_TAG,"ENTERING  ON-DESTROY");

        closeApplication();
    }

    // Método para crear el gestor de conexiones. Inicializa y conecta el gestor de conexiones.
    void createConnectionManager() {
        try {
            connectionManager = new ConnectionManager(connectionObserver);
            connectionManager.connect(getApplicationContext());
            isMeasurementRunning = true;
        } catch (Throwable t) {
            Log.e(APP_TAG, t.getMessage());
        }
    }

    // Método para detener todos los trackers. Cancela el Timer y detiene los listeners de los trackers.
    public void stopTrackers(){
        isMeasurementRunning = false;
        Log.i(APP_TAG, "Stop all trackers");

        timer.cancel(); // Cancelar medición de SpO2

        if (heartRateListener != null) {
            heartRateListener.stopTracker();
        }
        if (spO2Listener != null) {
            spO2Listener.stopTracker();
        }
        if (tempListener != null) {
            tempListener.stopTracker();
        }
        TrackerDataNotifier.getInstance().removeObserver(trackerDataObserver);
        if (connectionManager != null) {
            connectionManager.disconnect();
        }
    }

    // Método para iniciar las mediciones. Activa los trackers de frecuencia cardíaca, temperatura y SpO2.
    public void measure() {
        heartRateListener.startTracker();
        Log.i(APP_TAG, "HR tracker ON");
        tempListener.startTracker();
        Log.i(APP_TAG, "temp tracker ON");
        spO2Listener.startTracker();
        Log.i(APP_TAG, "SPO2 tracker ON");
    }
}