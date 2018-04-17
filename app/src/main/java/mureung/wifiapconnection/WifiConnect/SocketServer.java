package mureung.wifiapconnection.WifiConnect;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import mureung.wifiapconnection.MainActivity;
import mureung.wifiapconnection.MainView;

public class SocketServer extends Thread {

    private static boolean Server_FLAG = false;

    private static HashMap<String,DataOutputStream> clients;
    private ServerSocket serverSocket = null;
    public static final String allConnect = "All";
    public static String ipAddressConnect = null;


    /*public static void main(String[] args){
        new SocketServer().start();
        Log.e("SocketServer","main");
    }*/

    public static boolean isServer_FLAG() {
        return Server_FLAG;
    }


    public SocketServer(){
        if(serverSocket != null){
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void run() {
        int port = 9990;
        Socket socket = null;
        clients = new HashMap<String,DataOutputStream>();
        Collections.synchronizedMap(clients);
        try {
            serverSocket = new ServerSocket(port);
            Log.e("SocketServer","접속 대기 중");
            while(true){
                socket = serverSocket.accept();
                InetAddress ip = socket.getInetAddress();
                String strIp = ip.toString().substring(1,ip.toString().length());
                Log.e("SocketServer","Connected Ip Address : " + strIp);
                new SocketConnectThread(socket).start();
                Server_FLAG = true;
            }
        }catch (Exception e){
            Server_FLAG = false;
            e.printStackTrace();

        }

    }


    class SocketConnectThread extends Thread{
        Socket socket = null;
        String mac = null;
        String connectIp = null;
        String msg = null;

        DataInputStream input;
        DataOutputStream output;


        public SocketConnectThread(Socket socket){
            this.socket = socket;
            try {
                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());
            }catch (Exception e){
                e.printStackTrace();
                cancel();
            }
        }

        @Override
        public void run() {
            try {


                String strIpAddress = null;
                strIpAddress = input.readUTF();
                Log.e("SocketServer","IP Address : " + strIpAddress);
                clients.put(strIpAddress,output);
                sendMsg(strIpAddress+"   접속");
                new MainView().addListItem(strIpAddress);

                while (input != null){
                    try {

                        String temp = input.readUTF();
                        sendMsg("OK");
                        //sendMsg(temp);
                        Log.e("SocketServer","temp : " + temp);
                        if(MainActivity.mainAcitivityHandler!=null){
                            MainActivity.mainAcitivityHandler.obtainMessage(1,temp).sendToTarget();
                        }
                    }catch (Exception e){
                        socket.close();
                        clients.remove(strIpAddress);
                        new MainView().removeListItem(strIpAddress);
                        e.printStackTrace();
                        break;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                cancel();
            }
        }

        public void cancel(){
            Server_FLAG = false;
            try {
                if(serverSocket!=null){
                    serverSocket.close();
                }

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void setIpAddressConnect(String ipAddress){
        Log.e("SocketServer","setIpAddressConnect ipAddress : " + ipAddress);
        if(ipAddress!=null){
            ipAddressConnect = ipAddress;
        }else {
            ipAddressConnect = allConnect;
        }
    }

    public static void sendMsg(String msg){
        Log.e("test","ipAddressConnect : " + ipAddressConnect);
        if(ipAddressConnect != null){
            if(ipAddressConnect.equals(allConnect)){
                allSendMsg(msg);
            }else {
                targetSendMsg(ipAddressConnect,msg);
            }
        }else {
            allSendMsg(msg);
        }

    }
    private static void allSendMsg(String msg){
        Log.e("SocketServer","allSendMsg msg : " + msg);
        Iterator<String> it = clients.keySet().iterator();

        while(it.hasNext()){
            try {
                OutputStream dos = clients.get(it.next());
                DataOutputStream output = new DataOutputStream(dos);
                output.writeUTF(msg);

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private static void targetSendMsg(String ipAddress , String msg){
        Log.e("SocketServer","targetSendMsg ipAddress : " + ipAddress + " , msg : " + msg + " , clients.size() : " + clients.size());
        if(!clients.isEmpty()){
            try {
                OutputStream dos = clients.get(ipAddress);
                Log.e("SocketServer","targetSendMsg clients.get(ipAddress) : " + clients.get(ipAddress));
                DataOutputStream output = new DataOutputStream(dos);
                output.writeUTF(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
