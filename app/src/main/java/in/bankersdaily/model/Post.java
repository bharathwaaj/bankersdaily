package in.bankersdaily.model;

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
import java.util.List;

@Entity
public class Post {

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

    @Property private String title;

    @Property private String content;

    @Property private String featuredMedia;

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

    @Generated(hash = 587973530)
    public Post(Long id, Date date, Date modified, String slug, String status, String link,
            String title, String content, String featuredMedia) {
        this.id = id;
        this.date = date;
        this.modified = modified;
        this.slug = slug;
        this.status = status;
        this.link = link;
        this.title = title;
        this.content = content;
        this.featuredMedia = featuredMedia;
    }

    @Generated(hash = 1782702645)
    public Post() {
    }

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

    public static QueryBuilder<Post> getPostListQueryBuilder(PostDao postDao, int categoryId) {
        QueryBuilder<Post> queryBuilder = postDao.queryBuilder().orderDesc(PostDao.Properties.Date);
        if (categoryId != 0) {
            queryBuilder
                    .join(JoinPostsWithCategories.class, JoinPostsWithCategoriesDao.Properties.PostId)
                    .where(JoinPostsWithCategoriesDao.Properties.CategoryId.eq(categoryId));
        }
        return queryBuilder;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1915117241)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getPostDao() : null;
    }

}
