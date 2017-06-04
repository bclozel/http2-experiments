package http2xp;

import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.OkHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class Http2IntegrationTest {

    @Value("${server.ssl.key-store-password}")
    private String sslKeyStorePassword;

    @Value("${server.ssl.protocol}")
    private String sslProtocol;

    private RestTemplate restTemplate;

    @Before
    public void setup() throws Exception {

        Resource keyStoreFile = new ClassPathResource("keystore.jks");
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(keyStoreFile.getInputStream(), sslKeyStorePassword.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);
        SSLContext ctx = SSLContext.getInstance(sslProtocol);
        ctx.init(null, tmf.getTrustManagers(), null);

        OkHttpClient okHttpClient = new OkHttpClient
				.Builder()
				.sslSocketFactory(ctx.getSocketFactory())
				.hostnameVerifier((s, sslSession) -> true)
				.build();
        this.restTemplate = new RestTemplate(new OkHttp3ClientHttpRequestFactory(okHttpClient));
    }


    @Test
    public void testHelloEndpoint() {

        ResponseEntity<String> response = this.restTemplate
                .getForEntity("https://localhost:8443/hello?name=Spring", String.class);
        Assert.assertEquals("Hello Spring", response.getBody());
    }

}
