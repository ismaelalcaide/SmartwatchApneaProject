package com.samsung.health.multisensortracking;

/*
 * Copyright (C) 2021 Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Mobile Communication Division,
 * IT & Mobile Communications, Samsung Electronics Co., Ltd.
 *
 * This software and its documentation are confidential and proprietary
 * information of Samsung Electronics Co., Ltd.  No part of the software and
 * documents may be copied, reproduced, transmitted, translated, or reduced to
 * any electronic medium or machine-readable form without the prior written
 * consent of Samsung Electronics.
 *
 * Samsung Electronics makes no representations with respect to the contents,
 * and assumes no responsibility for any errors that might appear in the
 * software and documents. This publication and the contents hereof are subject
 * to change without notice.
 */



import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

///Activity que maneja la solicitud de permisos en tiempo de ejecución.
public class PermissionActivity extends FragmentActivity {
    // Lista de permisos requeridos por la aplicación.
    private ArrayList<String> mPermissions;

    // Etiqueta de depuración para registros.
    private static final String TAG = PermissionActivity.class.getSimpleName();

    // Clave para pasar permisos a través de Intent.
    private static final String PERMISSION_KEY = "permissions";

    // Identificador de la solicitud de permisos.
    private static final int PERMISSION_REQ_TAG = 1;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        finish(); // Finaliza la actividad al recibir un nuevo Intent.
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Recuperar lista de permisos desde el estado guardado o el Intent.
        if (savedInstanceState != null) {
            mPermissions = savedInstanceState.getStringArrayList(PERMISSION_KEY);
        } else {
            mPermissions = new ArrayList<>(Arrays.asList(getIntent().getStringArrayExtra(PERMISSION_KEY)));
        }

        Log.i(TAG, "onCreate()");
        requestPermission(); // Llamar a la función que maneja la solicitud de permisos.
    }

    //Solicita los permisos que aún no han sido otorgados.
    public void requestPermission() {
        Iterator<String> it = mPermissions.iterator();
        while (it.hasNext()){
            if (ActivityCompat.checkSelfPermission(this, it.next()) == PackageManager.PERMISSION_GRANTED) {
                it.remove(); // Eliminar permisos ya concedidos.
            }
        }

        // Si todos los permisos fueron concedidos, finalizar la actividad.
        if (mPermissions.isEmpty()) {
            setResult(RESULT_OK);
            Log.i(TAG, "finished");
            finish();
        } else {
            String[] permissions = mPermissions.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQ_TAG);
            Log.i(TAG, "requestPermissions");
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(PERMISSION_KEY, mPermissions); // Guardar lista de permisos en el estado.
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult() requestCode = " + requestCode);

        // Procesar resultado de la solicitud de permisos.
        if (requestCode == PERMISSION_REQ_TAG) {
            if (permissions.length == 0) {
                Log.i(TAG, "onRequestPermissionsResult : permission is 0");
                return;
            }
            for (int p : grantResults) {
                if (p == PackageManager.PERMISSION_DENIED) {
                    Log.i(TAG, "onRequestPermissionsResult : permission denied");
                    finish();
                    return;
                }
            }
            setResult(RESULT_OK);
            finish();
        }
    }

    //Verifica si todos los permisos especificados han sido concedidos.
    public static boolean checkPermission(@Nullable Context context, @NonNull String[] permissions) {
        for(String permission : permissions) {
            if (context == null || ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                Log.i(TAG, "checkPermission : PERMISSION_DENIED : " + permission);
                return false;
            } else {
                Log.i(TAG, "checkPermission : PERMISSION_GRANTED : " + permission);
            }
        }
        return true;
    }

    //Inicia PermissionActivity para solicitar los permisos necesarios.
    public static void showPermissionPrompt(@NotNull Activity callingActivity, int requestCode, @NotNull String[] permissions) {
        Intent intent = new Intent(callingActivity, PermissionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra(PERMISSION_KEY, permissions);
        callingActivity.startActivityForResult(intent, requestCode);
    }

}
