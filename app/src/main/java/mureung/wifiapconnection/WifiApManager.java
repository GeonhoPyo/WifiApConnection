package mureung.wifiapconnection;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Method;

import mureung.wifiapconnection.WifiConnect.SocketServer;

public class WifiApManager {

    public boolean isApOn(Context context){
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        try{


            Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            Log.e("test","(Boolean) method.invoke(wifiManager) : " + (Boolean) method.invoke(wifiManager));
            return (Boolean) method.invoke(wifiManager);

        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean configApState(Context context){
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = "".concat("MureungTest").concat("");
        wifiConfig.status = WifiConfiguration.Status.DISABLED;
        wifiConfig.priority = 40;
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        wifiConfig.allowedAuthAlgorithms.clear();
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);




        try{
            if(isApOn(context)){
                wifiManager.setWifiEnabled(false);

            }else {
                new SocketServer().start();
            }
            Method method = wifiManager.getClass().getDeclaredMethod("setWifiApEnabled",WifiConfiguration.class,boolean.class);
            method.invoke(wifiManager,wifiConfig,!isApOn(context));
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
