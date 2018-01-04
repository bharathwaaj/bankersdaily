package in.bankersdaily.network;

import java.io.IOException;
import java.util.List;

import in.bankersdaily.model.Post;
import retrofit2.Response;

public class PostPager extends BaseResourcePager<Post> {

    private ApiClient apiClient;

    public PostPager(ApiClient apiClient) {
        this.apiClient = apiClient;
        itemsPerPage = 10;
    }

    @Override
    protected Object getId(Post post) {
        return post.getId();
    }

    @Override
    public Response<List<Post>> getItems(int page, int size) throws IOException {
        queryParams.put(ApiClient.EMBED, "1");
        return apiClient.getPosts(ApiClient.POSTS_PATH, queryParams).execute();
    }

}
