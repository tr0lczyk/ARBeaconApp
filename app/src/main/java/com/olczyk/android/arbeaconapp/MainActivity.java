package com.olczyk.android.arbeaconapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Toast;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.TransformableNode;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    CustomArFragment arFragment;
    ModelRenderable lampPostRenderable;
    BeaconController beaconController;
    boolean isBeaconNear = false;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }
        setContentView(R.layout.activity_main);
        arFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.customArFragment);
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
        arFragment.setOnTapArPlaneListener(
                (HitResult hitresult, Plane plane, MotionEvent motionevent) -> {
                    if(isBeaconNear == true){
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
}
