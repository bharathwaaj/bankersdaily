package in.bankersdaily.model;

import com.google.gson.annotations.SerializedName;

public class Content {

    private String rendered;

    @SerializedName("protected")
    private Boolean _protected;

    public String getRendered() {
        return rendered;
    }

    public void setRendered(String rendered) {
        this.rendered = rendered;
    }

    public Boolean getProtected() {
        return _protected;
    }

    public void setProtected(Boolean _protected) {
        this._protected = _protected;
    }
}
