package com.finalproject.app.ui.VideoStream;

import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

public class ClientThread extends Thread implements Parcelable {

    private String dstAddress;
    private int dstPort;
    private static volatile boolean running = false;
    private VideoStream.ClientHandler handler;

    private Socket socket;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    ClientThread(String addr, int port, VideoStream.ClientHandler handler) {
        super();
        dstAddress = addr;
        dstPort = port;
        this.handler = handler;
    }

    protected ClientThread(Parcel in) {
        dstAddress = in.readString();
        dstPort = in.readInt();
    }

    boolean isRunning() {
        return running;
    }

    void setRunning(boolean running){
        this.running = running;
    }

    private void sendState(String state, String color){
        handler.sendMessage(
                Message.obtain(handler,
                        VideoStream.ClientHandler.UPDATE_STATE, state + ":" + color));
    }

    void txMsg(String msgToSend){
        if(printWriter != null){
            printWriter.printf(msgToSend);
        }
    }

    private void sendStart(){
        handler.sendMessage(
                Message.obtain(handler,
                        VideoStream.ClientHandler.UPDATE_START));
    }

    private void sendError(){
        handler.sendMessage(
                Message.obtain(handler,
                        VideoStream.ClientHandler.UPDATE_ERROR));
    }

    @Override
    public void run() {
        sendState("connecting...", "#707070");

        running = true;

        try {
            socket = new Socket(dstAddress, dstPort);
            sendState("connected", "#28FF28");
            sendStart();

            OutputStream outputStream = socket.getOutputStream();
            printWriter = new PrintWriter(outputStream, true);
            while (running);
        } catch (ConnectException e){
            sendError();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(bufferedReader != null){
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(printWriter != null){
                printWriter.close();
            }

            if(socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        handler.sendEmptyMessage(VideoStream.ClientHandler.UPDATE_END);
    }

    public static final Creator<ClientThread> CREATOR = new Creator<ClientThread>() {
        @Override
        public ClientThread createFromParcel(Parcel in) {
            ClientThread clientThread = new ClientThread(in.readString(), in.readInt(), (VideoStream.ClientHandler)in.readValue(VideoStream.ClientHandler.class.getClassLoader()));
            clientThread.socket = (Socket)in.readValue(Socket.class.getClassLoader());
            clientThread.printWriter = (PrintWriter)in.readValue(PrintWriter.class.getClassLoader());
            clientThread.bufferedReader = (BufferedReader)in.readValue(BufferedReader.class.getClassLoader());
            clientThread.setRunning(in.readInt() == 1);
            return clientThread;
        }

        @Override
        public ClientThread[] newArray(int size) {
            return new ClientThread[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        try {
            dest.writeString(dstAddress);
            dest.writeInt(dstPort);
            dest.writeValue(handler);
            dest.writeInt(isRunning() ? 1 : 0);

            dest.writeValue(socket);
            dest.writeValue(printWriter);
            dest.writeValue(bufferedReader);
        }catch (RuntimeException e){

        }
    }
}
