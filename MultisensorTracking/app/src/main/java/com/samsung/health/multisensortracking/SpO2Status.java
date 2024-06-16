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

//Clase que define constantes para representar los distintos estados de una medición de SpO2.
public class SpO2Status {
    // Indica que la señal es demasiado baja para obtener una lectura precisa.
    public static final int LOW_SIGNAL = -5;

    // Indica que el dispositivo está en movimiento y puede afectar la precisión de la medición.
    public static final int DEVICE_MOVING = -4;

    // Estado inicial de la medición, antes de que se haya realizado cualquier cálculo.
    public static final int INITIAL_STATUS = -1;

    // Estado durante el cual se están calculando los valores de SpO2.
    public static final int CALCULATING = 0;

    // Indica que la medición de SpO2 se ha completado con éxito y los datos están listos para ser leídos.
    public static final int MEASUREMENT_COMPLETED = 2;
}
