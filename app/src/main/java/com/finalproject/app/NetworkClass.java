package com.finalproject.app;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 獲取HTML數據
 */
public class NetworkClass {

    public static String getHtml(String path) throws Exception {
        // 通過網絡地址創建URL對象
        URL url = new URL(path);
        // 根據URL
        // 打開連接，URL.openConnection函數會根據URL的類型，返回不同的URLConnection子類的對象，這裏URL是一個http，因此實際返回的是HttpURLConnection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        // 設定URL的請求類別，有POST、GET 兩類
        conn.setRequestMethod("GET");
        //設置從主機讀取數據超時（單位：毫秒）
        conn.setConnectTimeout(5000);
        //設置連接主機超時（單位：毫秒）
        conn.setReadTimeout(5000);
        // 通過打開的連接讀取的輸入流,獲取html數據
        InputStream inStream = conn.getInputStream();
        // 得到html的二進制數據
        byte[] data = readInputStream(inStream);
        // 是用指定的字符集解碼指定的字節數組構造一個新的字符串
        String html = new String(data, "BIG5");
        return html;
    }

    /**
     * 讀取輸入流，得到html的二進制數據
     *
     * @param inStream
     * @return
     * @throws Exception
     */
    public static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }

}