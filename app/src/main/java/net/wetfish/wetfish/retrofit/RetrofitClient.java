package net.wetfish.wetfish.retrofit;


import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ${Michael} on 11/2/2017.
 *
 * Utility class to create the Retrofit client with JsonInterceptor functionality
 */

public class RetrofitClient {

    private static Retrofit retrofit = null;


        private String BASE_URL = "https://volunteer.wetfish.net/";

    // Create Retrofit Instance
    public static Retrofit getClient(String baseUrl) {

        String BASE_URL = "https://volunteer.wetfish.net/";

        if (retrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(new JsonInterceptor());
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }
}