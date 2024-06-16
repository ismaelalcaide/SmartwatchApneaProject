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

import android.os.Handler;
import android.util.Log;
import com.samsung.android.service.health.tracking.HealthTracker;

// Clase BaseListener para manejar los eventos del HealthTracker
public class BaseListener {

    // Etiqueta usada para los logs de esta clase
    private static final String APP_TAG = "BaseListener";

    // Handler para postergar la ejecución de código en el hilo principal
    private Handler handler;
    // Referencia al HealthTracker
    private HealthTracker healthTracker;
    // Flag para controlar si el handler está corriendo
    private boolean isHandlerRunning = false;

    // Listener de eventos para el HealthTracker
    private HealthTracker.TrackerEventListener trackerEventListener = null;

    // Configura el HealthTracker
    public void setHealthTracker(HealthTracker tracker) {
        healthTracker = tracker;
    }

    // Configura el Handler
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    // Establece el estado de ejecución del handler
    public void setHandlerRunning(boolean handlerRunning) {
        isHandlerRunning = handlerRunning;
    }

    // Establece el listener de eventos del tracker
    public void setTrackerEventListener(HealthTracker.TrackerEventListener tracker) {
        trackerEventListener = tracker;
    }

    // Inicia el tracker
    public void startTracker() {
        Log.i(APP_TAG, "startTracker called");
        Log.d(APP_TAG, "healthTracker: " + healthTracker);
        Log.d(APP_TAG, "trackerEventListener: " + trackerEventListener);
        if (!isHandlerRunning) {
            handler.post(() -> {
                healthTracker.setEventListener(trackerEventListener);
                setHandlerRunning(true);
            });
        }
    }

    // Detiene el tracker
    public void stopTracker() {
        Log.i(APP_TAG, "stopTracker called");
        Log.d(APP_TAG, "healthTracker: " + healthTracker);
        Log.d(APP_TAG, "trackerEventListener: " + trackerEventListener);
        if (isHandlerRunning) {
            healthTracker.unsetEventListener();
            setHandlerRunning(false);
            handler.removeCallbacksAndMessages(null);
        }
    }
}
