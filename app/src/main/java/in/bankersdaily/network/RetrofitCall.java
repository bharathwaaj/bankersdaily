package in.bankersdaily.network;

import java.io.IOException;

import retrofit2.Response;

public interface RetrofitCall<T> {

    void cancel();

    RetrofitCall<T> enqueue(RetrofitCallback<T> callback);

    RetrofitCall<T> clone();

    Response<T> execute() throws IOException;

}
