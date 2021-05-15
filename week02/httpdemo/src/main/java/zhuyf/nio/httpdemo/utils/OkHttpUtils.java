package zhuyf.nio.httpdemo.utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class OkHttpUtils {

    // 缓存客户端实例
    public static OkHttpClient client = new OkHttpClient();

    // GET 调用
    public static String requestHttpserver(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static void main(String[] args) throws IOException {

        String url = "http://localhost:8801";
        String resp = OkHttpUtils.requestHttpserver(url);
        System.out.println(resp);
    }
}
