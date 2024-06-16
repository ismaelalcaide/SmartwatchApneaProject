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

import com.samsung.android.service.health.tracking.HealthTrackerException;

// Interfaz ConnectionObserver define los métodos para manejar los resultados y errores de la conexión
public interface ConnectionObserver {

    // Método que se invoca cuando se obtiene un resultado de conexión
    // El parámetro stringResourceId se refiere al ID de un recurso de string que indica el resultado de la conexión
    void onConnectionResult(int stringResourceId);

    // Método que se invoca cuando ocurre un error durante la conexión o el seguimiento de salud
    // El parámetro e es una excepción que contiene información sobre el error ocurrido
    void onError(HealthTrackerException e);
}
