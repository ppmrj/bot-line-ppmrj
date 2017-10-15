package com.ppmrj.linebot.Web_API;

import com.ppmrj.linebot.Web_API.Model.*;

import retrofit2.Call;
import retrofit2.http.*;

public interface WebAPI {

    @POST("grup/{nama}")
    Call<GrupResponse> registerGroup(@Path("nama") String nama, @Body Grup grup);

    @GET("grup/{id}")
    Call<GrupResponse> getGrup(@Path("id") String id);

    @DELETE("grup/{id}")
    Call<GrupResponse> unregisterGrup(@Path("id") String id);

    @POST("divisi")
    Call<DivisiResponse> addDivisi(@Body String nama);

    @GET("grup/divisi/{nama}")
    Call<GrupResponse> getDivisiGrup(@Path("nama") String nama);
}
