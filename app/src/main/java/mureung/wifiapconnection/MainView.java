package mureung.wifiapconnection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Objects;

import mureung.wifiapconnection.WifiConnect.SocketClient;
import mureung.wifiapconnection.WifiConnect.SocketServer;
import mureung.wifiapconnection.WifiConnect.WifiListAdapter;


/**
 * Created by user on 2018-01-29.
 */

public class MainView extends Fragment implements View.OnClickListener {

    LinearLayout wifiState;
    LinearLayout wifiConnect,wifiTether;
    LinearLayout wifiTestButton,pushData;
    ImageView bluetoothIcon;
    WifiListAdapter wifiListAdapter;

    TextView wifiTetherText , wifiConnectText;
    EditText mainEditText;

    static Handler mainViewHandler;

    ListView mainViewList;
    private static ArrayList<String> wifiArrayList;


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

        wifiState = (LinearLayout)view.findViewById(R.id.wifiState);
        wifiState.setOnClickListener(this);
        pushData = (LinearLayout)view.findViewById(R.id.pushData);
        pushData.setOnClickListener(this);

        mainEditText = (EditText)view.findViewById(R.id.mainEditText);

        mainViewList = (ListView)view.findViewById(R.id.mainViewList);
        wifiArrayList = new ArrayList<String>();
        wifiArrayList.add(SocketServer.allConnect);
        wifiListAdapter = new WifiListAdapter(Objects.requireNonNull(getContext()),R.layout.list_wificonnect_item,wifiArrayList);
        mainViewList.setAdapter(wifiListAdapter);
        mainViewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new SocketServer().setIpAddressConnect(wifiArrayList.get(position));
                Log.e("test","test SocketAddress : " + SocketServer.ipAddressConnect);
            }
        });




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
                    case 3 :
                        wifiListAdapter.notifyDataSetChanged();

                        break;
                }
                return true;
            }
        });
    }

    public void addListItem(String ipAddress){
        if(!wifiArrayList.contains(ipAddress)){
            wifiArrayList.add(ipAddress);
            if(mainViewHandler!=null){
                mainViewHandler.obtainMessage(3,null).sendToTarget();
            }
        }

    }
    public void removeListItem(String ipAddress){
        if(wifiArrayList.contains(ipAddress)){
            wifiArrayList.remove(ipAddress);

            if(mainViewHandler!=null){

                mainViewHandler.obtainMessage(3,null).sendToTarget();
            }
        }
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
                connectWifi(getContext());



                break;

            case R.id.wifiTether:

                if(new WifiApManager().configApState(getContext().getApplicationContext())){
                    Log.e("MainView","wifiTether 테더링 성공");
                }else {
                    Log.e("MainView","wifiTether 테더링 실패");
                }





                break;

            case R.id.wifiState:
                //현재 연결된 wifi ip 정보 가져오기
                WifiManager wm = (WifiManager)getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                DhcpInfo dhcpInfo = wm.getDhcpInfo();
                int ipAddress = dhcpInfo.ipAddress;
                int netmask = dhcpInfo.netmask;
                int gateway = dhcpInfo.gateway;
                int serverAddress = dhcpInfo.serverAddress;
                int dns1 = dhcpInfo.dns1;
                int dns2 = dhcpInfo.dns2;



                String strIpAddress = String.format("%d.%d.%d.%d",(ipAddress&0xff),(ipAddress>>8&0xff),(ipAddress>>16&0xff),(ipAddress>>24&0xff));
                String strNetMask = String.format("%d.%d.%d.%d",(netmask&0xff),(netmask>>8&0xff),(netmask>>16&0xff),(netmask>>24&0xff));
                String strGateway = String.format("%d.%d.%d.%d",(gateway&0xff),(gateway>>8&0xff),(gateway>>16&0xff),(gateway>>24&0xff));
                String strServerAddress = String.format("%d.%d.%d.%d",(serverAddress&0xff),(serverAddress>>8&0xff),(serverAddress>>16&0xff),(serverAddress>>24&0xff));
                String strDns1 = String.format("%d.%d.%d.%d",(dns1&0xff),(dns1>>8&0xff),(dns1>>16&0xff),(dns1>>24&0xff));
                String strDns2 = String.format("%d.%d.%d.%d",(dns2&0xff),(dns2>>8&0xff),(dns2>>16&0xff),(dns2>>24&0xff));
                Log.e("MainView ","wifiState strIpAddress : " + strIpAddress);
                Log.e("MainView ","wifiState strNetMask : " + strNetMask);
                Log.e("MainView ","wifiState strGateway : " + strGateway);
                Log.e("MainView ","wifiState strServerAddress : " + strServerAddress);
                Log.e("MainView ","wifiState strDns1 : " + strDns1);
                Log.e("MainView ","wifiState strDns2 : " + strDns2);
                /*try {
                    Socket socket = new Socket(ipAddress,80);
                    String localAddress = socket.getLocalAddress().getHostAddress();
                    Log.e("test","localAddress : " + localAddress);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                //Log.e("MainView","getMyIpAddress : " + getMyIpAddress());

                break;

            case R.id.pushData :
                //Client.connectClient();

                //Client.connectClient("연결 테스트");

                if(SocketClient.isClient_FLAG()){
                    new SocketClient().sendMsg(String.valueOf(mainEditText.getText()));
                }
                if(SocketServer.isServer_FLAG()){
                    SocketServer.sendMsg(String.valueOf(mainEditText.getText()));
                }


                break;

        }

    }

    private String getMyIpAddress(){
        try {
            Enumeration<NetworkInterface> en =  NetworkInterface.getNetworkInterfaces();
            while(en.hasMoreElements()) {
                NetworkInterface interf = en.nextElement();
                Enumeration<InetAddress> ips = interf.getInetAddresses();
                while (ips.hasMoreElements()) {
                    InetAddress inetAddress = ips.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void connectWifi(Context context){
        new MainActivity().setWifiBroadcastReceiver(context);
        Log.e("MainView","wifiConnect");
        ConnectivityManager manager ;
        WifiManager wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        manager = (ConnectivityManager)context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
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
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        wifiConfig.allowedAuthAlgorithms.clear();
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId,true);
        wifiManager.reconnect();
    }




}
