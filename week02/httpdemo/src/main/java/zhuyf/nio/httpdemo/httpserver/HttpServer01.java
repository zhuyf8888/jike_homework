package zhuyf.nio.httpdemo.httpserver;

import com.sun.xml.internal.ws.api.pipe.ContentType;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * HttpServer01
 */
public class HttpServer01 {

    /**
     * 启动httpserver
     */
    public void startServer() {

        try {
            ServerSocket serverSocket = new ServerSocket(8801);
            while(true) {
                Socket socket = serverSocket.accept();
                this.handleSocket(socket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * http请求处理
     * @param socket
     */
    private void handleSocket(Socket socket) throws IOException {

        PrintWriter pw = new PrintWriter(socket.getOutputStream(),true);
        pw.println("HTTP/1.1 200 OK");
        pw.println("Content-Type:text/html;charset=utf-8");
        String body = "hello,httpserver01";
        pw.println("Content-Length:"+body.getBytes().length);
        pw.println();
        pw.write(body);
        pw.close();
        socket.close();
    }
}
