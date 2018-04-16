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

public class SocketServer extends Thread {

    private static boolean Server_FLAG = false;

    private static HashMap<String,DataOutputStream> clients;
    private ServerSocket serverSocket = null;


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
        clients = new HashMap<String,DataOutputStream>();
        Collections.synchronizedMap(clients);
    }

    @Override
    public void run() {
        int port = 9990;
        Socket socket = null;
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
                        sendMsg("No Message");
                        e.printStackTrace();
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

    public static void sendMsg(String msg){
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

}
