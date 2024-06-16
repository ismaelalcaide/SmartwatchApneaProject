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

import android.app.Service;

import java.util.ArrayList;
import java.util.List;

//Clase que gestiona la notificación de cambios en los datos de seguimiento a todos los observadores registrados.
public class TrackerDataNotifier {
    // Instancia única de la clase.
    private static TrackerDataNotifier instance;

    // Lista de observadores que escuchan los cambios de datos.
    private final List<TrackerDataObserver> observers = new ArrayList<>();

    //Devuelve la única instancia de la clase, creándola si es necesario.
    public static TrackerDataNotifier getInstance() {
        if (instance == null) {
            instance = new TrackerDataNotifier();
        }
        return instance;
    }

    //Añade un observador a la lista de observadores.
    public void addObserver(TrackerDataObserver observer) {
        observers.add(observer);
    }

    //Elimina un observador de la lista de observadores.
    public void removeObserver(TrackerDataObserver observer) {
        observers.remove(observer);
    }

    //Notifica a todos los observadores de cambios en los datos del monitor de frecuencia cardíaca.
    public void notifyHeartRateTrackerObservers(HeartRateData hrData) {
        observers.forEach(observer -> observer.onHeartRateTrackerDataChanged(hrData));
    }

    //Notifica a todos los observadores de cambios en los datos del monitor de SpO2.
    public void notifySpO2TrackerObservers(int status, int spO2Value, String timestamp) {
        observers.forEach(observer -> observer.onSpO2TrackerDataChanged(status, spO2Value, timestamp));
    }

    //Notifica a todos los observadores de cambios en los datos del monitor de temperatura.
    public void notifyTempTrackerObservers(int status, float tempValue, float tempValueAmbient, String timestamp) {
        observers.forEach(observer -> observer.onTempTrackerDataChanged(status, tempValue, tempValueAmbient, timestamp));
    }

    //Notifica a todos los observadores de cambios en los datos de la aceleración.
    public void notifyAccTrackerObservers(List<AccData> accData) {
        observers.forEach(observer -> observer.onAccTrackerDataChanged(accData));
    }

    //Notifica a todos los observadores de un error ocurrido durante el seguimiento.
    public void notifyError(int errorResourceId) {
        observers.forEach(observer -> observer.onError(errorResourceId));
    }
}
