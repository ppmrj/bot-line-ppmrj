package com.ppmrj.linebot.Web_API;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by Asus on 10/14/2017.
 */
public class WebAPIBuilder {
    public static final String API_BASE_URL = "http://api.ppmrjbandung.com/";
    public static final long DEFAULT_CONNECT_TIMEOUT = 10_000;
    public static final long DEFAULT_READ_TIMEOUT = 10_000;
    public static final long DEFAULT_WRITE_TIMEOUT = 10_000;

    private String apiEndPoint = API_BASE_URL;
    private long connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private long readTimeout = DEFAULT_READ_TIMEOUT;
    private long writeTimeout = DEFAULT_WRITE_TIMEOUT;

    private OkHttpClient.Builder okHttpClientBuilder;
    private Retrofit.Builder retrofitBuilder;

    public static WebAPIBuilder create(){
        return new WebAPIBuilder();
    }

    public WebAPIBuilder(){

    }

    public WebAPIBuilder okHttpClientBuilder(OkHttpClient.Builder okHttpClientBuilder) {
        this.okHttpClientBuilder = okHttpClientBuilder;
        return this;
    }

    public WebAPIBuilder retrofitBuilder(Retrofit.Builder retrofitBuilder) {
        this.retrofitBuilder = retrofitBuilder;
        return this;
    }

    public WebAPI build() {
        if (okHttpClientBuilder == null) {
            okHttpClientBuilder = new OkHttpClient.Builder();
        }
        okHttpClientBuilder
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS);
        final OkHttpClient okHttpClient = okHttpClientBuilder.build();

        if (retrofitBuilder == null) {
            retrofitBuilder = createDefaultRetrofitBuilder();
        }
        retrofitBuilder.client(okHttpClient);
        retrofitBuilder.baseUrl(apiEndPoint);
        final Retrofit retrofit = retrofitBuilder.build();

        return retrofit.create(WebAPI.class);
    }

    private static Retrofit.Builder createDefaultRetrofitBuilder() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        objectMapper.registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

        return new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create(objectMapper));
    }
//
}
