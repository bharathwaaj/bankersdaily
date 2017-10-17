package in.bankersdaily.network;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import retrofit2.Response;

/**
 * References
 * https://gist.github.com/rahulgautam/25c72ffcac70dacb87bd#file-errorhandlingexecutorcalladapterfactory-java
 * https://github.com/square/retrofit/tree/master/samples/src/main/java/com/example/retrofit
 */
public class RetrofitException extends RuntimeException {
    public static RetrofitException httpError(Response response) {
        String message = response.code() + " " + response.message();
        return new RetrofitException(message, response, Kind.HTTP, null);
    }

    public static RetrofitException networkError(IOException exception) {
        return new RetrofitException(exception.getMessage(), null, Kind.NETWORK, exception);
    }

    public static RetrofitException unexpectedError(Throwable exception) {
        return new RetrofitException(exception.getMessage(), null, Kind.UNEXPECTED, exception);
    }

    /** Identifies the event kind which triggered a {@link RetrofitException}. */
    public enum Kind {
        /** An {@link IOException} occurred while communicating to the server. */
        NETWORK,
        /** A non-200 HTTP status code was received from the server. */
        HTTP,
        /**
         * An internal error occurred while attempting to execute a request. It is best practice to
         * re-throw this exception so your application crashes.
         */
        UNEXPECTED
    }

    private final Response response;
    private final Kind kind;
    private int statusCode;
    private String message;

    private RetrofitException(String message, Response response, Kind kind, Throwable exception) {
        super(message, exception);
        this.response = response;
        this.message = message;
        this.kind = kind;
        if (response != null) {
            statusCode = response.code();
        }
    }

    /** Response object containing status code, headers, body, etc. */
    public Response getResponse() {
        return response;
    }

    /** The event kind which triggered this error. */
    public Kind getKind() {
        return kind;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * HTTP response body converted to specified {@code type}. {@code null} if there is no
     * response.
     */
    public <T> T getErrorBodyAs(Response response, Class<T> type) {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        try {
            String json = response.errorBody().string();
            return gson.fromJson(json, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isNetworkError() {
        return kind == Kind.NETWORK;
    }

    public boolean isUnauthenticated() {
        return statusCode == 403;
    }

    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500 && statusCode != 403;
    }

    public boolean isServerError() {
        return statusCode >= 500 && statusCode < 600;
    }
}
