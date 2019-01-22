package com.olczyk.android.arbeaconapp;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class BeaconController implements BeaconConsumer {
    private static final String TAG = "MonitoringActivity";

    private BeaconManager beaconManager;
    private Context context;

    double distanceTrigger = 0.00;
    boolean connected = false;

    interface onBeaconDiscoveredListener {
        void onBeaconDiscovered();

        void onBeaconExit();
    }

    interface onBeaconInDistanceListener {
        void onBeaconInDistance(double distance);
    }

    void setDistanceTrigger(double distanceTrigger) {
        this.distanceTrigger = distanceTrigger;
    }

    BeaconController(Context context) {
        this.context = context;

        beaconManager = BeaconManager.getInstanceForApplication(context);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
    }

    void BeaconDiscoveryObserve(final onBeaconDiscoveredListener listener) {
        if (connected)
            beaconManager.addMonitorNotifier(new MonitorNotifier() {
                @Override
                public void didEnterRegion(Region region) {
                    listener.onBeaconDiscovered();
                    Log.i(TAG, "I just saw an beacon for the first time!");
                }

                @Override
                public void didExitRegion(Region region) {
                    listener.onBeaconExit();
                    Log.i(TAG, "I no longer see an beacon");
                }

                @Override
                public void didDetermineStateForRegion(int state, Region region) {
                    Log.i(TAG, "I have just switched from seeing/not seeing beacons: " + state);
                    if (state == 1) {
                        listener.onBeaconDiscovered();
                    } else {
                        listener.onBeaconExit();
                    }
                }
            });


        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException ignored) {
        }

    }


    void BeaconRangeObserve(final onBeaconInDistanceListener listener) {
        if (connected)
            beaconManager.addRangeNotifier(new RangeNotifier() {
                @Override
                public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                    if (distanceTrigger == 0.00) {
                        throw new RuntimeException("distance can't be 0, use setDistanceTrigger to ");
                    }
                    if (collection.size() > 0) {
                        if (collection.iterator().next().getDistance() >= distanceTrigger) {
                            listener.onBeaconInDistance(collection.iterator().next().getDistance());
                            Log.i(TAG, "The first beacon I see is about " + collection.iterator().next().getDistance() + " meters away.");
                        }
                    }
                }
            });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException ignored) {

        }
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.removeAllMonitorNotifiers();
        connected = true;
    }

    @Override
    public Context getApplicationContext() {
        return context;
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {

    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return false;
    }
}
