package com.buddycloud.android.buddydroid;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.packet.IQ;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.buddycloud.android.buddydroid.collector.CellListener;
import com.buddycloud.android.buddydroid.collector.NetworkListener;
import com.buddycloud.android.buddydroid.provider.BuddyCloud.ChannelData;
import com.buddycloud.android.buddydroid.provider.BuddyCloud.Roster;
import com.buddycloud.android.buddydroid.sync.ChannelSync;
import com.buddycloud.android.buddydroid.sync.RoasterSync;
import com.buddycloud.jbuddycloud.BCAtomListener;
import com.buddycloud.jbuddycloud.BCGeoLocListener;
import com.buddycloud.jbuddycloud.BuddycloudClient;
import com.buddycloud.jbuddycloud.packet.BCAtom;
import com.buddycloud.jbuddycloud.packet.BeaconLog;
import com.buddycloud.jbuddycloud.packet.GeoLoc;
import com.buddycloud.jbuddycloud.packet.PlainPacket;

public class BuddycloudService extends Service {

    private static final String TAG = "Service";
    private BuddycloudClient mConnection;
    private BuddycloudService service = this;

    private CellListener cellListener = null;
    private NetworkListener networkListener = null;

    private TaskQueueThread taskQueue;

    private Handler toastHandler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            Toast.makeText(service, msg.getData().get("msg").toString(),
                    Toast.LENGTH_LONG).show();
        }

    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, " onCreate");
        cellListener = new CellListener(this);
        networkListener = new NetworkListener(this);
    }

    public void createConnection() {
        if (mConnection != null && mConnection.isConnected()) {
            return;
        }

        SharedPreferences pm =
            PreferenceManager.getDefaultSharedPreferences(this);

        String jid = pm.getString("jid", null);

        if (jid != null && jid.indexOf('@') == -1) {
            Log.d("SMACK", "Invalid jid!!");
            return;
        }
        String password = pm.getString("password", null);

        Connection.DEBUG_ENABLED = true;
        BuddycloudClient.DEBUG_ENABLED = true;
        mConnection = BuddycloudClient.createBuddycloudClient(
            jid,
            password,
            null, null, null
        );

    }

    public void configureConnection() {
        if (mConnection == null || !mConnection.isAuthenticated()) {
            return;
        }
        mConnection.addGeoLocListener(new BCGeoLocListener() {
            public void receive(String from, GeoLoc loc) {
                if (loc.getType() == null) {
                    return;
                }
                ContentValues values = new ContentValues();
                if (loc.getLocType().equals(GeoLoc.Type.CURRENT)) {
                    values.put(Roster.GEOLOC, loc.getText());
                } else
                if (loc.getLocType().equals(GeoLoc.Type.NEXT)) {
                    values.put(Roster.GEOLOC_NEXT, loc.getText());
                } else
                if (loc.getLocType().equals(GeoLoc.Type.PREV)) {
                    values.put(Roster.GEOLOC_PREV, loc.getText());
                }
                Log.d(TAG, "Update '/user/" + from + "/channel'");
                getContentResolver().update(Roster.CONTENT_URI, values,
                        Roster.JID + "='/user/" + from + "/channel'",
                        null);
            }
        });
        mConnection.addAtomListener(new BCAtomListener() {
            public void receive(String node, BCAtom atom) {
                if (node.startsWith("/user/")) {
                    node = node.substring(6);
                    String jid = node.substring(0, node.indexOf('/'));
                    node = node.substring(node.indexOf('/') + 1);
                    if (node.startsWith("geo/")) {
                        ContentValues values = new ContentValues();
                        GeoLoc loc = atom.getGeoloc();
                        if (node.equals("geo/future")) {
                            values.put(Roster.GEOLOC_NEXT, loc.getText());
                        } else
                        if (node.equals("geo/current")) {
                            values.put(Roster.GEOLOC, loc.getText());
                        } else
                        if (node.equals("geo/previous")) {
                            values.put(Roster.GEOLOC_PREV, loc.getText());
                        }
                        getContentResolver().update(Roster.CONTENT_URI, values,
                                Roster.JID + "='" + jid + "'",
                                null);
                        return;
                    }
                    if (!node.equals("channel")) {
                        // /user/jid/mood ?
                        return;
                    }
                    node = "/user/" + jid + "/" + node;
                }
                // Channel !
                ContentValues values = new ContentValues();
                values.put(ChannelData.NODE_NAME,
                           node);
                values.put(ChannelData.AUTHOR,
                           atom.getAuthorName());
                values.put(ChannelData.AUTHOR_JID,
                           atom.getAuthorJid());
                values.put(ChannelData.AUTHOR_AFFILIATION,
                           atom.getAffiliation());
                values.put(ChannelData.CONTENT,
                           atom.getContent());
                values.put(ChannelData.CONTENT_TYPE,
                           atom.getContentType());
                values.put(ChannelData.ITEM_ID,
                           atom.getId());
                values.put(ChannelData.LAST_UPDATED,
                           atom.getId());
                values.put(ChannelData.PARENT,
                           atom.getParentId());
                values.put(ChannelData.PUBLISHED,
                           atom.getPublished());
                GeoLoc loc = atom.getGeoloc();
                if (loc != null) {
                    values.put(ChannelData.GEOLOC_ACCURACY,
                               loc.getAccuracy());
                    values.put(ChannelData.GEOLOC_AREA,
                               loc.getArea());
                    values.put(ChannelData.GEOLOC_COUNTRY,
                               loc.getCountry());
                    values.put(ChannelData.GEOLOC_LAT,
                               loc.getLat());
                    values.put(ChannelData.GEOLOC_LOCALITY,
                               loc.getLocality());
                    values.put(ChannelData.GEOLOC_LON,
                               loc.getLon());
                    values.put(ChannelData.GEOLOC_REGION,
                               loc.getRegion());
                    values.put(ChannelData.GEOLOC_TEXT,
                               loc.getText());
                    if (loc.getLocType() != null) {
                        values.put(ChannelData.GEOLOC_TYPE,
                                   loc.getLocType().toString());
                    }
                }

                getContentResolver().insert(ChannelData.CONTENT_URI, values);
                Log.d(TAG, "stored " + atom.getId() + "@" + node);
            }
        });
    }

    public void updateRoaster() {
        if (mConnection == null ||
           !mConnection.isConnected() ||
           !mConnection.isAuthenticated()
        ) {
            return;
        }
        new RoasterSync(mConnection, getContentResolver());
    }

    public void updateChannels() {
        if (mConnection == null ||
           !mConnection.isConnected() ||
           !mConnection.isAuthenticated()
        ) {
            return;
        }
        new ChannelSync(mConnection, getContentResolver());
    }

    private long beaconLogTimer;
    private int internalPriority;

    /**
     * Send a beacon log. Priority is 0..10, with 0 meaning instant and
     * 10 meaning "if 3 min. have passed".
     * @param prio
     * @throws InterruptedException 
     */
    public boolean sendBeaconLog(final int prio) throws InterruptedException {
        final int priority = Math.min(internalPriority, prio);
        return taskQueue.add(new Runnable() {
            public void run() {
                if (mConnection == null ||
                    !mConnection.isAuthenticated()
                ) {
                    return;
                }
                long now = System.currentTimeMillis();
                long delta = now - beaconLogTimer;
                if (delta < 30000 * priority) {
                    return;
                }
                if (delta < 10000) {
                    internalPriority = priority;
                    return;
                }
                internalPriority = 10;
                beaconLogTimer = now;
                BeaconLog log = new BeaconLog();
                log.setTo("butler.buddycloud.com");
                log.setFrom(mConnection.getUser());
                cellListener.appendTo(log);
                networkListener.appendTo(log);
                mConnection.sendPacket(log);
            }
        });
    }

    public boolean send(final IQ iq) throws InterruptedException {
        return taskQueue.add(new Runnable() {
            public void run() {
                if (mConnection == null ||
                    !mConnection.isAuthenticated()
                ) {
                    return;
                }
                mConnection.sendPacket(iq);
            }
        });
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG, " onStart");
        super.onStart(intent, startId);

        if (taskQueue == null) {
            taskQueue = new TaskQueueThread();
            taskQueue.start();
        }

        try {
            if (!taskQueue.add(new Runnable() {

                public void run() {
                    if (mConnection == null ||
                            !mConnection.isConnected() ||
                            !mConnection.isAuthenticated()
                        ) {
                            cellListener.start();

                            createConnection();

                            android.os.Message msg = new android.os.Message();
                            if (mConnection != null && mConnection.isAuthenticated()) {
                                msg.getData().putString("msg", "You are online!");
                                toastHandler.sendMessage(msg);
                                try {
                                    sendBeaconLog(0);
                                } catch (InterruptedException e) {
                                    Log.d(TAG, e.toString(), e);
                                }
                            } else {
                                msg.getData().putString("msg", "Login failed :-(");
                                toastHandler.sendMessage(msg);
                                return;
                            }
                            try {
                                sendBeaconLog(0);
                            } catch (InterruptedException e) {
                                Log.d(TAG, e.toString(), e);
                            }
                            configureConnection();
                            updateRoaster();
                            updateChannels();
                        }
                }
            })) {
                Log.d(TAG, "Failed to start service");
            }
        } catch (InterruptedException e) {
            Log.d(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, " onDestroy");
        super.onDestroy();

        cellListener.stop();

        try {
            taskQueue.add(new Runnable() {
                public void run() {
                    synchronized (TAG) {
                        if (mConnection != null && mConnection.isConnected()) {
                            mConnection.disconnect();
                            mConnection = null;
                        }
                    }
                }
            });
        } catch (InterruptedException e) {
            // Doesn't matter
        }

        taskQueue.stopQueue();
        taskQueue = null;

        synchronized (TAG) {
            if (mConnection != null && mConnection.isConnected()) {
                mConnection.disconnect();
                mConnection = null;
            }
        }

        Log.i(TAG, "disonnected.");
    }

    private final IBuddycloudService.Stub binder =
        new IBuddycloudService.Stub() {

            public boolean isConnected() throws RemoteException {
                return (mConnection != null) && mConnection.isConnected();
            }

            public void send(String rawXml)
                    throws RemoteException {
                Log.d("RemoteBC", "received: " + rawXml);
                mConnection.sendPacket(new PlainPacket(rawXml));
            }

            public String getJidWithResource() throws RemoteException {
                return mConnection.getUser();
            }
        
    };

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
