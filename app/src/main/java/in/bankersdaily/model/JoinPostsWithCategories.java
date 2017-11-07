package in.bankersdaily.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Index;

@Entity(indexes = {
        @Index(value = "postId, categoryId", unique = true)
})
public class JoinPostsWithCategories {

    @Id(autoincrement = true) private Long id;

    private Long postId;

    private Long categoryId;

    @Generated(hash = 542829967)
    public JoinPostsWithCategories(Long id, Long postId, Long categoryId) {
        this.id = id;
        this.postId = postId;
        this.categoryId = categoryId;
    }

    public JoinPostsWithCategories(Long postId, Long categoryId) {
        this.postId = postId;
        this.categoryId = categoryId;
    }

    @Generated(hash = 1176904500)
    public JoinPostsWithCategories() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
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
