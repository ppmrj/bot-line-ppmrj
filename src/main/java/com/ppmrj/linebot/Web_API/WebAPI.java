package com.ppmrj.linebot.Web_API;

import com.ppmrj.linebot.Web_API.Model.*;

import retrofit2.Call;
import retrofit2.http.*;

public interface WebAPI {

    @POST("grup/")
    Call<GrupResponse> registerGroup(@Body String nama_divisi,
                                       @Body String groupId,
                                       @Body String nama,
                                       @Body String statusGame,
                                       @Body String tipeGrup);

    @GET("grup/{id}")
    Call<GrupResponse> getGrup(@Path("id") String id);

    @DELETE("grup/{id}")
    Call<GrupResponse> unregisterGrup(@Path("id") String id);

    @POST("divisi/")
    Call<DivisiResponse> addDivisi(@Body String nama);

    @GET("divisi/{nama}/grup")
    Call<GrupResponse> getDivisiGrup(@Path("nama") String nama);
}
