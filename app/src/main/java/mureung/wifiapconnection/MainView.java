package mureung.wifiapconnection;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * Created by user on 2018-01-29.
 */

public class MainView extends Fragment implements View.OnClickListener {

    LinearLayout pidTestBtn,pidScheduleBtn;
    LinearLayout wifiConnect,wifiTether;
    LinearLayout wifiTestButton;
    ImageView bluetoothIcon;

    TextView wifiTetherText , wifiConnectText;

    Handler mainViewHandler;



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mainview,container,false);

        wifiConnect = (LinearLayout)view.findViewById(R.id.wifiConnect);
        wifiConnect.setOnClickListener(this);
        wifiTether = (LinearLayout)view.findViewById(R.id.wifiTether);
        wifiTether.setOnClickListener(this);
        wifiTetherText = (TextView)view.findViewById(R.id.wifiTetherText);

        wifiConnectText = (TextView)view.findViewById(R.id.wifiConnectText);






        setMainViewHandler();






        PageStr.setPageStrData(PageStr.Mainview);




        return view;
    }

    public final int wifiConnectState = 1;
    public final int wifiTetherState = 2;


    private void setMainViewHandler(){
        mainViewHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case wifiConnectState :
                        wifiConnectText.setText(String.valueOf(msg.obj));
                        break;
                    case wifiTetherState :
                        wifiTetherText.setText(String.valueOf(msg.obj));
                        break;
                }
                return true;
            }
        });
    }

    public void setWifiConnectText(String text){
        if(mainViewHandler != null){
            mainViewHandler.obtainMessage(wifiConnectState,text).sendToTarget();
        }
    }
    public void setWifiTetherText(String text){
        if(mainViewHandler != null){
            mainViewHandler.obtainMessage(wifiTetherState,text).sendToTarget();
        }
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.wifiConnect:
                Log.e("MainView","wifiConnect");
                ConnectivityManager manager ;
                WifiManager wifiManager = (WifiManager)getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                manager = (ConnectivityManager)getContext().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if(wifi.isConnected()){
                    Log.e("Wifi","Wifi 가 이미 켜져있음");
                }else {
                    Log.e("Wifi","Wifi 가 꺼져 있음");
                    if (wifiManager != null) {
                        wifiManager.setWifiEnabled(true);
                    }
                }

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

                int netId = wifiManager.addNetwork(wifiConfig);
                wifiManager.disconnect();
                wifiManager.enableNetwork(netId,true);
                wifiManager.reconnect();



                break;

            case R.id.wifiTether:

                if(new MainActivity().configApState(getContext().getApplicationContext())){
                    Log.e("MainView","wifiTether 테더링 성공");
                }else {
                    Log.e("MainView","wifiTether 테더링 실패");
                }

                break;

        }

    }




}