package com.utils;

import java.lang.reflect.Method;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

/*
 * This class is use to handle all Hotspot related information.
 */
public class WifiApControl {
    private static final String TAG = "## WifiApControl";
    static final String SSID = "MyTalker_OPAfGjk85637";
    static final String PASSWORD = "jgyutfsSH$8753";
    static final int TYPE = 3;

    private static Method getWifiApState;
    private static Method isWifiApEnabled;
    private static Method setWifiApEnabled;
    private static Method getWifiApConfiguration;

    public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";

    private static final int WIFI_AP_STATE_DISABLED = WifiManager.WIFI_STATE_DISABLED;
    private static final int WIFI_AP_STATE_DISABLING = WifiManager.WIFI_STATE_DISABLING;
    private static final int WIFI_AP_STATE_ENABLED = WifiManager.WIFI_STATE_ENABLED;
    private static final int WIFI_AP_STATE_ENABLING = WifiManager.WIFI_STATE_ENABLING;
    private static final int WIFI_AP_STATE_FAILED = WifiManager.WIFI_STATE_UNKNOWN;

    public static final String EXTRA_PREVIOUS_WIFI_AP_STATE = WifiManager.EXTRA_PREVIOUS_WIFI_STATE;
    public static final String EXTRA_WIFI_AP_STATE = WifiManager.EXTRA_WIFI_STATE;

    static {
        // lookup methods and fields not defined publicly in the SDK.
        Class<?> cls = WifiManager.class;
        for (Method method : cls.getDeclaredMethods()) {
            String methodName = method.getName();
            switch (methodName){
                case "getWifiApState":
                    getWifiApState = method;
                    break;
                case "isWifiApEnabled":
                    isWifiApEnabled = method;
                    break;
                case "setWifiApEnabled":
                    setWifiApEnabled = method;
                    break;
                case "getWifiApConfiguration":
                    getWifiApConfiguration = method;
                    break;
            }
        }
    }

    private static boolean isApSupported() {
        return (getWifiApState != null && isWifiApEnabled != null
                && setWifiApEnabled != null && getWifiApConfiguration != null);
    }

    private WifiManager mWifiManager;

    private WifiApControl(WifiManager wifiManager) {
        this.mWifiManager = wifiManager;
    }

    public static WifiApControl createApControl(Context context) {
        if (!isApSupported()){
            Toast.makeText(context, "Cannot Support WifiAP", Toast.LENGTH_LONG).show();
            return null;
        }
        WifiManager wifiManager= (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return new WifiApControl(wifiManager);
    }

    public boolean isWifiApEnabled() {
        try {
            boolean isEnabled = (Boolean) isWifiApEnabled.invoke(mWifiManager);
            Log.i(TAG, "AP is " + isEnabled);
            return isEnabled;
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
            return false;
        }
    }

    public void printWifiApState() {
        try {
            int state = (Integer) getWifiApState.invoke(mWifiManager);
            switch (state){
                case WIFI_AP_STATE_DISABLED:
                    Log.i(TAG, "WIFI_AP_STATE : " + "DISABLED");
                    break;
                case WIFI_AP_STATE_DISABLING:
                    Log.i(TAG, "WIFI_AP_STATE : " + "DISABLING");
                    break;
                case WIFI_AP_STATE_ENABLED:
                    Log.i(TAG, "WIFI_AP_STATE : " + "ENABLED");
                    break;
                case WIFI_AP_STATE_ENABLING:
                    Log.i(TAG, "WIFI_AP_STATE : " + "ENABLING");
                    break;
                case WIFI_AP_STATE_FAILED:
                    Log.i(TAG, "WIFI_AP_STATE : " + "FAILED");
                    break;
            }
        } catch (Exception e) {
            Log.v(TAG, e.getMessage());
        }
    }

    public WifiConfiguration getWifiApConfiguration() {
        try {
            return (WifiConfiguration) getWifiApConfiguration.invoke(mWifiManager);
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
            return null;
        }
    }

    public boolean openAP(boolean enable){
        WifiConfiguration config = createWifiInfo(SSID, PASSWORD, TYPE);
        if (enable){
            mWifiManager.setWifiEnabled(false);
            return setWifiApEnabled(config);
        }
        mWifiManager.setWifiEnabled(true);
        return false;
    }

    private boolean setWifiApEnabled(WifiConfiguration config) {
        try {
            Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            return (Boolean) method.invoke(mWifiManager, config, true);
        } catch (Exception e) {
            //e.printStackTrace();
            Log.e(TAG, "setWifiApEnabled");
            return false;
        }
    }

    private WifiConfiguration createWifiInfo(String SSID, String Password, int Type)
    {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        if(Type == 1) //WIFICIPHER_NOPASS
        {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if(Type == 2) //WIFICIPHER_WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0]= "\"" + Password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if(Type == 3) //WIFICIPHER_WPA
        {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }
}