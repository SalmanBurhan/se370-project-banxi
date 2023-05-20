package com.accountrix.banxi.service.plaid;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.plaid.client.ApiClient;
import com.plaid.client.model.*;
import com.plaid.client.request.PlaidApi;
import org.springframework.stereotype.Service;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Configuration
@Service
public class PlaidConfiguration {

    private static String plaidVersion;

    // Sandbox Mode
    private static String plaidSandboxClientID;
    private static String plaidSandboxSecret;
    private static String plaidSandboxEnvironment;

    @Value("${plaid.version}")
    public void setPlaidVersion(String plaidVersion) { PlaidConfiguration.plaidVersion = plaidVersion; }
    @Value("${plaid.client-id.sandbox}")
    public void setPlaidSandboxClientID(String plaidSandboxClientID) { PlaidConfiguration.plaidSandboxClientID = plaidSandboxClientID; }
    @Value("${plaid.secret.sandbox}")
    public void setPlaidSandboxSecret(String plaidSandboxSecret) { PlaidConfiguration.plaidSandboxSecret = plaidSandboxSecret; }
    @Value("${plaid.environment.sandbox}")
    public void setPlaidSandboxEnvironment(String sandboxEnvironment) { PlaidConfiguration.plaidSandboxEnvironment = sandboxEnvironment; }

    @Bean public PlaidSandboxClient plaidSandboxClient() {
        return createPlaidClient(plaidSandboxClientID, plaidSandboxSecret, plaidSandboxEnvironment)
                .createService(PlaidSandboxClient.class);
    }

    public static String getPlaidSandboxClientID() { return plaidSandboxClientID; }
    public static String getPlaidSandboxSecret() { return plaidSandboxSecret; }
    public static String getPlaidSandboxEnvironment() { return plaidSandboxEnvironment; }


    // Development Mode
    private static String plaidDevelopmentClientID;
    private static String plaidDevelopmentSecret;
    private static String plaidDevelopmentEnvironment;

    @Value("${plaid.client-id.development}")
    public void setPlaidDevelopmentClientID(String developmentClientID) { PlaidConfiguration.plaidDevelopmentClientID = developmentClientID; }
    @Value("${plaid.secret.development}")
    public void setPlaidDevelopmentSecret(String developmentSecret) { PlaidConfiguration.plaidDevelopmentSecret = developmentSecret; }
    @Value("${plaid.environment.development}")
    public void setPlaidDevelopmentEnvironment(String developmentEnvironment) { PlaidConfiguration.plaidDevelopmentEnvironment = developmentEnvironment; }

    @Bean public PlaidDevelopmentClient plaidDevelopmentClient() {
        return createPlaidClient(plaidDevelopmentClientID, plaidDevelopmentSecret, plaidDevelopmentEnvironment)
                .createService(PlaidDevelopmentClient.class);
    }
    public static String getPlaidDevelopmentClientID() { return plaidDevelopmentClientID; }
    public static String getPlaidDevelopmentSecret() { return plaidDevelopmentSecret; }
    public static String getPlaidDevelopmentEnvironment() { return plaidDevelopmentEnvironment; }


    private ApiClient createPlaidClient(String clientID, String secret, String environment) {
        HashMap<String, String> apiKeys = new HashMap<String, String>();
        apiKeys.put("clientId", clientID);
        apiKeys.put("secret", secret);
        apiKeys.put("plaidVersion", plaidVersion);
        ApiClient apiClient;
        apiClient = new ApiClient(apiKeys);
        apiClient.setPlaidAdapter(environment);
        return apiClient;
    }

}
