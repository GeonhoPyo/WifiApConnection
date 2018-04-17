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
import android.net.wifi.ScanResult;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.List;

import javax.security.auth.login.LoginException;

import mureung.wifiapconnection.WifiConnect.SocketClient;
import mureung.wifiapconnection.WifiConnect.SocketServer;


public class MainActivity extends AppCompatActivity{


    public static Handler mainAcitivityHandler;
    public static Context mainContext;
    BroadcastReceiver broadcastReceiver;
    BroadcastReceiver scanBroadCastReceiver;

    public static WifiP2pManager wifiP2pManager ;
    public static WifiP2pManager.Channel channel ;

    public static String connectIpAddress = null;
    public static String connectGatewayAddress = null;
    private boolean Connect_FLAG = false;
    private static WifiManager broadCastWifiManager;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Log.e("test","configApState() : " + configApState());

        mainContext = getBaseContext();
        mainChangeMenu(new MainView());


        wifiP2pManager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this,getMainLooper(),null);


        /*ConnectivityManager connectivityManager = (ConnectivityManager)getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager !=null){
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo!=null){
                Log.e("test","ConnectivityManager.TYPE_WIFI : " + ConnectivityManager.TYPE_WIFI);
                Log.e("test","ConnectivityManager.TYPE_MOBILE : " + ConnectivityManager.TYPE_MOBILE);
                Log.e("test","networkInfo : " + networkInfo.getType());
            }else {
                Log.e("test","networkInfo : null");
            }

        }*/




        checkPermission(this);
        setHandler();
        getConnectedInfo(getBaseContext());
        preStateCheckConnect();



    }

    /**
     * 이전 상태를 가져와서 미리 연결 되거나,
     * 테더링이 켜져잇다면 Accept
     * wifi가 켜져잇다면 연결 요청
     * */
    private void preStateCheckConnect(){
        setScanBroadcastReceiver(getBaseContext());
        if(new WifiApManager().isApOn(getBaseContext())){
            new SocketServer().start();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            unregisterReceiver(scanBroadCastReceiver);
            scanBroadCastReceiver = null;
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


    public void setWifiBroadcastReceiver(Context context){
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
                    preConnectWifi(context);
                }
            }
        };


        IntentFilter intentFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        context.registerReceiver(broadcastReceiver,intentFilter);
    }

    private void preConnectWifi(Context context){
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

    public void setScanBroadcastReceiver(Context context){

        if(scanBroadCastReceiver != null){
            try{
                unregisterReceiver(scanBroadCastReceiver);
            }catch (Exception e){
                e.printStackTrace();
            }
            scanBroadCastReceiver = null;
        }
        broadCastWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        Log.e("MainActivity","isWifiEnabled : " + broadCastWifiManager.isWifiEnabled());
        if (broadCastWifiManager != null && broadCastWifiManager.isWifiEnabled()) {
            preConnectWifi(context);

        }else {
            if (broadCastWifiManager != null) {
                broadCastWifiManager.startScan();
            }

            scanBroadCastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
                        List<ScanResult> scanResults = broadCastWifiManager.getScanResults();
                        for(int i = 0 ; i < scanResults.size() ; i++){
                            if(scanResults.get(i).SSID.equals("MureungTest")){
                                new MainView().connectWifi(context);
                                unregisterReceiver(scanBroadCastReceiver);
                                scanBroadCastReceiver = null;
                            }
                        }


                    }


                }
            };


            IntentFilter intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            context.registerReceiver(scanBroadCastReceiver,intentFilter);
        }

    }

    private void setHandler(){
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
    }




}
