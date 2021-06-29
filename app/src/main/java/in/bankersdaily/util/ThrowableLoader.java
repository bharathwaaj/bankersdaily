package in.bankersdaily.util;

import android.content.Context;
import android.util.Log;

import androidx.loader.content.Loader;

import java.util.List;

import in.bankersdaily.network.RetrofitException;

/**
 * Loader that support throwing an exception when loading in the background
 *
 * @param <D>
 */
public abstract class ThrowableLoader<D> extends AsyncLoader<D> {

    private final D data;

    private RetrofitException exception;

    /**
     * Create loader for context and seeded with initial data
     *
     * @param context
     * @param data
     */
    public ThrowableLoader(final Context context, final D data) {
        super(context);

        this.data = data;
    }

    @Override
    public D loadInBackground() {
        exception = null;
        try {
            return loadData();
        } catch (final RetrofitException e) {
            Log.d("ThrowableLoader", "Exception loading data");
            e.printStackTrace();
            exception = e;
            return data;
        }
    }

    /**
     * @return exception
     */
    public RetrofitException getException() {
        return exception;
    }

    /**
     * Clear the stored exception and return it
     *
     * @return exception
     */
    public RetrofitException clearException() {
        final RetrofitException throwable = exception;
        exception = null;
        return throwable;
    }

    /**
     * Load data
     *
     * @return data
     * @throws RetrofitException
     */
    public abstract D loadData() throws RetrofitException;

    /**
     * return the stored exception in given loader and clear it.
     *
     * @return exception
     */
    public static <T> Exception getException(Loader<List<T>> loader) {
        if (loader instanceof ThrowableLoader) {
            return ((ThrowableLoader<List<T>>) loader).clearException();
        } else {
            return null;
        }
    }
}
