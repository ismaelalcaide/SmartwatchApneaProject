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

// Clase que almacena y gestiona datos de frecuencia cardíaca
public class HeartRateData {
    // Constantes para manejar la calidad del intervalo entre latidos (IBI)
    public static final int IBI_QUALITY_SHIFT = 15;
    public static final int IBI_MASK = 0x1;
    public static final int IBI_QUALITY_MASK = 0x7FFF;

    // Estado de la medición de la frecuencia cardíaca
    int status = HeartRateStatus.HR_STATUS_NONE;
    // Valor de la frecuencia cardíaca
    int hr = 0;
    // Valor del intervalo entre latidos
    int ibi = 0;
    // Calidad del intervalo entre latidos
    int qIbi = 1;

    // Marca de tiempo para los datos
    String timeStamp = "";

    // Constructor sin argumentos
    HeartRateData() {
    }

    // Constructor con argumentos para inicializar todos los campos de la clase
    HeartRateData(int status, int hr, int ibi, int qIbi, String timeStamp) {
        this.status = status;
        this.hr = hr;
        this.ibi = ibi;
        this.qIbi = qIbi;
        this.timeStamp = timeStamp;
    }

    // Método para obtener el intervalo entre latidos con información de calidad incorporada
    int getHrIbi() {
        // Desplaza los bits de calidad y combina con el IBI usando una operación OR
        return (qIbi << IBI_QUALITY_SHIFT) | ibi;
    }
}

