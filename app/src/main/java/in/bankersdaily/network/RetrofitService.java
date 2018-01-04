package in.bankersdaily.network;

import java.util.List;
import java.util.Map;

import in.bankersdaily.model.Category;
import in.bankersdaily.model.Comment;
import in.bankersdaily.model.CreateCommentResponse;
import in.bankersdaily.model.LoginResponse;
import in.bankersdaily.model.Post;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

import static in.bankersdaily.network.ApiClient.COMMENTS_PATH;
import static in.bankersdaily.network.ApiClient.CREATE_COMMENT_PATH;

public interface RetrofitService {

    @GET("{posts_url}")
    RetrofitCall<List<Post>> getPosts(
            @Path(value = "posts_url", encoded = true) String endExamUrlFrag,
            @QueryMap Map<String, Object> params);

    @GET(ApiClient.CATEGORIES_PATH)
    RetrofitCall<List<Category>> getCategories(@QueryMap Map<String, Object> params);

    @GET(ApiClient.LOGIN_PATH)
    RetrofitCall<LoginResponse> authenticate(@QueryMap Map<String, Object> params);

    @GET(COMMENTS_PATH)
    RetrofitCall<List<Comment>> getComments(@QueryMap Map<String, Object> params);

    @GET(CREATE_COMMENT_PATH)
    RetrofitCall<CreateCommentResponse> postComment(@QueryMap Map<String, Object> params);

}
