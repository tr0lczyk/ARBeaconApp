package com.olczyk.android.arbeaconapp;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Toast;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    ArFragment arFragment;
    ModelRenderable lampPostRenderable;
    BeaconController beaconController;
    boolean isBeaconNear = false;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }
        setContentView(R.layout.activity_main);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);
        ModelRenderable.builder()
                .setSource(this, Uri.parse("Beer.sfb"))
                .build()
                .thenAccept(renderable -> lampPostRenderable = renderable)
                .exceptionally(throwable -> {
                    Toast toast =
                            Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return null;
                });
        getLocationPermission();
        arFragment.setOnTapArPlaneListener(
                (HitResult hitresult, Plane plane, MotionEvent motionevent) -> {
                    if(isBeaconNear != false){
                        if (lampPostRenderable == null){
                            return;
                        }
                        Anchor anchor = hitresult.createAnchor();
                        AnchorNode anchorNode = new AnchorNode(anchor);
                        anchorNode.setParent(arFragment.getArSceneView().getScene());
                        TransformableNode lamp = new TransformableNode(arFragment.getTransformationSystem());
                        lamp.setParent(anchorNode);
                        lamp.setRenderable(lampPostRenderable);
                        lamp.select();
                    } else {
                        Toast.makeText(this, "NO BEACON FOUND", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        beaconController = new BeaconController(this);

        beaconController.setDistanceTrigger(0.01);
        beaconController.BeaconDiscoveryObserve(new BeaconController.onBeaconDiscoveredListener() {
            @Override
            public void onBeaconDiscovered() {
                Toast.makeText(MainActivity.this, "DISCOVERED", Toast.LENGTH_SHORT).show();
                isBeaconNear = true;
            }

            @Override
            public void onBeaconExit() {
                Toast.makeText(MainActivity.this, "NO BEACON", Toast.LENGTH_SHORT).show();
                isBeaconNear = false;
            }
        });



        beaconController.BeaconRangeObserve(new BeaconController.onBeaconInDistanceListener() {
            @Override
            public void onBeaconInDistance(double distance) {
                Log.d("TEST","distance" + distance);
            }
        });
    }
    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        Log.d("LOG", "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(MainActivity.this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(MainActivity.this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        Log.i("LOG", "onRequestPermissionsResult: called.");
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        Log.i("LOG", "permission failed");
                    } else {
                        Log.d("LOG", "permission success");
                        this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (ActivityCompat.checkSelfPermission(MainActivity.this,
                                        Manifest.permission.ACCESS_FINE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED &&
                                        ActivityCompat.checkSelfPermission(MainActivity.this,
                                                Manifest.permission.ACCESS_COARSE_LOCATION)
                                                != PackageManager.PERMISSION_GRANTED) {
                                }
                            }
                        });
                    }
                }
            }
        }
    }
}
