package main.java.com.ffc.bot;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;

public class HttpClient {

    // one instance, reuse
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();

    public static int getChannelMembersCount(String chatId) throws Exception {
        //https://api.telegram.org/bot<token-here>/getChatMembersCount?chat_id=<id-number>
        StringBuilder sb = new StringBuilder();
        sb
                .append("https://api.telegram.org/bot")
                .append(PropertiesReader.getProperty("bot_token"))
                .append("/getChatMembersCount?chat_id=")
                .append(chatId);
        JSONObject obj = getRequest(sb.toString());
        return obj.getInt("result");
    }

    private static JSONObject getRequest(String uri) throws IOException, URISyntaxException {
        HttpGet request = new HttpGet(uri);

        // add request headers
        request.addHeader("custom-key", "mkyong");
        request.addHeader(HttpHeaders.USER_AGENT, "Googlebot");

        try (CloseableHttpResponse response = httpClient.execute(request)) {

            HttpEntity entity = response.getEntity();
            // return it as a String
            String result = EntityUtils.toString(entity);
//            System.out.println(result);

            JSONObject obj = new JSONObject(result);
            return obj;
        }
    }

}
