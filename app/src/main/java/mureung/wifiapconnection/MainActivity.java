package mureung.wifiapconnection;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;


public class MainActivity extends AppCompatActivity{


    public static Handler MainActivityHandler;
    public static Context mainContext;
    BroadcastReceiver broadcastReceiver;

    public static WifiP2pManager wifiP2pManager ;
    public static WifiP2pManager.Channel channel ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Log.e("test","configApState() : " + configApState());

        mainContext = getBaseContext();
        mainChangeMenu(new MainView());


        wifiP2pManager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this,getMainLooper(),null);


        ConnectivityManager connectivityManager = (ConnectivityManager)getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager !=null){
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo!=null){
                Log.e("test","ConnectivityManager.TYPE_WIFI : " + ConnectivityManager.TYPE_WIFI);
                Log.e("test","ConnectivityManager.TYPE_MOBILE : " + ConnectivityManager.TYPE_MOBILE);
                Log.e("test","networkInfo : " + networkInfo.getType());
            }else {
                Log.e("test","networkInfo : null");
            }

        }



        checkPermission(this);

        /*BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.startDiscovery();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    Log.e("MainActivity","test onReceive getName :  "+device.getName()+"   , getAddress : "+device.getAddress());


                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(broadcastReceiver, filter);*/


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void mainChangeMenu(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container,fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        switch (PageStr.getPageStrData()){
            case PageStr.Mainview :
                new DialogManager(this).positiveNegativeDialog("앱 종료", "앱을 종료하시겠습니까? ",
                        "확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                android.os.Process.killProcess(Process.myPid());

                            }
                        }, "취소",null);
                break;

        }
    }

    public static boolean isPermission(Context context, String strPermission){
        return ActivityCompat.checkSelfPermission(context, strPermission) == PackageManager.PERMISSION_GRANTED;
    }
    public static void checkPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    !isPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    !isPermission(context, Manifest.permission.CALL_PHONE) ||
                    !isPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.ACCESS_FINE_LOCATION) ||
                        ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.CAMERA) ||
                        ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                }
                ActivityCompat.requestPermissions((Activity)context,new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                android.Manifest.permission.CAMERA,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1000);
            }

            if(Settings.canDrawOverlays(context)){
                if(context != null){
                    Log.e("test","다른 앱 위에 그리기 권한  "+Settings.canDrawOverlays(context));
                }

            }else{
                if(context != null){
                    Log.e("test","다른 앱 위에 그리기 권한  "+Settings.canDrawOverlays(context));
                    Uri uri = Uri.fromParts("package",context.getPackageName(),null);
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }

            }
        }

    }

    /**
     * 2018.04.04 by.GeonHo
     * 핫스팟 자동으로 켜기 부분
     * configApState() 키면 자동으로 켜진다.
     * */

    private boolean isApOn(Context context){
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
        wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wifiConfig.preSharedKey = "\"".concat("123456789").concat("\"");



        try{
            if(isApOn(context)){
                wifiManager.setWifiEnabled(false);
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
