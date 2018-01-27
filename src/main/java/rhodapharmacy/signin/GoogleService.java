package rhodapharmacy.signin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class GoogleService {

    private static Logger log = LoggerFactory.getLogger(GoogleService.class);

    public static final String GOOGLE_TOKEN_RETRIEVE_URL = "https://www.googleapis.com/oauth2/v4/token";
    public static final String GOOGLE_ACCESS_TOKEN_INFO_URL = "https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=";

    private ObjectMapper objectMapper;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUrl;

    public GoogleService(SecuredConfig securedConfig) {
        objectMapper = new ObjectMapper();
        clientId      = securedConfig.getProperty("google.clientId");
        clientSecret  = securedConfig.getProperty("google.clientSecret");
        if(clientId == null || clientId.isEmpty() || clientSecret == null || clientSecret.isEmpty()) {
            throw new IllegalStateException("could not resolve google client credentials");
        }
        redirectUrl   = securedConfig.getProperty("google.redirectUrl");
    }

    public GoogleAccessToken getAccessToken(String code)
    throws IOException {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("code", code));
        params.add(new BasicNameValuePair("client_id", clientId));
        params.add(new BasicNameValuePair("client_secret", clientSecret));
        params.add(new BasicNameValuePair("redirect_uri", redirectUrl));
        params.add(new BasicNameValuePair("grant_type", "authorization_code"));
        try {
            HttpPost post = new HttpPost(GOOGLE_TOKEN_RETRIEVE_URL);
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");
            post.setEntity(new UrlEncodedFormEntity(params));
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(post);
            GoogleAccessToken googleAccessToken = objectMapper.readValue(response.getEntity().getContent(), GoogleAccessToken.class);
            googleAccessToken.setGoogleAccessTokenInfo(getUserTokenInfo(googleAccessToken));
            return googleAccessToken;
        }
        catch (IOException e) {
            throw new IOException("failed to retrieve user profile", e);
        }
    }

    public GoogleAccessTokenInfo getUserTokenInfo(GoogleAccessToken accessToken)
    throws IOException {
        HttpGet getTokenInfo = new HttpGet(GOOGLE_ACCESS_TOKEN_INFO_URL + accessToken.getId_token());
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(getTokenInfo);
        return objectMapper.readValue(response.getEntity().getContent(), GoogleAccessTokenInfo.class);
    }
}
