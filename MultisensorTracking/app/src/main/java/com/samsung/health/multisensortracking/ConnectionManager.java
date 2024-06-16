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

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.samsung.android.service.health.tracking.ConnectionListener;
import com.samsung.android.service.health.tracking.HealthTracker;
import com.samsung.android.service.health.tracking.HealthTrackerException;
import com.samsung.android.service.health.tracking.HealthTrackingService;
import com.samsung.android.service.health.tracking.data.HealthTrackerType;

import java.util.List;

// Clase que gestiona la conexión con los servicios de seguimiento de salud
public class ConnectionManager {
    private static final String TAG = "Connection Manager";
    private final ConnectionObserver connectionObserver;
    private HealthTrackingService healthTrackingService = null;

    // Listener para eventos de conexión
    private final ConnectionListener connectionListener = new ConnectionListener() {
        @Override
        public void onConnectionSuccess() {
            Log.i(TAG, "Connected");
            connectionObserver.onConnectionResult(R.string.ConnectedToHs);
            // Comprobaciones de disponibilidad de ciertos tipos de seguimiento
            checkTrackerSupport();
        }

        @Override
        public void onConnectionEnded() {
            Log.i(TAG, "Disconnected");
        }

        @Override
        public void onConnectionFailed(HealthTrackerException e) {
            connectionObserver.onError(e);
        }
    };

    public ConnectionManager(ConnectionObserver observer) {
        connectionObserver = observer;
    }

    public void connect(Context context) {
        healthTrackingService = new HealthTrackingService(connectionListener, context);
        healthTrackingService.connectService();
    }

    public void disconnect() {
        if (healthTrackingService != null)
            healthTrackingService.disconnectService();
    }

    // Métodos para inicializar diferentes HealthTrackers
    public void initSpO2(SpO2Listener spO2Listener) {
        initTracker(HealthTrackerType.SPO2, spO2Listener);
    }

    public void initHeartRate(HeartRateListener heartRateListener) {
        initTracker(HealthTrackerType.HEART_RATE, heartRateListener);
    }

    public void initAcc(AccListener accListener) {
        initTracker(HealthTrackerType.ACCELEROMETER, accListener);
    }

    public void initTemperature(TemperatureListener tempListener) {
        initTracker(HealthTrackerType.SKIN_TEMPERATURE_CONTINUOUS, tempListener);
    }

    // Método auxiliar para configurar un HealthTracker y su Handler
    private void initTracker(HealthTrackerType type, BaseListener listener) {
        HealthTracker healthTracker = healthTrackingService.getHealthTracker(type);
        listener.setHealthTracker(healthTracker);
        setHandlerForBaseListener(listener);
    }

    private void setHandlerForBaseListener(BaseListener baseListener) {
        baseListener.setHandler(new Handler(Looper.getMainLooper()));
    }

    // Métodos para verificar la disponibilidad de funciones de seguimiento
    private boolean isSpO2Available(@NonNull HealthTrackingService service) {
        return isTrackerAvailable(service, HealthTrackerType.SPO2);
    }

    private boolean isHeartRateAvailable(@NonNull HealthTrackingService service) {
        return isTrackerAvailable(service, HealthTrackerType.HEART_RATE);
    }

    private boolean isAccAvailable(@NonNull HealthTrackingService service) {
        return isTrackerAvailable(service, HealthTrackerType.ACCELEROMETER);
    }

    private boolean isTrackerAvailable(@NonNull HealthTrackingService service, HealthTrackerType type) {
        List<HealthTrackerType> availableTrackers = service.getTrackingCapability().getSupportHealthTrackerTypes();
        return availableTrackers.contains(type);
    }

    // Método para revisar la disponibilidad de seguimiento y notificar al observer
    private void checkTrackerSupport() {
        if (!isSpO2Available(healthTrackingService)) {
            Log.i(TAG, "Device does not support SpO2 tracking");
            connectionObserver.onConnectionResult(R.string.NoSpo2Support);
        }
        if (!isHeartRateAvailable(healthTrackingService)) {
            Log.i(TAG, "Device does not support Heart Rate tracking");
            connectionObserver.onConnectionResult(R.string.NoHrSupport);
        }
        if (!isAccAvailable(healthTrackingService)) {
            Log.i(TAG, "Device does not support Accelerometer tracking");
            connectionObserver.onConnectionResult(R.string.NoAccSupport);
        }
    }
}
