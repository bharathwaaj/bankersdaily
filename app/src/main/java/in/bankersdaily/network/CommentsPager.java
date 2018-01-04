package in.bankersdaily.network;

import java.io.IOException;
import java.util.List;

import in.bankersdaily.model.Comment;
import retrofit2.Response;

public class CommentsPager extends BaseResourcePager<Comment> {

    private ApiClient apiClient;
    private long postId;
    private int parent;

    public CommentsPager(ApiClient apiClient, long postId, int parent) {
        this.apiClient = apiClient;
        this.postId = postId;
        this.parent = parent;
        itemsPerPage = 10;
    }

    @Override
    protected Object getId(Comment post) {
        return post.getId();
    }

    @Override
    public Response<List<Comment>> getItems(int page, int size) throws IOException {
        queryParams.put(ApiClient.POST, postId);
        queryParams.put(ApiClient.PARENT, parent);
        return apiClient.getComments(queryParams).execute();
    }

}
