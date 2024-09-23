package com.lius.heath.service.config;

import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyStore;

public class HttpsClientHttpRequestFactory extends SimpleClientHttpRequestFactory {
    @Override
    protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
        try {
            if (connection instanceof HttpsURLConnection) {// https协议，修改协议版本
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                // 信任任何链接,忽略对证书的校验
                TrustStrategy anyTrustStrategy = (x509Certificates, s) -> true;
                //自定义SSLContext
                SSLContext ctx = SSLContexts.custom().loadTrustMaterial(trustStore, anyTrustStrategy).build();
                // ssl问题
                ((HttpsURLConnection) connection).setSSLSocketFactory(ctx.getSocketFactory());
                //解决No subject alternative names matching IP address xxx.xxx.xxx.xxx found问题
                ((HttpsURLConnection) connection).setHostnameVerifier((s, sslSession) -> true);
                HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
                super.prepareConnection(httpsConnection, httpMethod);
            } else { // http协议
                super.prepareConnection(connection, httpMethod);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
