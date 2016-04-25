package com.cmj.network;

import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by 陈茂建 on 2016/4/8.
 */
public class BasicNetwork {
    public static String BASIC_HOST = "http://127.0.0.1/";
    static long i = 0;
    Handler handler;

    public BasicNetwork(Handler handler) {
        this.handler = handler;
    }

    public void httpPost(final String url, final String paramMap, final int feedback) {
        new Thread() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = feedback;
                long i_now = i;
                msg.obj = executePost(url, paramMap);
                if (i_now == i) {
                    handler.sendMessage(msg);
                }
                i++;
            }
        }.start();
    }

    private HttpDataObject executePost(String url, String params) {
        try {
            URL posturl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) posturl.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("POST");
//            connection.setRequestHead();
            connection.setDoOutput(true);
            connection.getOutputStream().write(params.getBytes());
            connection.connect();
            return getEntry(connection.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HttpDataObject(HttpDataObject.REQUEST_ERROR, "");
    }

    private HttpDataObject getEntry(InputStream inputStream) {
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(isr);
        try {
            return new HttpDataObject(br.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HttpDataObject(HttpDataObject.REQUEST_ERROR, "");
    }

    /**
     * 关于GET请求的内容
     **/
    public void httpGet(final String url, final int feedback) {
        new Thread() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = feedback;
                long i_now = i;
                msg.obj = executeGet(url);
                if (i_now == i) {
                    handler.sendMessage(msg);
                }
                i++;
            }
        }.start();
    }

    private HttpDataObject executeGet(String url) {
        try {
            URL getUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.connect();
            return getEntry(connection.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HttpDataObject(HttpDataObject.REQUEST_ERROR, "");
    }

    public void httpFile(final String url, final int feedback, final FileInputStream fis) {
        new Thread() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = feedback;
                long i_now = i;
                msg.obj = uploadFile(url, fis);
                if (i_now == i) {
                    handler.sendMessage(msg);
                }
                i++;
            }
        }.start();
    }

    public HttpDataObject uploadFile(String URL, FileInputStream fis) {
        try {
            String BOUNDARY = "---------7d4a6d158c9"; // 定义数据分隔线
            URL url = new URL(URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

            OutputStream out = new DataOutputStream(conn.getOutputStream());
            byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();// 定义最后数据分隔线

            StringBuilder sb = new StringBuilder();
            sb.append("--");
            sb.append(BOUNDARY);
            sb.append("\r\n");
            sb.append("Content-Disposition: form-data;name=\"face\";filename=\"" + "face.png" + "\"\r\n");
            sb.append("Content-Type: image/png\n\r\n\r\n");

            byte[] data = sb.toString().getBytes();
            out.write(data);
//            DataInputStream in = new Strea(fis);
            int bytes;
            byte[] bufferOut = new byte[1024];
            while ((bytes = fis.read(bufferOut)) != -1) {
                out.write(bufferOut, 0, bytes);
            }
            out.write("\r\n".getBytes()); //多个文件时，二个文件之间加入这个
            fis.close();
            out.write(end_data);
            out.flush();

            // 定义BufferedReader输入流来读取URL的响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = reader.readLine();
            reader.close();
            out.close();
            return new HttpDataObject(line);
        } catch (Exception e) {
            System.out.println("发送POST请求出现异常！" + e);
            e.printStackTrace();
        }
        return new HttpDataObject(HttpDataObject.REQUEST_ERROR, "");
    }

    public class HttpDataObject {
        public static final int REQUEST_ERROR = 0;
        private final int OK = 200;
        public int STATUS;
        public String Data;
        public String message;

        public HttpDataObject(String line) {
            this.Data = line;
            STATUS = OK;
        }

        public HttpDataObject(int status, String data) {
            this.Data = data;
            this.STATUS = status;
        }

        void setStatus(int status) {
            this.STATUS = status;
        }

        void setMessage(String message) {
            this.message = message;
        }
    }

    public static void setBaseHost(String host) {
        BASIC_HOST = host;
    }
}
