package in.bankersdaily.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity
public class Bookmark {

    @Id private Long id;

    @ToOne(joinProperty = "id")
    private Post post;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1189571525)
    private transient BookmarkDao myDao;

    @Generated(hash = 1213035114)
    public Bookmark(Long id) {
        this.id = id;
    }

    @Generated(hash = 1206029275)
    public Bookmark() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Generated(hash = 1690682906)
    private transient Long post__resolvedKey;

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 409998522)
    public Post getPost() {
        Long __key = this.id;
        if (post__resolvedKey == null || !post__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PostDao targetDao = daoSession.getPostDao();
            Post postNew = targetDao.load(__key);
            synchronized (this) {
                post = postNew;
                post__resolvedKey = __key;
            }
        }
        return post;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 797437088)
    public void setPost(Post post) {
        synchronized (this) {
            this.post = post;
            id = post == null ? null : post.getId();
            post__resolvedKey = id;
        }
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

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 2020207998)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getBookmarkDao() : null;
    }

}
