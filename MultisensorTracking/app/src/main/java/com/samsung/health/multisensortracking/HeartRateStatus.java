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

// Clase que define constantes para representar los diferentes estados del sensor de frecuencia cardíaca
public class HeartRateStatus {
    // No hay estado actual detectado
    public static final int HR_STATUS_NONE = 0;

    // Frecuencia cardíaca encontrada
    public static final int HR_STATUS_FIND_HR = 1;

    // Sensor correctamente adjunto al usuario
    public static final int HR_STATUS_ATTACHED = -1;

    // Movimiento detectado que puede afectar a la medición
    public static final int HR_STATUS_DETECT_MOVE = -2;

    // Sensor desprendido del usuario
    public static final int HR_STATUS_DETACHED = -3;

    // Fiabilidad baja de los datos obtenidos
    public static final int HR_STATUS_LOW_RELIABILITY = -8;

    // Fiabilidad muy baja de los datos obtenidos
    public static final int HR_STATUS_VERY_LOW_RELIABILITY = -10;

    // No hay datos disponibles para ser enviados
    public static final int HR_STATUS_NO_DATA_FLUSH = -99;
}
