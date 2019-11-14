package won.bot.jokebot.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import won.bot.jokebot.api.model.ChuckNorrisJoke;

/**
 * @author ms Handles all needed webrequests
 */
public class JokeBotsApi {
    private String jsonURL;

    public JokeBotsApi(String jsonURL) {
        this.jsonURL = jsonURL;
    }

    public ChuckNorrisJoke fetchJokeData() {
        ChuckNorrisJoke joke = null;
        CloseableHttpResponse response = null;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet getRequest = new HttpGet(jsonURL);
            getRequest.addHeader("accept", "application/json");
            response = httpClient.execute(getRequest);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            JSONObject json = new JSONObject(sb.toString());
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                ChuckNorrisJoke tmpJoke = objectMapper.readValue(json.toString(),
                                ChuckNorrisJoke.class);
                joke = tmpJoke != null ? tmpJoke : null;
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return joke;
    }
}
