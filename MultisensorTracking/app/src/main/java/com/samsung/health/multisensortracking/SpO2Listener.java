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

import android.util.Log;

import androidx.annotation.NonNull;

import com.samsung.android.service.health.tracking.HealthTracker;
import com.samsung.android.service.health.tracking.data.DataPoint;
import com.samsung.android.service.health.tracking.data.ValueKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

//Clase para escuchar y manejar eventos de medición de SpO2 (saturación de oxígeno en la sangre).
public class SpO2Listener extends BaseListener {
    // Etiqueta para registro de eventos y errores.
    private final static String APP_TAG = "MainActivity";

    //Constructor que inicializa el oyente de eventos del rastreador de salud.
    SpO2Listener() {
        final HealthTracker.TrackerEventListener trackerEventListener = new HealthTracker.TrackerEventListener() {
            @Override
            public void onDataReceived(@NonNull List<DataPoint> list) {
                // Procesa cada punto de datos recibido.
                for (DataPoint data : list) {
                    updateSpo2(data);
                }
            }

            @Override
            public void onFlushCompleted() {
                // Registrado cuando todos los datos pendientes han sido procesados.
                Log.i(APP_TAG, "onFlushCompleted called");
            }

            @Override
            public void onError(HealthTracker.TrackerError trackerError) {
                // Manejo de errores durante el seguimiento de la salud.
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

    //Actualiza los valores de SpO2 recibidos del dispositivo de seguimiento.
    public void updateSpo2(DataPoint dataPoint) {
        SimpleDateFormat time_now = new SimpleDateFormat("HH_mm_ss");
        String timestamp = time_now.format(new Date()); // Obtiene el timestamp actual.

        // Obtiene el estado de la medición.
        final int status = dataPoint.getValue(ValueKey.SpO2Set.STATUS);
        int spo2Value = 0;
        if (status == SpO2Status.MEASUREMENT_COMPLETED) {
            // Si la medición se completó correctamente, obtiene el valor de SpO2.
            spo2Value = dataPoint.getValue(ValueKey.SpO2Set.SPO2);
            // Notifica a los observadores del valor de SpO2.
            TrackerDataNotifier.getInstance().notifySpO2TrackerObservers(status, spo2Value, timestamp);
        }
    }
}
