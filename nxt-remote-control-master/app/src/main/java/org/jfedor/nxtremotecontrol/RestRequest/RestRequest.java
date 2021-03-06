package org.jfedor.nxtremotecontrol.RestRequest;

/**
 * Created by YoungHyub on 2015-03-20.
 */

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.http.*;
import retrofit.Retrofit;

public class RestRequest {

 //   private RestCookieManager cookieManager;
    private Retrofit retrofitAdapter;

    public APIService getService(){
        retrofitAdapter = new Retrofit.Builder()
                .baseUrl("http://uzuki.me:5000")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofitAdapter.create(APIService.class);
    }


    public interface APIService {
        @FormUrlEncoded
        @POST("/api/user/alert/")
        Call<Item> sendAlert(
                @Field("alert") String alert);

        @FormUrlEncoded
        @PUT("/api/user/gcm/{devicetype}")
        Call<Item> sendID(
                @Field("id") String id, @Path("devicetype") String type);
    }
}
/*
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
}*/