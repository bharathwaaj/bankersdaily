package in.bankersdaily.model;

public class LoginResponse {

    private String status;
    private String msg;
    private Integer wpUserId;
    private String cookie;
    private String userLogin;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getWpUserId() {
        return wpUserId;
    }

    public void setWpUserId(Integer wpUserId) {
        this.wpUserId = wpUserId;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

}
