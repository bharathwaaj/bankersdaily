package in.bankersdaily.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;

@Entity(indexes = {
        @Index(value = "publishedDateState, postId, categoryId", unique = true)
})
public class FetchedPostsTracker {

    @Id(autoincrement = true) private Long id;

    private String publishedDateState;

    private Long postId;

    private Long categoryId;

    @Generated(hash = 1655219351)
    public FetchedPostsTracker(Long id, String publishedDateState, Long postId,
            Long categoryId) {
        this.id = id;
        this.publishedDateState = publishedDateState;
        this.postId = postId;
        this.categoryId = categoryId;
    }

    public FetchedPostsTracker(String publishedDateState, Long postId, int categoryId) {
        this.publishedDateState = publishedDateState;
        this.postId = postId;
        this.categoryId = (long) categoryId;
    }

    @Generated(hash = 1585437129)
    public FetchedPostsTracker() {
    }

    public static void insertOrUpdate(FetchedPostsTracker fetchedPostsTracker,
                                      FetchedPostsTrackerDao fetchedPostsTrackerDao,
                                      String publishedDateState, Long postId, int categoryId) {

        if (fetchedPostsTracker == null) {
            fetchedPostsTracker = new FetchedPostsTracker(publishedDateState, postId, categoryId);
            fetchedPostsTrackerDao.insert(fetchedPostsTracker);
            return;
        }
        fetchedPostsTracker.setPostId(postId);
        fetchedPostsTrackerDao.save(fetchedPostsTracker);
    }

    public static void insertIfNotExist(FetchedPostsTracker fetchedPostsTracker,
                                        FetchedPostsTrackerDao fetchedPostsTrackerDao,
                                        String publishedDateState, Long postId, int categoryId) {

        if (fetchedPostsTracker == null) {
            fetchedPostsTracker = new FetchedPostsTracker(publishedDateState, postId, categoryId);
            fetchedPostsTrackerDao.insert(fetchedPostsTracker);
        }
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPublishedDateState() {
        return this.publishedDateState;
    }

    public void setPublishedDateState(String publishedDateState) {
        this.publishedDateState = publishedDateState;
    }

    public Long getPostId() {
        return this.postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

}
