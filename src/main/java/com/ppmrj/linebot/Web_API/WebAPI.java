package com.ppmrj.linebot.Web_API;

import com.ppmrj.linebot.Web_API.Model.*;

import retrofit2.Call;
import retrofit2.http.*;

public interface WebAPI {

    @POST("grup/{nama}")
    Call<GroupResponse> registerGroup(@Path("nama") String nama, @Body Group group);

    @GET("grup/{id}")
    Call<GroupResponse> getGrup(@Path("id") String id);

    @DELETE("grup/{id}")
    Call<GroupResponse> unregisterGrup(@Path("id") String id);

    @POST("divisi")
    Call<DivisiResponse> addDivisi(@Body String nama);

    @GET("grup/divisi/{nama}")
    Call<GroupResponse> getDivisiGrup(@Path("nama") String nama);
}
