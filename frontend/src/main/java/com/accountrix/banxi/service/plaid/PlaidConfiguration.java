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

    // Sandbox Mode
    private static final String plaidSandboxClientID = "56c949d37c1539e11d919abc";
    private static final String plaidSandboxSecret = "e2aa3bc2bc8d8f96a9d540460b8831";
    private static final String plaidSandboxEnvironment = ApiClient.Sandbox;
    @Bean public PlaidSandboxClient plaidSandboxClient() {
        HashMap<String, String> apiKeys = new HashMap<String, String>();
        apiKeys.put("clientId", plaidSandboxClientID);
        apiKeys.put("secret", plaidSandboxSecret);
        apiKeys.put("plaidVersion", "2020-09-14");
        ApiClient apiClient;
        apiClient = new ApiClient(apiKeys);
        apiClient.setPlaidAdapter(plaidSandboxEnvironment);
        return apiClient.createService(PlaidSandboxClient.class);
    }
    public static String getPlaidSandboxClientID() { return plaidSandboxClientID; }
    public static String getPlaidSandboxSecret() { return plaidSandboxSecret; }
    public static String getPlaidSandboxEnvironment() { return plaidSandboxEnvironment; }


    // Development Mode
    private static final String plaidDevelopmentClientID = "645862fc1c80a20018c351a6";
    private static final String plaidDevelopmentSecret = "c969234d7678433c333d9096b1d763";
    private static final String plaidDevelopmentEnvironment = ApiClient.Development;
    @Bean public PlaidDevelopmentClient plaidDevelopmentClient() {
        HashMap<String, String> apiKeys = new HashMap<String, String>();
        apiKeys.put("clientId", plaidDevelopmentClientID);
        apiKeys.put("secret", plaidDevelopmentSecret);
        apiKeys.put("plaidVersion", "2020-09-14");
        ApiClient apiClient;
        apiClient = new ApiClient(apiKeys);
        apiClient.setPlaidAdapter(plaidDevelopmentEnvironment);
        return apiClient.createService(PlaidDevelopmentClient.class);
    }
    public static String getPlaidDevelopmentClientID() { return plaidDevelopmentClientID; }
    public static String getPlaidDevelopmentSecret() { return plaidDevelopmentSecret; }
    public static String getPlaidDevelopmentEnvironment() { return plaidDevelopmentEnvironment; }


//    public static String getPlaidClientID() {
//        return plaidClientID;
//    }
//
//    public static String getPlaidSecret() {
//        return plaidSecret;
//    }

}
