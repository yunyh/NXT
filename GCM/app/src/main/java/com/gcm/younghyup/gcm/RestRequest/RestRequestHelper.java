package com.gcm.younghyup.gcm.RestRequest;

/**
 * Created by YoungHyub on 2015-03-20.
 */
import com.google.gson.JsonObject;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.http.*;
import retrofit.Retrofit;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class RestRequestHelper {

    private static RestRequestHelper instance;

    private RestCookieManager cookieManager;
    private Retrofit retrofitAdapter;
    private RestRequest restRequest;

    public RestRequestHelper() {
        cookieManager = new RestCookieManager();
        CookieHandler.setDefault(cookieManager);

  //      restAdapter = new RestAdapter.Builder().setEndpoint("http://uzuki.me:5000").setLogLevel(RestAdapter.LogLevel.FULL).build();

    }

    public RestRequest getService(){
        retrofitAdapter = new Retrofit.Builder()
                .baseUrl("http://uzuki.me:5000")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofitAdapter.create(RestRequest.class);
    }

    public interface RestRequest {
        @FormUrlEncoded
        @POST("/api/user/gcm")
        Call<User> sendID(
                @Field("id") String id);

    }

    public class User {
        private String id;

        public String getRet() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

}

class RestCookieManager extends CookieManager {

    private String currentCookie;

    @Override
    public void put(URI uri, Map<String, List<String>> stringListMap) throws IOException {
        super.put(uri, stringListMap);
        if (stringListMap != null && stringListMap.get("Set-Cookie") != null)
            for (String string : stringListMap.get("Set-Cookie")) {
                if (string.contains("session")) {
                    currentCookie = string;
                }
            }
    }

    public String getCurrentCookie() {
        return currentCookie;
    }
}