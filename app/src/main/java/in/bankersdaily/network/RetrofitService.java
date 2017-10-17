package in.bankersdaily.network;

import java.util.List;

import in.bankersdaily.model.Post;
import retrofit2.http.GET;

public interface RetrofitService {

    @GET(ApiClient.POSTS_PATH)
    RetrofitCall<List<Post>> getPosts();

}
