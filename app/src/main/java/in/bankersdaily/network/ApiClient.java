package in.bankersdaily.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import in.bankersdaily.R;
import in.bankersdaily.model.Category;
import in.bankersdaily.model.Comment;
import in.bankersdaily.model.CommentDeserializer;
import in.bankersdaily.model.CreateCommentResponse;
import in.bankersdaily.model.DateDeserializer;
import in.bankersdaily.model.LoginResponse;
import in.bankersdaily.model.Post;
import in.bankersdaily.model.PostDeserializer;
import in.bankersdaily.util.Assert;
import in.testpress.core.TestpressSdk;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.QueryMap;

import static in.bankersdaily.ui.LoginActivity.KEY_AUTH_SHARED_PREFS;
import static in.bankersdaily.ui.LoginActivity.KEY_WORDPRESS_TOKEN;

public class ApiClient {

    public static final String POSTS_PATH = "wp-json/wp/v2/posts/";
    public static final String PAGES_PATH = "wp-json/wp/v2/pages/";

    static final String CATEGORIES_PATH = "wp-json/wp/v2/categories/";

    static final String LOGIN_PATH = "api/user/fb_connect/";

    static final String COMMENTS_PATH = "wp-json/wp/v2/comments/";

    static final String CREATE_COMMENT_PATH = "api/user/post_comment/";

    public static final String SEARCH_QUERY = "search";

    public static final String TIME_ZONE = "GMT+05:30";

    /**
     * Query Params
     */
    public static final String PER_PAGE = "per_page";
    public static final String ORDER = "order";
    public static final String AFTER = "after";
    public static final String BEFORE = "before";
    public static final String CATEGORY = "categories";
    public static final String EMBED = "_embed";
    public static final String SLUG = "slug";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String INSECURE = "insecure";
    public static final String COOL = "cool";
    public static final String POST = "post";
    public static final String POST_ID = "post_id";
    public static final String PARENT = "parent";
    public static final String COOKIE = "cookie";
    public static final String COMMENT_STATUS = "comment_status";
    public static final String CONTENT = "content";

    private final Retrofit retrofit;
    private Context context;

    public ApiClient(final Context context) {
        Assert.assertNotNull("Context must not be null.", context);
        this.context = context;
        Gson gson = getGsonBuilder()
                .registerTypeAdapter(Post.class, new PostDeserializer())
                .registerTypeAdapter(Comment.class, new CommentDeserializer())
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

    private void addCookie(Map<String, Object> params) {
        SharedPreferences pref =
                context.getSharedPreferences(KEY_AUTH_SHARED_PREFS, Context.MODE_PRIVATE);

        params.put(COOKIE, pref.getString(KEY_WORDPRESS_TOKEN, ""));
    }

    public RetrofitCall<List<Post>> getPosts(String url, @QueryMap Map<String, Object> params) {
        return getRetrofitService().getPosts(url, params);
    }

    public RetrofitCall<List<Category>> getCategories(@QueryMap Map<String, Object> params) {
        return getRetrofitService().getCategories(params);
    }

    public RetrofitCall<LoginResponse> authenticate(Map<String, Object> params) {
        return getRetrofitService().authenticate(params);
    }

    public RetrofitCall<List<Comment>> getComments(Map<String, Object> params) {
        return getRetrofitService().getComments(params);
    }

    public RetrofitCall<CreateCommentResponse> postComment(Map<String, Object> params) {
        addCookie(params);
        return getRetrofitService().postComment(params);
    }



}
