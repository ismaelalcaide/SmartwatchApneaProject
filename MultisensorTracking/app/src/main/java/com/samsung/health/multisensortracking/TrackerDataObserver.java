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

import java.util.List;

//Interfaz para observadores que desean recibir notificaciones sobre cambios en los datos de seguimiento de diferentes sensores de salud.
public interface TrackerDataObserver {
    //Se invoca cuando hay un cambio en los datos de frecuencia cardíaca.
    void onHeartRateTrackerDataChanged(HeartRateData hrData);

    //Se invoca cuando hay un cambio en los datos de SpO2.
    void onSpO2TrackerDataChanged(int status, int spO2Value, String timestamp);

    //Se invoca cuando hay un cambio en los datos de la aceleración.
    void onAccTrackerDataChanged(List<AccData> accData);

    //Se invoca cuando hay un cambio en los datos de temperatura.
    void onTempTrackerDataChanged(int status, float tempData, float tempDataAmbient, String timestamp);

    //Se invoca cuando ocurre un error durante el proceso de seguimiento.
    void onError(int errorResourceId);
}
