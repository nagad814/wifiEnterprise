package com.ants.plugin.wifienterprise;

import org.apache.cordova.*;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import android.content.Context;

import java.util.List;
import java.util.Collections;

import java.util.concurrent.Future;
import java.net.NetworkInterface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.SupplicantState;
import android.content.Context;
import android.util.Log;
import android.os.CountDownTimer;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;

public class WifiEnterprise extends CordovaPlugin {

  private static final String TAG                 = "WifiEnterprise";
  private static final String SET_WIFI_EAP        = "setWifiEap";
  private static final String REMOVE_WIFI_EAP     = "removeWifiEap";
  private static final String CONNECT_EAP_NETWORK = "connectNetwork";
  private static final String DISABLE_NETWORK     = "disconnectNetwork";
  private static final String GET_MAC_ADDRESS     = "getMacAddress";
  private static final String GET_WIFI_MAC        = "getWifiMacAddress";
  private static final String START_SCAN          = "startScan";
  private static final String SCAN_WIFI           = "scanWifi";
  private static final String LIST_NETWORKS       = "listNetworks";
  private static final String GET_SCAN_RESULTS    = "getScanResults";
  private static final String IS_WIFI_ENABLED     = "isWifiEnabled";
  private static final String SET_WIFI_ENABLED    = "setWifiEnabled";
  private static final String GET_CONNECTED_SSID  = "getConnectedSSID";
  private static final String GET_SUPPLICANT_STATE  = "getSupplicantState";

  private WifiManager wifiManager;
  private CallbackContext callbackContext;
  private WifiConfiguration wifi = new WifiConfiguration();

  @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.wifiManager = (WifiManager) cordova.getActivity().getSystemService(Context.WIFI_SERVICE);
        // this.registerReceiver(this.WifiStateChangedReceiver,
        //        new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext)
          throws JSONException {

        this.callbackContext = callbackContext;

        if(action.equals(IS_WIFI_ENABLED))
            return this.isWifiEnabled(callbackContext);
        else if(action.equals(SET_WIFI_ENABLED))
            return this.setWifiEnabled(callbackContext, data);
        else if(action.equals(LIST_NETWORKS))
            return this.listNetworks(callbackContext);
        else if(action.equals(START_SCAN))
            return this.startScan(callbackContext);
        else if(action.equals(SCAN_WIFI))
            return this.scanWifi(callbackContext, data);
        else if(action.equals(GET_SCAN_RESULTS))
            return this.getScanResults(callbackContext, data);
        else if(action.equals(SET_WIFI_EAP))
            return this.setWifiEap(callbackContext,data);
        else if(action.equals(REMOVE_WIFI_EAP))
            return this.removeWifiEap(callbackContext,data);
        else if(action.equals(CONNECT_EAP_NETWORK))
            return this.connectNetwork(callbackContext,data);
        else if(action.equals(DISABLE_NETWORK))
            return this.disconnectNetwork(callbackContext,data);
        else if(action.equals(GET_MAC_ADDRESS))
            return this.getMacAddress(callbackContext);
        else if(action.equals(GET_WIFI_MAC))
            return this.getWifiMacAddress(callbackContext);
        else if(action.equals(GET_CONNECTED_SSID))
            return this.getConnectedSSID(callbackContext);
        else if(action.equals(GET_SUPPLICANT_STATE))
            return this.getSupplicantState(callbackContext);
        else
            return false;
    }

//=============================== Helper methods ====================================
    /**
     *    This method takes a given String, searches the current list of configured WiFi
     *     networks, and returns the networkId for the network if the SSID matches. If not,
     *     it returns -1.
     */
    private int ssidToNetworkId(String ssid) {
        List<WifiConfiguration> currentNetworks = wifiManager.getConfiguredNetworks();
        int networkId = -1;

        // For each network in the list, compare the SSID with the given one
        for (WifiConfiguration test : currentNetworks) {
            if ( test.SSID.equals(ssid) ) {
                networkId = test.networkId;
            }
        }

        return networkId;
    }

    private boolean validateData(JSONArray data) {
        try {
            if (data == null || data.get(0) == null) {
                callbackContext.error("Data is null.");
                return false;
            }
            return true;
        }
        catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
        return false;
    }

//=============================== Helper methods ====================================

    /**
     * This method retrieves the current WiFi status
     *
     *    @param    callbackContext        A Cordova callback context
     *    @return    true if WiFi is enabled, fail will be called if not.
    */
    private boolean isWifiEnabled(CallbackContext callbackContext) {
        boolean isEnabled = wifiManager.isWifiEnabled();
        callbackContext.success(isEnabled ? "1" : "0");
        return isEnabled;
    }

    /***
     *    This method enables or disables the wifi
     */
    private boolean setWifiEnabled(CallbackContext callbackContext, JSONArray data) {
        if(!validateData(data)) {
            callbackContext.error("WifiEnterprise: disconnectNetwork invalid data");
            Log.d(TAG, "WifiEnterprise: disconnectNetwork invalid data");
            return false;
        }

        String status = "";

        try {
            status = data.getString(0);
        }
        catch (Exception e) {
            callbackContext.error(e.getMessage());
            Log.d(TAG, e.getMessage());
            return false;
        }

        boolean currentStatus = status.equals("true")?true:false;

        if (wifiManager.setWifiEnabled(currentStatus) ) {
            callbackContext.success("");
            return true;
        }
        else {
            callbackContext.error("Cannot enable wifi");
            return false;
        }
    }

    /**
     * Gets the mac address.
     *
     * @return the mac address
     */
    private boolean getMacAddress(CallbackContext callbackContext) {
        String macAddress = null;
        //WifiManager wm = (WifiManager) this.cordova.getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        macAddress = wifiManager.getConnectionInfo().getMacAddress();

        if (macAddress == null || macAddress.length() == 0) {
            macAddress = "00:00:00:00:00:00";
            callbackContext.error("Failed to get macAddress : " + macAddress);
            return false;
        }

        callbackContext.success(macAddress);
        return true;
    }

    public boolean getWifiMacAddress(CallbackContext callbackContext) {

        String macAddress = null;
        try {

            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                     callbackContext.error("Error retreiving wifi macAddress : ");
                     return false;
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                macAddress = res1.toString();
                callbackContext.success(macAddress);

                return true;
            }
        }
        catch (Exception ex) {

        }
        macAddress = "02:00:00:00:00:00";
        callbackContext.error("Failed to get macAddress : " + macAddress);
        return false;
    }

    /**
     *    This method uses the callbackContext.success method to send a JSONArray
     *    of the currently configured networks.
     *
     *    @param    callbackContext        A Cordova callback context
     *    @param    data                JSON Array, with [0] being SSID to connect
     *    @return    true if network disconnected, false if failed
     */
    private boolean listNetworks(CallbackContext callbackContext) {
        Log.d(TAG, "WifiEnterprise: listNetworks entered.");
        List<WifiConfiguration> wifiList = wifiManager.getConfiguredNetworks();

        JSONArray returnList = new JSONArray();

        for (WifiConfiguration wifi : wifiList) {
            returnList.put(wifi.SSID);
        }

        callbackContext.success(returnList);

        return true;
    }

    /**
       *    This method uses the callbackContext.success method to send a JSONArray
       *    of the scanned networks.
       *
       *    @param    callbackContext        A Cordova callback context
       *    @param    data                   JSONArray with [0] == JSONObject
       *    @return    true
       */
    private boolean getScanResults(CallbackContext callbackContext, JSONArray data) {
        List<ScanResult> scanResults = wifiManager.getScanResults();

        JSONArray returnList = new JSONArray();

        Integer numLevels = null;

        if(!validateData(data)) {
            callbackContext.error("WifiEnterprise: disconnectNetwork invalid data");
            Log.d(TAG, "WifiEnterprise: disconnectNetwork invalid data");
            return false;
        }else if (!data.isNull(0)) {
            try {
                JSONObject options = data.getJSONObject(0);

                if (options.has("numLevels")) {
                    Integer levels = options.optInt("numLevels");

                    if (levels > 0) {
                        numLevels = levels;
                    } else if (options.optBoolean("numLevels", false)) {
                        // use previous default for {numLevels: true}
                        numLevels = 5;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                callbackContext.error(e.toString());
                return false;
            }
        }

        for (ScanResult scan : scanResults) {
            /*
             * @todo - breaking change, remove this notice when tidying new release and explain changes, e.g.:
             *   0.y.z includes a breaking change to WifiEnterprise.getScanResults().
             *   Earlier versions set scans' level attributes to a number derived from wifiManager.calculateSignalLevel.
             *   This update returns scans' raw RSSI value as the level, per Android spec / APIs.
             *   If your application depends on the previous behaviour, we have added an options object that will modify behaviour:
             *   - if `(n == true || n < 2)`, `*.getScanResults({numLevels: n})` will return data as before, split in 5 levels;
             *   - if `(n > 1)`, `*.getScanResults({numLevels: n})` will calculate the signal level, split in n levels;
             *   - if `(n == false)`, `*.getScanResults({numLevels: n})` will use the raw signal level;
             */

            int level;

            if(scan.capabilities.contains("EAP"))
              Log.d(TAG, "WifiEnterprise: Found EAP Configured SSID!!!");

            if (numLevels == null) {
              level = scan.level;
            } else {
              level = wifiManager.calculateSignalLevel(scan.level, numLevels);
            }

            JSONObject lvl = new JSONObject();
            try {
                lvl.put("level", level);
                lvl.put("SSID", scan.SSID);
                lvl.put("BSSID", scan.BSSID);
                lvl.put("frequency", scan.frequency);
                lvl.put("capabilities", scan.capabilities);
               // lvl.put("timestamp", scan.timestamp);
                returnList.put(lvl);
            } catch (JSONException e) {
                e.printStackTrace();
                callbackContext.error(e.toString());
                return false;
            }
        }

        callbackContext.success(returnList);
        return true;
    }

    /**
       *    This method uses the callbackContext.success method. It starts a wifi scanning
       *
       *    @param    callbackContext        A Cordova callback context
       *    @return    true if started was successful
       */
    private boolean startScan(CallbackContext callbackContext) {

        if(wifiManager.startScan()) {
            callbackContext.success();
            return true;
        }
        else {
            callbackContext.error("Scan failed");
            return false;
        }
    }

    private class ScanSyncContext {
        public boolean finished = false;
    }

    /**
     * Scans networks and sends the list back on the success callback
     * @param callbackContext   A Cordova callback context
     * @param data  JSONArray with [0] == JSONObject
     * @return true
     */
    private boolean scanWifi(final CallbackContext callbackContext, final JSONArray data) {
        Log.v(TAG, "Entering startScan");
        final ScanSyncContext syncContext = new ScanSyncContext();

        final BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.v(TAG, "Entering onReceive");
                synchronized (syncContext) {
                    if (syncContext.finished) {
                        Log.v(TAG, "In onReceive, already finished");
                        return;
                    }
                    syncContext.finished = true;
                    context.unregisterReceiver(this);
                }
                Log.v(TAG, "In onReceive, success");
                getScanResults(callbackContext, data);
            }
        };

        final Context context = cordova.getActivity().getApplicationContext();

        Log.v(TAG, "Submitting timeout to threadpool");
        cordova.getThreadPool().submit(new Runnable() {
            public void run() {
                Log.v(TAG, "Entering timeout");
                final int TEN_SECONDS = 10000;
                try {
                    Thread.sleep(TEN_SECONDS);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Received InterruptedException e, " + e);
                    // keep going into error
                }
                Log.v(TAG, "Thread sleep done");
                synchronized (syncContext) {
                    if (syncContext.finished) {
                        Log.v(TAG, "In timeout, already finished");
                        return;
                    }
                    syncContext.finished = true;
                    context.unregisterReceiver(receiver);
                }
                Log.v(TAG, "In timeout, error");
                callbackContext.error("Timed out waiting for scan to complete");
            }
        });

        Log.v(TAG, "Registering broadcastReceiver");
        context.registerReceiver(
                receiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        );

        if (!wifiManager.startScan()) {
            callbackContext.error("Scan failed");
        }
        Log.v(TAG, "Starting wifi scan");
        return true;
    }

    private boolean setWifiEap(CallbackContext callbackContext, JSONArray data) {

        try {

            String newSSID = data.getString(0);

            String identity = data.getString(1);
            String passwd = data.getString(2);

            wifi.SSID = newSSID;
            wifi.enterpriseConfig.setIdentity(identity);
            wifi.enterpriseConfig.setPassword(passwd);

            wifi.status = WifiConfiguration.Status.ENABLED;
            wifi.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
            wifi.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
            wifi.enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TTLS);
            wifi.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.PAP);

            wifi.networkId = ssidToNetworkId(newSSID);

            if ( wifi.networkId == -1 ) {
                wifiManager.addNetwork(wifi);
                callbackContext.success(newSSID + " successfully added eap config.");
            }
            else {
                wifiManager.updateNetwork(wifi);
                callbackContext.success(newSSID + " successfully updated eap config.");
            }

            wifiManager.saveConfiguration();
            return true;
        }
        catch (Exception e) {
            callbackContext.error(e.getMessage());
            Log.d("WifiEnterprise",e.getMessage());
            return false;
        }

    }//End of method

    /**
     *    This method removes a network from the list of configured networks.
     *
     *    @param    callbackContext        A Cordova callback context
     *    @param    data                JSON Array, with [0] being SSID to remove
     *    @return    true if network removed, false if failed
     */
    private boolean removeWifiEap(CallbackContext callbackContext, JSONArray data) {

        Log.d(TAG, "WifiEnterprise: removeWifiEap entered.");

        if(!validateData(data)) {
            callbackContext.error("WifiEnterprise: removeWifiEap data invalid");
            Log.d(TAG, "WifiEnterprise: removeNetwork data invalid");
            return false;
        }

        try {
            String ssidToDisconnect = data.getString(0);

            int networkIdToRemove = ssidToNetworkId(ssidToDisconnect);

            if (networkIdToRemove >= 0) {
                wifiManager.removeNetwork(networkIdToRemove);
                wifiManager.saveConfiguration();
                callbackContext.success("Network removed.");
                return true;
            }
            else {
                callbackContext.error("Network not found.");
                Log.d(TAG, "WifiEnterprise: Network not found, can't remove.");
                return false;
            }
        }
        catch (Exception e) {
            Log.d(TAG, e.getMessage());
            callbackContext.error(e.getMessage());

            return false;
        }
    }//End of method removeWifiEap()

    private boolean getSupplicantState(CallbackContext callbackContext) {

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        // SupplicantState supState = wifiInfo.getSupplicantState();
        // String strSupState = supState.toString();
        try {
            callbackContext.success( wifiInfo.getSupplicantState().toString() );
            return true;
        }
        catch (Exception e) {
            callbackContext.error(e.getMessage());
            Log.d("WifiEnterprise",e.getMessage());
            return false;
        }
    }
    /**
     *    This method connects a network.
     *
     *    @param    callbackContext        A Cordova callback context
     *    @param    data                JSON Array, with [0] being SSID to connect
     *    @return    true if network connected, false if failed
     */
    private boolean connectNetwork(CallbackContext callbackContext, JSONArray data) {

        Log.i(TAG, "WifiEnterprise: connectNetwork entered.");

        // if(!validateData(data)) {
        //     callbackContext.error("WifiEnterprise: connectNetwork invalid data");
        //     Log.i(TAG, "WifiEnterprise: connectNetwork invalid data.");
        //     return false;
        // }

        String ssidToConnect = "";

        try {
            ssidToConnect = data.getString(0);
        }
        catch (Exception e) {
            callbackContext.error(e.getMessage());
            Log.i(TAG, e.getMessage());
            return false;
        }

        int networkIdToConnect = ssidToNetworkId(ssidToConnect);

        if (networkIdToConnect >= 0) {
            // We disable the network before connecting, because if this was the last connection before
            // a disconnect(), this will not reconnect.
            wifiManager.disableNetwork(networkIdToConnect);
            wifiManager.enableNetwork(networkIdToConnect, true);

            callbackContext.success("SUCCESS");
            // SupplicantState supState;
            // WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            // supState = wifiInfo.getSupplicantState();
            // String strSupState = supState.toString();

            // if(supState.equals("COMPLETED"))
            //     callbackContext.success(strSupState);
            // else 
            return true;
        }
        else{
            callbackContext.error("WifiEnterprise: cannot connect to network");
            return false;
        }
    }//End of method connectNetwork

    /**
     *    This method disconnects a network.
     *
     *    @param    callbackContext        A Cordova callback context
     *    @param    data                JSON Array, with [0] being SSID to connect
     *    @return    true if network disconnected, false if failed
     */
    private boolean disconnectNetwork(CallbackContext callbackContext, JSONArray data) {

        Log.i(TAG, "WifiEnterprise: disconnectNetwork entered.");
        if(!validateData(data)) {
            callbackContext.error("WifiEnterprise: disconnectNetwork invalid data");
            Log.d(TAG, "WifiEnterprise: disconnectNetwork invalid data");
            return false;
        }

        String ssidToDisconnect = "";

        try {
            ssidToDisconnect = data.getString(0);
        }
        catch (Exception e) {
            callbackContext.error(e.getMessage());
            Log.i(TAG, e.getMessage());
            return false;
        }

        int networkIdToDisconnect = ssidToNetworkId(ssidToDisconnect);

        if (networkIdToDisconnect > 0) {
            wifiManager.disableNetwork(networkIdToDisconnect);
            callbackContext.success("Network " + ssidToDisconnect + " disconnected!");
            return true;
        }
        else {
            callbackContext.error("Network " + ssidToDisconnect + " not found!");
            Log.i(TAG, "WifiEnterprise: Network not found to disconnect.");
            return false;
        }
    }//End of method disconnectNetwork

    /**
     *    This method disconnects current network.
     *
     *    @param    callbackContext        A Cordova callback context
     *    @return    true if network disconnected, false if failed
     */
    private boolean disconnect(CallbackContext callbackContext) {
        Log.i(TAG, "WifiEnterprise: disconnect entered.");

        if (wifiManager.disconnect()) {
            callbackContext.success("Disconnected from current network");
            return true;
        } else {
            callbackContext.error("Unable to disconnect from the current network");
            return false;
        }
    }//End of method disconnect

     /**
     * This method retrieves the SSID for the currently connected network
     *
     *    @param    callbackContext        A Cordova callback context
     *    @return    true if SSID found, false if not.
    */
    private boolean getConnectedSSID(CallbackContext callbackContext){
        if(!wifiManager.isWifiEnabled()){
            callbackContext.error("Wifi is disabled");
            return false;
        }

        WifiInfo info = wifiManager.getConnectionInfo();

        if(info == null){
            callbackContext.error("Unable to read wifi info");
            return false;
        }

        String ssid = info.getSSID();
        if(ssid.isEmpty()) {
            ssid = info.getBSSID();
        }
        if(ssid.isEmpty()){
            callbackContext.error("SSID is empty");
            return false;
        }

        callbackContext.success(ssid);
        return true;
    }
}

/*
public boolean execute(String action, final JSONArray args,
        final CallbackContext callbackId) throws JSONException {
    IntentFilter wifiFilter = new IntentFilter(
            WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
    cordova.getActivity().registerReceiver(wifiBroadcastReceiver,
            wifiFilter);
    this.callbackContext = callbackId;
    PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
    result.setKeepCallback(true);
    this.callbackContext.sendPluginResult(result);
    return true;
}

public class WifiReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
            PluginResult result;
            if (intent.getBooleanExtra(
                    WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
                Toast.makeText(cordova.getActivity(), "Wifi Connected",
                        Toast.LENGTH_SHORT).show();
                result = new PluginResult(PluginResult.Status.OK,
                        "Wifi Connected");
            } else {
                Toast.makeText(cordova.getActivity(), "Wifi Disconnected",
                        Toast.LENGTH_SHORT).show();
                result = new PluginResult(PluginResult.Status.ERROR,
                        "Wifi Disconnected");
            }

            result.setKeepCallback(false);
            if (callbackContext != null) {
                callbackContext.sendPluginResult(result);
                callbackContext = null;
            }
        }
    }
}
*/
