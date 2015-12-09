package com.gcm.younghyup.gcm.RestRequest;

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
        @PUT("/api/user/gcm/")
        Call<Item> sendID(
                @Field("id") String id);

        @FormUrlEncoded
        @POST("/api/user/confirm")
        Call<Item> sendConfirm(
            @Field("status") boolean status);
    }
}