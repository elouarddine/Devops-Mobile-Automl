package com.example.parksmart.network;

import android.content.Context;

import com.example.parksmart.utils.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    // 10.0.2.2 fonctionne uniquement sur l'émulateur Android.
    // Sur un téléphone physique, remplacez par l'IP locale de votre machine, par ex. http://192.168.1.20:8000/
    private static final String BASE_URL = "http://10.0.2.2:8000/";

    private static Retrofit retrofit;

    private ApiClient() {
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(buildBaseClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService createService() {
        return getClient().create(ApiService.class);
    }

    public static ApiService createAuthenticatedService(Context context) {
        SessionManager sessionManager = new SessionManager(context);

        Interceptor authInterceptor = chain -> {
            Request original = chain.request();
            Request.Builder builder = original.newBuilder();
            String token = sessionManager.getToken();
            if (token != null && !token.isEmpty()) {
                builder.header("Authorization", "Bearer " + token);
            }
            return chain.proceed(builder.build());
        };

        OkHttpClient client = buildBaseClient().newBuilder()
                .addInterceptor(authInterceptor)
                .build();

        Retrofit authenticatedRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return authenticatedRetrofit.create(ApiService.class);
    }

    private static OkHttpClient buildBaseClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
    }
}
