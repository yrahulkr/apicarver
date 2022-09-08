package com.apicarv.testCarver.apirunner;

import com.apicarv.testCarver.apirecorder.NetworkEvent;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Test;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class APIRunnerTest {
    CloseableHttpClient httpClient;
    @Before
    public void initClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        httpClient = HttpClients
                .custom()
                .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
    }
    @Test
    public void testExecuteRequest() {
//         = HttpClients.createDefault();
        NetworkEvent logEntry = new NetworkEvent(0);
        logEntry.setMethod(NetworkEvent.MethodClazz.GET);
        logEntry.setRequestUrl("https://self-signed.badssl.com");
        logEntry.setClazz(NetworkEvent.EventClazz.Probe);
        logEntry.setHeaders(new ArrayList<>());
        APIResponse returnResponse = new APIResponse(0);

        returnResponse.setRequest(logEntry);
        Map<String, String> cookieCache = new HashMap<>();
        APIResponse.Status status = APIRunner.executeRequest(httpClient, logEntry, returnResponse, cookieCache, false);
        System.out.println(returnResponse.getResponse().getStatus());
    }

    @Test
    public void testAPIRunner(){
        APIRunner.main(new String[]{"ecomm", "20220827_151530", "20220827_154949"});
    }
}