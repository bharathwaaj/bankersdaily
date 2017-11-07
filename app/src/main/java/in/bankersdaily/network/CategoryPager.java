package in.bankersdaily.network;

import java.io.IOException;
import java.util.List;

import in.bankersdaily.model.Category;
import retrofit2.Response;

public class CategoryPager extends BaseResourcePager<Category> {

    private ApiClient apiClient;

    public CategoryPager(ApiClient apiClient) {
        this.apiClient = apiClient;
        itemsPerPage = 10;
    }

    @Override
    protected Object getId(Category category) {
        return category.getId();
    }

    @Override
    public Response<List<Category>> getItems(int page, int size) throws IOException {
        return apiClient.getCategories(queryParams).execute();
    }

}
