package com.accountrix.banxi.service.plaid;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.plaid.client.ApiClient;
import com.plaid.client.model.*;
import com.plaid.client.request.PlaidApi;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Configuration
public class PlaidConfiguration {
    private static final String plaidClientID = "56c949d37c1539e11d919abc";
    private static final String plaidSecret = "e2aa3bc2bc8d8f96a9d540460b8831";

    @Bean
    public PlaidApi plaidClient() {
        HashMap<String, String> apiKeys = new HashMap<String, String>();
        apiKeys.put("clientId", "56c949d37c1539e11d919abc");
        apiKeys.put("secret", "e2aa3bc2bc8d8f96a9d540460b8831");
        apiKeys.put("plaidVersion", "2020-09-14");
        ApiClient apiClient;
        apiClient = new ApiClient(apiKeys);
        apiClient.setPlaidAdapter(ApiClient.Sandbox);// or equivalent, depending on which environment you're calling into
        return apiClient.createService(PlaidApi.class);
    }

    public static String getPlaidClientID() {
        return plaidClientID;
    }

    public static String getPlaidSecret() {
        return plaidSecret;
    }

}
