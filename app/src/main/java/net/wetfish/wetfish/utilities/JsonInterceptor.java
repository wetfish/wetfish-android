package net.wetfish.wetfish.utilities;

/**
 * Created by ${Michael} on 11/4/2017.
 */

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by ${Michael} on 11/2/2017.
 *
 * Interceptor to log JSON data from Retrofit
 */

public class JsonInterceptor implements Interceptor {

    private static final String LOG_TAG = JsonInterceptor.class.getSimpleName();
    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();

        Response response = chain.proceed(request);
        String rawJson = response.body().string();

        Log.d(LOG_TAG, String.format("raw JSON response is: %s", rawJson));

        // Re-create the response before returning it because body can be read only once
        return response.newBuilder()
                .body(ResponseBody.create(response.body().contentType(), rawJson)).build();
    }
}