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

// Clase HeartRateListener que extiende BaseListener para manejar eventos de datos del ritmo cardíaco
public class HeartRateListener extends BaseListener {
    private static final String APP_TAG = "HRactivity";

    // Constructor que configura el listener de eventos del rastreador
    HeartRateListener() {
        final HealthTracker.TrackerEventListener trackerEventListener = new HealthTracker.TrackerEventListener() {
            @Override
            public void onDataReceived(@NonNull List<DataPoint> list) {
                for (DataPoint dataPoint : list) {
                    readValuesFromDataPoint(dataPoint);
                }
            }

            @Override
            public void onFlushCompleted() {
                Log.i(APP_TAG, " onFlushCompleted called");
            }

            @Override
            public void onError(HealthTracker.TrackerError trackerError) {
                Log.e(APP_TAG, " onError called: " + trackerError);
                setHandlerRunning(false);
                handleTrackerError(trackerError);
            }
        };
        setTrackerEventListener(trackerEventListener);
    }

    // Método para procesar y enviar datos del punto de datos recibido
    public void readValuesFromDataPoint(DataPoint dataPoint) {
        SimpleDateFormat time_now = new SimpleDateFormat("HH_mm_ss");
        String timestamp = time_now.format(new Date());

        final HeartRateData hrData = new HeartRateData();
        hrData.timeStamp = timestamp;

        // Recuperar y almacenar las listas de intervalos entre latidos y su estado
        final List<Integer> hrIbiList = dataPoint.getValue(ValueKey.HeartRateSet.IBI_LIST);
        final List<Integer> hrIbiStatus = dataPoint.getValue(ValueKey.HeartRateSet.IBI_STATUS_LIST);

        hrData.status = dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE_STATUS);
        hrData.hr = dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE);
        if (hrIbiList != null && !hrIbiList.isEmpty()) {
            hrData.ibi = hrIbiList.get(hrIbiList.size() - 1); // Último intervalo entre latidos
        }
        if (hrIbiStatus != null && !hrIbiStatus.isEmpty()) {
            hrData.qIbi = hrIbiStatus.get(hrIbiStatus.size() - 1); // Calidad del último IBI
        }

        // Notificar a los observadores sobre los nuevos datos
        TrackerDataNotifier.getInstance().notifyHeartRateTrackerObservers(hrData);
        Log.d(APP_TAG, "Data received: " + dataPoint.toString());
    }

    // Método para manejar errores específicos del tracker
    private void handleTrackerError(HealthTracker.TrackerError trackerError) {
        if (trackerError == HealthTracker.TrackerError.PERMISSION_ERROR) {
            TrackerDataNotifier.getInstance().notifyError(R.string.NoPermission);
        }
        if (trackerError == HealthTracker.TrackerError.SDK_POLICY_ERROR) {
            TrackerDataNotifier.getInstance().notifyError(R.string.SdkPolicyError);
        }
    }
}
