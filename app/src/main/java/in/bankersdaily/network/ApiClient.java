package in.bankersdaily.network;

import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import in.bankersdaily.R;
import in.bankersdaily.model.DateDeserializer;
import in.bankersdaily.model.Post;
import in.bankersdaily.model.PostDeserializer;
import in.bankersdaily.util.Assert;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.QueryMap;

public class ApiClient {

    static final String POSTS_PATH= "wp-json/wp/v2/posts/";

    public static final String TIME_ZONE = "GMT+05:30";

    /**
     * Query Params
     */
    public static final String ORDER = "order";
    public static final String AFTER = "after";
    public static final String BEFORE = "before";

    private final Retrofit retrofit;

    public ApiClient(final Context context) {
        Assert.assertNotNull("Context must not be null.", context);
        Gson gson = getGsonBuilder()
                .registerTypeAdapter(Post.class, new PostDeserializer())
                .create();

        // Set headers for all network requests
        Interceptor interceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
                Request.Builder header = chain.request().newBuilder()
                        .addHeader("User-Agent", UserAgentProvider.get(context));
                return chain.proceed(header.build());
            }
        };
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.connectTimeout(2, TimeUnit.MINUTES).readTimeout(2, TimeUnit.MINUTES);
        httpClient.addNetworkInterceptor(interceptor);

        // Set log level
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(httpLoggingInterceptor);

        retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.base_url))
                .addCallAdapterFactory(new ErrorHandlingCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();
    }

    public static GsonBuilder getGsonBuilder() {
        return new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateDeserializer())
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
    }

    private RetrofitService getRetrofitService() {
        return retrofit.create(RetrofitService.class);
    }

    public RetrofitCall<List<Post>> getPosts(@QueryMap Map<String, Object> params) {
        return getRetrofitService().getPosts(params);
    }

}
