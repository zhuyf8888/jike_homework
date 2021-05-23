package zhuyf.gateway.filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;

public class ProxyBizFilter implements HttpRequestFilter {

    @Override
    public void filter(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {

        String uri = fullRequest.uri();
        if("/nofilter".equals(uri) || "/favicon.ico".equals(uri)) {
            return;
        }

        // 验证用户访问权限
        String tokenVal = "123456";
        HttpHeaders headers = fullRequest.headers();
        if(headers == null) {
            throw new RuntimeException("没有访问url:" + uri+"的权限");
        }

        String token = headers.get("token");
        if(!tokenVal.equals(token)) {
            throw new RuntimeException("没有访问url:" + uri+"的权限");
        }

        headers.add("proxy-tag", this.getClass().getSimpleName());
    }
}
