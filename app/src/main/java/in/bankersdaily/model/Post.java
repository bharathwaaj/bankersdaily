package in.bankersdaily.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinEntity;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import in.bankersdaily.BankersDailyApp;
import in.bankersdaily.network.ApiClient;
import in.bankersdaily.network.RetrofitCallback;

@Entity
public class Post implements Parcelable {

    public static final String LATEST = "latest";
    public static final String OLDEST = "oldest";

    @Expose @Id private Long id;

    @Expose @SerializedName("date")
    @Property private Date date;

    @Expose @SerializedName("modified")
    @Property private Date modified;

    @Expose @Property private String slug;
    @Expose @Property private String status;
    @Expose @Property private String link;
    @Expose @Property private String commentStatus;

    @Property private String title;

    @Property private String content;

    @Property private String featuredMedia;
    @Property private String featuredMediaSquare;

    @ToMany
    @JoinEntity(
            entity = JoinPostsWithCategories.class,
            sourceProperty = "postId",
            targetProperty = "categoryId"
    )
    private List<Category> categories;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 572315894)
    private transient PostDao myDao;

    @Generated(hash = 2123399416)
    public Post(Long id, Date date, Date modified, String slug, String status, String link,
            String commentStatus, String title, String content, String featuredMedia,
            String featuredMediaSquare) {
        this.id = id;
        this.date = date;
        this.modified = modified;
        this.slug = slug;
        this.status = status;
        this.link = link;
        this.commentStatus = commentStatus;
        this.title = title;
        this.content = content;
        this.featuredMedia = featuredMedia;
        this.featuredMediaSquare = featuredMediaSquare;
    }

    @Generated(hash = 1782702645)
    public Post() {
    }

    protected Post(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        date = new Date(in.readLong());
        modified = new Date(in.readLong());
        slug = in.readString();
        status = in.readString();
        link = in.readString();
        title = in.readString();
        content = in.readString();
        featuredMedia = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeLong(date.getTime());
        dest.writeLong(modified.getTime());
        dest.writeString(slug);
        dest.writeString(status);
        dest.writeString(link);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(featuredMedia);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getModified() {
        return this.modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getSlug() {
        return this.slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLink() {
        return this.link;
    }

    public String getCommentStatus() {
        return this.commentStatus;
    }

    public void setCommentStatus(String commentStatus) {
        this.commentStatus = commentStatus;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFeaturedMedia() {
        return this.featuredMedia;
    }

    public void setFeaturedMedia(String featuredMedia) {
        this.featuredMedia = featuredMedia;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1105457336)
    public List<Category> getCategories() {
        if (categories == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            CategoryDao targetDao = daoSession.getCategoryDao();
            List<Category> categoriesNew = targetDao._queryPost_Categories(id);
            synchronized (this) {
                if (categories == null) {
                    categories = categoriesNew;
                }
            }
        }
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public String getFeaturedMediaSquare() {
        return this.featuredMediaSquare;
    }

    public void setFeaturedMediaSquare(String featuredMediaSquare) {
        this.featuredMediaSquare = featuredMediaSquare;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1494004962)
    public synchronized void resetCategories() {
        categories = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    public static QueryBuilder<Post> getPostListQueryBuilder(Context context, int categoryId,
                                                             boolean filterBookmarked) {

        PostDao postDao = BankersDailyApp.getDaoSession(context).getPostDao();
        QueryBuilder<Post> queryBuilder = postDao.queryBuilder()
                .orderDesc(PostDao.Properties.Date);

        if (filterBookmarked) {
            queryBuilder
                    .join(Bookmark.class, BookmarkDao.Properties.Id)
                    .where(BookmarkDao.Properties.Id.isNotNull());
        } else {
            Date oldestPostDate = FetchedPostsTracker.getDate(context, categoryId, Post.OLDEST);
            if (oldestPostDate != null) {
                queryBuilder.where(PostDao.Properties.Date.ge(oldestPostDate));
            }
        }
        if (categoryId != 0) {
            queryBuilder
                    .join(JoinPostsWithCategories.class, JoinPostsWithCategoriesDao.Properties.PostId)
                    .where(JoinPostsWithCategoriesDao.Properties.CategoryId.eq(categoryId));
        }
        return queryBuilder;
    }

    @Nullable
    public Bookmark getBookmark() {
        final DaoSession daoSession = this.daoSession;
        if (daoSession == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        BookmarkDao targetDao = daoSession.getBookmarkDao();
        List<Bookmark> bookmarks = targetDao.queryBuilder()
                .where(BookmarkDao.Properties.Id.eq(getId()))
                .list();

        if (!bookmarks.isEmpty()) {
            return bookmarks.get(0);
        }
        return null;
    }

    public static void loadLatest(ApiClient apiClient, int categoryId,
                                  RetrofitCallback<List<Post>> callback) {

        Map<String, Object> queryParams = new LinkedHashMap<String, Object>();
        queryParams.put(ApiClient.CATEGORY, categoryId);
        queryParams.put(ApiClient.PER_PAGE, 3);
        queryParams.put(ApiClient.EMBED, "1");
        apiClient.getPosts(ApiClient.POSTS_PATH, queryParams).enqueue(callback);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1915117241)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getPostDao() : null;
    }

}
