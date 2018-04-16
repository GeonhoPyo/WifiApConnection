package mureung.wifiapconnection.WifiConnect;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import mureung.wifiapconnection.MainActivity;

public class SocketClient{
    private static boolean Client_FLAG = false;
    static ReceiveThread receive;

    static boolean threadAlive;
    static String ip;
    static String port;

    static Socket socket;

    OutputStream outputStream = null;
    BufferedReader bufferedReader = null;
    private static  DataOutputStream output = null;
    private int count = 0;


    public static boolean isClient_FLAG() {
        return Client_FLAG;
    }


    public SocketClient(){

    }


    public SocketClient(String ip, String port){
        if(!threadAlive){
            threadAlive = true;
            this.ip= ip;
            this.port = port;
            new SocketConnect().start();
        }

    }

    class SocketConnect extends Thread{
        @Override
        public void run() {
            try {
                socket = new Socket(ip,Integer.parseInt(port));
                output = new DataOutputStream(socket.getOutputStream());
                receive = new ReceiveThread(socket);
                receive.start();


                String strIpAddress = null;
                WifiManager wifiManager = (WifiManager)MainActivity.mainContext.getSystemService(Context.WIFI_SERVICE);
                DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
                int ipAddress = dhcpInfo.ipAddress;
                strIpAddress = String.format("%d.%d.%d.%d",(ipAddress&0xff),(ipAddress>>8&0xff),(ipAddress>>16&0xff),(ipAddress>>24&0xff));

                output.writeUTF(strIpAddress);



                Client_FLAG =true;
            }catch (Exception e){
                e.printStackTrace();
                cancel();
            }

        }
        public void cancel (){
            Client_FLAG = false;
            try {
                if(socket!=null){
                    socket.close();
                }

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }



    class ReceiveThread extends Thread{
        DataInputStream input;

        public ReceiveThread(Socket socket){
            try {
                input = new DataInputStream(socket.getInputStream());
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                while(input != null){
                    String msg = input.readUTF();
                    Log.e("SocketClient","ReceiveThread : " + msg);
                    if(!msg.equals("OK")){
                        if(MainActivity.mainAcitivityHandler!=null){
                            MainActivity.mainAcitivityHandler.obtainMessage(1,"Client msg : "+ msg ).sendToTarget();
                        }
                    }

                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    class SendThread extends  Thread{

        String sendMsg ;
        DataOutputStream output;
        public SendThread( String sendMsg){

            this.sendMsg = sendMsg;
            try {
                output = new DataOutputStream(socket.getOutputStream());
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            try {
                String strIpAddress = null;
                WifiManager wifiManager = (WifiManager)MainActivity.mainContext.getSystemService(Context.WIFI_SERVICE);
                DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
                int ipAddress = dhcpInfo.ipAddress;
                strIpAddress = String.format("%d.%d.%d.%d",(ipAddress&0xff),(ipAddress>>8&0xff),(ipAddress>>16&0xff),(ipAddress>>24&0xff));

                if(output != null){
                    if(sendMsg != null){
                        output.writeUTF(strIpAddress + "<GeonHo>"+sendMsg);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void sendMsg (String msg){
        new SendThread(msg).start();
    }
}
