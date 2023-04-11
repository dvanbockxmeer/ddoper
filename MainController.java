package nl.minvenw.rws.loadtest;

import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class MainController {

    private static Logger LOGGER = LoggerFactory.getLogger(MainController.class);

    private static final String DD_OPER = "https://ddapi.rws.nl/dd-oper/2.0/locations";

    @Value("${keystorePassword}")
    private String keystorePassword;

    @Value("${keystoreFile}")
    private String keystoreFile;

    @Value("${certFile}")
    private String certFile;

    @Value("${certPassword}")
    private String certPassword;

    public ResponseEntity readDDOper(String[] parameters) {
        try {
            JsonNode node = requestJSON(DD_OPER);
            return ResponseEntity.ok(node);
        } catch (Exception e) {
            LOGGER.error("{}", e);
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return new ResponseEntity<>("Error", status);
        }
    }

    private JsonNode requestJSON(String urlString) throws Exception {
        InputStream keyStream = getClass().getClassLoader().getResourceAsStream(keystoreFile);
        KeyStore clientStore = KeyStore.getInstance("PKCS12");
        clientStore.load(keyStream, keystorePassword.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(clientStore, keystorePassword.toCharArray());
        KeyManager[] kms = kmf.getKeyManagers();

        InputStream certStream = getClass().getClassLoader().getResourceAsStream(certFile);
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(certStream, certPassword.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        TrustManager[] tms = tmf.getTrustManagers();

        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kms, tms, new SecureRandom());
        SSLContext.setDefault(sslContext);

        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        URL url = new URL(urlString);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        con.connect();

        InputStream is = con.getInputStream();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(is);

        con.disconnect();

        is.close();
        return node;
    }

}
