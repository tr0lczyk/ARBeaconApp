package com.olczyk.android.arbeaconapp;

import android.Manifest;

import com.google.ar.sceneform.ux.ArFragment;

public class CustomArFragment extends ArFragment {

    @Override
    public String[] getAdditionalPermissions() {
        String[] additionalPermissions = super.getAdditionalPermissions();
        int permissionLength = additionalPermissions != null ? additionalPermissions.length : 0;
        String[] permissions = new String[permissionLength + 2];
        permissions[0] = Manifest.permission.ACCESS_COARSE_LOCATION;
        permissions[1] = Manifest.permission.ACCESS_FINE_LOCATION;
        if (permissionLength > 0) {
            System.arraycopy(additionalPermissions, 0, permissions, 1, additionalPermissions.length);
        }
        return permissions;
    }
}
