package mureung.wifiapconnection;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
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

import mureung.wifiapconnection.WifiConnect.SocketClient;
import mureung.wifiapconnection.WifiConnect.SocketServer;


public class MainActivity extends AppCompatActivity{


    public static Handler mainAcitivityHandler;
    public static Context mainContext;
    BroadcastReceiver broadcastReceiver;

    public static WifiP2pManager wifiP2pManager ;
    public static WifiP2pManager.Channel channel ;

    public static String connectIpAddress = null;
    public static String connectGatewayAddress = null;
    private boolean Connect_FLAG = false;






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

        mainAcitivityHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case 1:
                        Toast.makeText(getBaseContext(),String.valueOf(msg.obj),Toast.LENGTH_SHORT).show();
                        break;
                }

                return true;
            }
        });

        getConnectedInfo(getBaseContext());


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            unregisterReceiver(broadcastReceiver);
        }catch (Exception e){
            e.printStackTrace();
        }

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
                    !isPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    !isPermission(context, Manifest.permission.WRITE_SETTINGS)) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.ACCESS_FINE_LOCATION) ||
                        ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.CAMERA) ||
                        ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.WRITE_SETTINGS) ||
                        ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                }
                ActivityCompat.requestPermissions((Activity)context,new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                android.Manifest.permission.CAMERA,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_SETTINGS},
                        1000);
            }

            /*if(Settings.canDrawOverlays(context)){
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

            }*/
            if(Settings.System.canWrite(context)){

            }else {
                if(context!= null){
                    Uri uri = Uri.fromParts("package",context.getPackageName(),null);
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,uri);
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

    private void getConnectedInfo(Context context){

        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo!= null && networkInfo.isConnected()){
            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
                WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
                int ipAddress = dhcpInfo.ipAddress;
                int gateway = dhcpInfo.gateway;
                String strIpAddress = String.format("%d.%d.%d.%d",(ipAddress&0xff),(ipAddress>>8&0xff),(ipAddress>>16&0xff),(ipAddress>>24&0xff));
                String strGateway = String.format("%d.%d.%d.%d",(gateway&0xff),(gateway>>8&0xff),(gateway>>16&0xff),(gateway>>24&0xff));
                if(!strIpAddress.equals("0.0.0.0")&&!strGateway.equals("0.0.0.0")){
                    connectIpAddress = strIpAddress;
                    connectGatewayAddress = strGateway;
                    Log.e("getConnectedInfo","strIpAddress : " + strIpAddress + " , strGateway : " + strGateway);

                }
            }
        }


    }


    public void setBroadcastReceiver(Context context){
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
                int ipAddress = dhcpInfo.ipAddress;
                int gateway = dhcpInfo.gateway;
                String strIpAddress = String.format("%d.%d.%d.%d",(ipAddress&0xff),(ipAddress>>8&0xff),(ipAddress>>16&0xff),(ipAddress>>24&0xff));
                final String strGateway = String.format("%d.%d.%d.%d",(gateway&0xff),(gateway>>8&0xff),(gateway>>16&0xff),(gateway>>24&0xff));
                if(!strIpAddress.equals("0.0.0.0")&&!strGateway.equals("0.0.0.0")){
                    Log.e("onReceiver","strIpAddress : " + strIpAddress + " , strGateway : " + strGateway);
                    connectIpAddress = strIpAddress;
                    connectGatewayAddress = strGateway;
                    if(!Connect_FLAG){
                        Connect_FLAG = true;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                new SocketClient(strGateway,"9990");
                            }
                        },1000);

                    }

                }


            }
        };



        context.registerReceiver(broadcastReceiver,new IntentFilter("android.net.wifi.STATE_CHANGE"));
    }





}
