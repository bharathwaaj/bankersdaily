package in.bankersdaily.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Category {

    @Id public Long id;

    @Property public String link;
    @Property public String name;
    @Property public String slug;
    @Property public Integer parent;

    @Generated(hash = 1368128009)
    public Category(Long id, String link, String name, String slug,
            Integer parent) {
        this.id = id;
        this.link = link;
        this.name = name;
        this.slug = slug;
        this.parent = parent;
    }

    @Generated(hash = 1150634039)
    public Category() {
    }

    public String getLink() {
        return this.link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return this.slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Integer getParent() {
        return this.parent;
    }

    public void setParent(Integer parent) {
        this.parent = parent;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
