package io.alstonlin.thelearninglock;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Data Access Object. Handles any network requests.
 */
public class DAO {

    public static final String WEATHER_URL = "http://api.worldweatheronline.com/free/v2/weather.ashx";

    /**
     * Uses the Weather API to do a RESTful Call for weather and returns the Weather img URL and temperature
     * @param longitude The user's longitude
     * @param latitude The user's latitude
     * @return A String[2] with [0] -> URL, [1] -> Temperature
     * @throws JSONException Something went wrong
     */
    public static String[] getWeather(final String WEATHER_API_KEY, double longitude, double latitude) {
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            String query = WEATHER_URL + "?key=" + WEATHER_API_KEY + "&q=" + latitude + "," + longitude  + "&format=json";
            HttpGet getRequest = new HttpGet(query);
            getRequest.addHeader("accept", "application/json");
            HttpResponse response = httpClient.execute(getRequest);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
            }
            HttpEntity entity = response.getEntity();
            JSONObject output = new JSONObject(EntityUtils.toString(entity, "UTF-8"));
            String[] result = new String[2];
            JSONObject currentWeather = (JSONObject) output.getJSONObject("data").getJSONArray("current_condition").get(0);
            result[0] = ((JSONObject)currentWeather.getJSONArray("weatherIconUrl").get(0)).getString("value");
            result[1] = currentWeather.getString("temp_C");
            httpClient.getConnectionManager().shutdown();
            return result;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
}