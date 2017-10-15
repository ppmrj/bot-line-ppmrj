package com.ppmrj.linebot.WebAPI;

import com.ppmrj.linebot.Model.*;

import com.ppmrj.linebot.Responses.DivisiResponse;
import com.ppmrj.linebot.Responses.GroupResponse;
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
