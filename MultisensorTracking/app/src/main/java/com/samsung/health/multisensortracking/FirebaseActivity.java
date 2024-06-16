package com.samsung.health.multisensortracking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Objects;

// Clase de actividad para manejar la subida de archivos a Firebase Storage
public class FirebaseActivity extends AppCompatActivity {
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    private static final String APP_TAG = "MainActivity";
    public int nfiles_sent = 0;
    public int nfiles_2send = 0;
    TextView nfiles_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase);
        nfiles_tv = findViewById(R.id.nfiles);
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfiles_sent = 0;
        nfiles_2send = 0;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        iterateOverFolderFiles();
    }

    // Método para iterar sobre archivos en un directorio y subirlos
    private void iterateOverFolderFiles() {
        String path = Objects.requireNonNull(this.getExternalFilesDir(null)).getAbsolutePath() + "/temp_data/";
        File directory = new File(path);
        File[] files = directory.listFiles();
        nfiles_2send = files != null ? files.length : 0;
        nfiles_tv.setText("ENVIADOS: " + nfiles_sent + " de " + nfiles_2send);

        if (files != null) {
            for (File file : files) {
                if (file.getAbsolutePath().endsWith(".csv")) {
                    uploadFile(file, file.getName());
                }
            }
        } else {
            nfiles_tv.setText("NADA QUE ENVIAR");
        }
    }

    // Método para subir un archivo a Firebase
    public void uploadFile(File file, String filename) {
        StorageReference sleepRef = storageRef.child("SleepData/" + filename);
        Uri file_uri = Uri.fromFile(file);
        UploadTask uploadTask = sleepRef.putFile(file_uri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.i(APP_TAG, "FIREBASE SENT ERROR");
                showToast("ERROR EN ENVIO");
                showToast(exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String filename = taskSnapshot.getMetadata().getName();
                String path = "/storage/emulated/0/Android/data/com.samsung.health.multisensortracking/files/temp_data/";
                new File(path + filename).delete();
                nfiles_sent++;
                nfiles_tv.setText("ENVIADOS: " + nfiles_sent + " de " + nfiles_2send);
                Log.i(APP_TAG, file + "-  FIREBASE SENT");
            }
        });
    }

    // Método para mostrar un Toast
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
