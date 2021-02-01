package com.example.weatherapiapp;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WeatherDataService {

    public static final String QUERY_FOR_CITY_ID = "https://www.metaweather.com/api/location/search/?query=";
    public static final String QUERY_FOR_CITY_WEATHER_BY_ID = "https://www.metaweather.com/api/location/";

    Context context;
    String cityId = "";

    public WeatherDataService(Context context) {
        this.context = context;
    }

    public interface VolleyResponseListener {
        void onError(String message);

        void onResponse(String cityID);
    }

    public void getCityID(String cityName, final VolleyResponseListener volleyResponseListener) {
        String url = QUERY_FOR_CITY_ID + cityName;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        cityId = "";
                        try {
                            JSONObject cityInfo = response.getJSONObject(0);
                            cityId = cityInfo.getString("woeid");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // This Worked but didn't return to main activity -> this is due to an asynchronous issue
                        //Toast.makeText(context, "City ID: " + cityId, Toast.LENGTH_SHORT).show();
                        volleyResponseListener.onResponse(cityId);
                    }
                }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
                            volleyResponseListener.onError(error.toString());
                        }
                });
            // This is used to create just one request queue for reducing the network calls
            MySingleton.getInstance(context).addToRequestQueue(request);
            //return cityId;
        }


    public interface ForeCastByIDResponse {
        void onError(String message);

        void onResponse(List<WeatherReportModel> weatherReportModel);
    }
    public void getCityForecastByID(String cityID, final ForeCastByIDResponse foreCastByIDResponse) {
        final List<WeatherReportModel> weatherReportModel = new ArrayList<>();
        // Get the Object
        String url = QUERY_FOR_CITY_WEATHER_BY_ID + cityID;
        //Toast.makeText(context, url, Toast.LENGTH_SHORT).show();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray consolidated_weather_list = response.getJSONArray("consolidated_weather");

                            for(int i = 0; i < consolidated_weather_list.length(); i++) {
                                WeatherReportModel One_day = new WeatherReportModel();
                                JSONObject first_day_from_api = (JSONObject) consolidated_weather_list.get(i);
                                One_day.setId(first_day_from_api.getInt("id"));
                                One_day.setWeather_state_name(first_day_from_api.getString("weather_state_name"));
                                One_day.setWeather_state_abbr(first_day_from_api.getString("weather_state_abbr"));
                                One_day.setWind_direction_compass(first_day_from_api.getString("wind_direction_compass"));
                                One_day.setCreated(first_day_from_api.getString("created"));
                                One_day.setApplicable_date(first_day_from_api.getString("applicable_date"));
                                One_day.setMin_temp(first_day_from_api.getLong("min_temp"));
                                One_day.setMax_temp(first_day_from_api.getLong("max_temp"));
                                One_day.setThe_temp(first_day_from_api.getLong("the_temp"));
                                One_day.setWind_speed(first_day_from_api.getLong("wind_speed"));
                                One_day.setWind_direction(first_day_from_api.getLong("wind_direction"));
                                One_day.setHumidity(first_day_from_api.getInt("humidity"));
                                One_day.setVisibility(first_day_from_api.getLong("visibility"));
                                One_day.setPredictability(first_day_from_api.getInt("predictability"));

                                weatherReportModel.add(One_day);
                            }

                            foreCastByIDResponse.onResponse(weatherReportModel);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show();
                    }
                    // Get the property consolidated_weather
                    // Get each item from teh array

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        MySingleton.getInstance(context).addToRequestQueue(request);
    }

        public interface GetCityForecastByName {
            void onError(String message);
            void onResponse(List<WeatherReportModel> weatherReportModels);
        }

        public void getCityForecastByName(String cityName, final GetCityForecastByName getCityForecastByName) {

            getCityID(cityName, new VolleyResponseListener() {
                @Override
                public void onError(String message) {

                }

                @Override
                public void onResponse(String cityID) {
                    // WE HAVE CITY ID HERE
                    getCityForecastByID(cityID, new ForeCastByIDResponse() {
                        @Override
                        public void onError(String message) {

                        }

                        @Override
                        public void onResponse(List<WeatherReportModel> weatherReportModel) {
                            // WEATHER REPORT
                            getCityForecastByName.onResponse(weatherReportModel);
                        }
                    });

                }
            });

        }
}
