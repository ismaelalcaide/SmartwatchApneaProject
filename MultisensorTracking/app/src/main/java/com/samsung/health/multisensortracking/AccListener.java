package com.samsung.health.multisensortracking;

import android.util.Log;
import androidx.annotation.NonNull;
import com.samsung.android.service.health.tracking.HealthTracker;
import com.samsung.android.service.health.tracking.data.DataPoint;
import com.samsung.android.service.health.tracking.data.ValueKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Clase que extiende BaseListener para manejar eventos de datos de aceleración
public class AccListener extends BaseListener {
    private static final String TAG = "MainActivity";

    // Constructor que configura el Listener de eventos del rastreador
    AccListener() {
        final HealthTracker.TrackerEventListener trackerEventListener = new HealthTracker.TrackerEventListener() {
            @Override
            public void onDataReceived(@NonNull List<DataPoint> list) {
                int vx = 0, vy = 0, vz = 0;
                int step = 5;  // Factor de reducción de datos
                int targetSize = Math.round(list.size() / step);

                List<AccData> accList = new ArrayList<>(targetSize);
                SimpleDateFormat time_now = new SimpleDateFormat("HH_mm_ss");
                String timestamp = time_now.format(new Date());

                // Procesa la lista de datos recibidos, reduciendo la frecuencia de muestreo
                for (int i = 0; i < list.size(); i += step) {
                    DataPoint dataPoint = list.get(i);
                    vx = dataPoint.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_X);
                    vy = dataPoint.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_Y);
                    vz = dataPoint.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_Z);

                    AccData accData = new AccData(0, vx, vy, vz, timestamp);
                    accList.add(accData);
                }

                if (!accList.isEmpty()) {
                    readValuesFromDataPoint(accList);
                } else {
                    Log.i(TAG, "onDataReceived List is zero");
                }
            }

            @Override
            public void onFlushCompleted() {
                Log.i(TAG, "onFlushCompleted called");
            }

            @Override
            public void onError(HealthTracker.TrackerError trackerError) {
                Log.e(TAG, "onError called: " + trackerError);
                setHandlerRunning(false);
                if (trackerError == HealthTracker.TrackerError.PERMISSION_ERROR) {
                    TrackerDataNotifier.getInstance().notifyError(R.string.NoPermission);
                }
                if (trackerError == HealthTracker.TrackerError.SDK_POLICY_ERROR) {
                    TrackerDataNotifier.getInstance().notifyError(R.string.SdkPolicyError);
                }
            }
        };
        setTrackerEventListener(trackerEventListener);
    }

    // Método para notificar a los observadores con los datos procesados
    public void readValuesFromDataPoint(List<AccData> accList) {
        TrackerDataNotifier.getInstance().notifyAccTrackerObservers(accList);
    }

    // Métodos para reducir la frecuencia de muestreo mediante promedio
    public static List<Double> downsample_avg(List<Double> original, int targetSize) {
        List<Double> downsampled = new ArrayList<>(targetSize);
        int originalSize = original.size();
        int step = originalSize / targetSize;

        for (int i = 0; i < targetSize; i++) {
            int start = i * step;
            int end = Math.min(start + step, originalSize);
            double sum = 0;
            for (int j = start; j < end; j++) {
                sum += original.get(j);
            }
            double average = sum / (end - start);
            downsampled.add(average);
        }
        return downsampled;
    }

    // Método para reducir la frecuencia de muestreo seleccionando valores
    public static List<Double> downsample(List<Double> original, int targetSize) {
        List<Double> downsampled = new ArrayList<>(targetSize);
        int originalSize = original.size();
        int step = originalSize / targetSize;
        int offset = step / 2; // Ajuste para redondear al valor más cercano

        for (int i = 0; i < targetSize; i++) {
            int index = i * step + offset;
            downsampled.add(original.get(index));
        }
        return downsampled;
    }
}
