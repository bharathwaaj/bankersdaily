package in.bankersdaily.network;

public abstract class RetrofitCallback<T> {

    public abstract void onSuccess(T result);

    public abstract void onException(RetrofitException exception);

}
