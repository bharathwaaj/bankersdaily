package in.bankersdaily.model;

import android.annotation.SuppressLint;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import in.bankersdaily.network.ApiClient;

/**
 * Use to parse date string from json & map to Date field, since timezone is not available in
 * Wordpress API date format, Gson can't parse it. So, parse the date using SimpleDateFormat with
 * time zone as UTC
 */

public class DateDeserializer implements JsonDeserializer<Date> {

    @SuppressLint("SimpleDateFormat")
    @Override
    public Date deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone(ApiClient.TIME_ZONE));
        try {
            return formatter.parse(element.getAsString());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

}
