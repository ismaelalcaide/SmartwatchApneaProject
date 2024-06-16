package com.samsung.health.multisensortracking;

import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.samsung.android.service.health.tracking.HealthTracker;
import com.samsung.android.service.health.tracking.data.DataPoint;
import com.samsung.android.service.health.tracking.data.ValueKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

//Clase que gestiona la escucha y procesamiento de datos de temperatura corporal recibidos del sensor de salud.
public class TemperatureListener extends BaseListener {
    // Etiqueta para registros de depuración.
    private final static String APP_TAG = "TemperatureListener";

    // Manejador para operaciones que requieren retraso o ejecución en el hilo de la interfaz de usuario.
    private Handler skinTemperatureHandler;

    //Constructor que inicializa el listener de eventos para el rastreador de salud.
    TemperatureListener() {
        final HealthTracker.TrackerEventListener trackerEventListener = new HealthTracker.TrackerEventListener() {
            @Override
            public void onDataReceived(@NonNull List<DataPoint> list) {
                // Procesar cada punto de datos recibido.
                for (DataPoint data : list) {
                    updateTemperature(data);
                }
            }

            @Override
            public void onFlushCompleted() {
                // Registro al completar el envío de datos pendientes.
                Log.i(APP_TAG, "onFlushCompleted called");
            }

            @Override
            public void onError(HealthTracker.TrackerError trackerError) {
                // Manejar errores en el seguimiento de la salud.
                Log.e(APP_TAG, "onError called: " + trackerError);
                setHandlerRunning(false);
                if (trackerError == HealthTracker.TrackerError.PERMISSION_ERROR) {
                    TrackerDataNotifier.getInstance().notifyError(R.string.NoPermission);
                }
                if (trackerError == HealthTracker.TrackerError.SDK_POLICY_ERROR) {
                    TrackerDataNotifier.getInstance().notifyError(R.string.SdkPolicyError);
                }
            }
        };
        setTrackerEventListener(trackerEventListener);
    }

    //Actualiza los valores de temperatura obtenidos del dispositivo de seguimiento.
    public void updateTemperature(DataPoint dataPoint) {
        final int status = dataPoint.getValue(ValueKey.SkinTemperatureSet.STATUS);
        float tempValue = 0;
        float ambientTemp = 0;
        if (status == TemperatureStatus.SUCCESSFUL_MEASUREMENT) {
            // Extraer valores de temperatura si la medición fue exitosa.
            tempValue = dataPoint.getValue(ValueKey.SkinTemperatureSet.OBJECT_TEMPERATURE);
            ambientTemp = dataPoint.getValue(ValueKey.SkinTemperatureSet.AMBIENT_TEMPERATURE);
        }

        SimpleDateFormat time_now = new SimpleDateFormat("HH_mm_ss");
        String timestamp = time_now.format(new Date()); // Obtener el timestamp actual.

        // Notificar a los observadores con los datos de temperatura.
        TrackerDataNotifier.getInstance().notifyTempTrackerObservers(status, tempValue, ambientTemp, timestamp);
        Log.d(APP_TAG, dataPoint.toString()); // Registro detallado del punto de datos.
    }
}

