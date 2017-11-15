package net.wetfish.wetfish.retrofit;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by ${Michael} on 11/2/2017.
 *
 * Interface for REST Api interactions with Retrofit
 */

public interface RESTInterface {

    // Multipart post method
    @Multipart
    @POST("/upload.php")
    Call<ResponseBody> postPhoto(@Part MultipartBody.Part file);
}
