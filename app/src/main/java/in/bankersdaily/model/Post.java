package in.bankersdaily.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import java.util.Date;

@Entity
public class Post {

    @Expose @Id private Long id;

    @Expose @SerializedName("date_gmt")
    @Property private Date date;

    @Expose @SerializedName("modified_gmt")
    @Property private Date modified;

    @Expose @Property private String status;
    @Expose @Property private String link;

    @Property private String title;

    @Property private String content;

    @Generated(hash = 1208912888)
    public Post(Long id, Date date, Date modified, String status, String link,
            String title, String content) {
        this.id = id;
        this.date = date;
        this.modified = modified;
        this.status = status;
        this.link = link;
        this.title = title;
        this.content = content;
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

}
