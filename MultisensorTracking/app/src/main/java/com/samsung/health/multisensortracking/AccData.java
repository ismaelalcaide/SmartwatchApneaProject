package com.samsung.health.multisensortracking;

// Clase para manejar los datos de la aceleración
public class AccData {

    // Estado de la aceleración, inicializado a un estado predeterminado
    int status = AccStatus.ACC_STATUS_NONE;

    // Sumatorias para las aceleraciones en los ejes X, Y y Z
    int sumX = 0;
    int sumY = 0;
    int sumZ = 0;

    // Marca de tiempo para los datos recogidos
    String timeStamp = "";

    // Constructor por defecto
    AccData() {
    }

    // Constructor con parámetros para inicializar los datos de la aceleración
    AccData(int status, int sumX, int sumY, int sumZ, String timeStamp) {
        this.status = status;
        this.sumX = sumX;
        this.sumY = sumY;
        this.sumZ = sumZ;
        this.timeStamp = timeStamp;
    }
}