package zhuyf.nio.httpdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import zhuyf.nio.httpdemo.httpserver.HttpServer01;
import zhuyf.nio.httpdemo.utils.OkHttpUtils;

@SpringBootApplication
public class HttpdemoApplication {

	public static void main(String[] args) {

		HttpServer01 httpServer = new HttpServer01();
		httpServer.startServer();
	}

}
