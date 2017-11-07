package in.bankersdaily.network;

import java.util.List;
import java.util.Map;

import in.bankersdaily.model.Category;
import in.bankersdaily.model.Post;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface RetrofitService {

    @GET(ApiClient.POSTS_PATH)
    RetrofitCall<List<Post>> getPosts(@QueryMap Map<String, Object> params);

    @GET(ApiClient.CATEGORIES_PATH)
    RetrofitCall<List<Category>> getCategories(@QueryMap Map<String, Object> params);

}
