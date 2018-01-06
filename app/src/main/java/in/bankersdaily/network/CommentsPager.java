package in.bankersdaily.network;

import java.io.IOException;
import java.util.List;

import in.bankersdaily.model.Comment;
import retrofit2.Response;

public class CommentsPager extends BaseResourcePager<Comment> {

    private ApiClient apiClient;
    private long postId;

    public CommentsPager(ApiClient apiClient, long postId) {
        this.apiClient = apiClient;
        this.postId = postId;
        itemsPerPage = 10;
    }

    @Override
    protected Object getId(Comment post) {
        return post.getId();
    }

    @Override
    public Response<List<Comment>> getItems(int page, int size) throws IOException {
        queryParams.put(ApiClient.POST, postId);
        return apiClient.getComments(queryParams).execute();
    }

}
