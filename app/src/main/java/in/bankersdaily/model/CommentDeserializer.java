package in.bankersdaily.model;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Handle nested rendered json fields
 * Replace "main_field": {"rendered": "required text"} as
 * "main_field": "required text"
 */

public class CommentDeserializer implements JsonDeserializer<Comment> {

    @Override
    public Comment deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .excludeFieldsWithoutExposeAnnotation()
                .create();

        JsonObject jsonObject = json.getAsJsonObject();
        Comment comment = gson.fromJson(json, Comment.class);
        comment.setContent(getRenderedString(jsonObject, "content"));
        comment.setAuthorAvatarUrl(jsonObject.getAsJsonObject("author_avatar_urls").get("48")
                .getAsString());

        return comment;
    }

    private String getRenderedString(JsonObject jsonObject, String key) {
        return jsonObject.getAsJsonObject(key).get("rendered").getAsString();
    }
}
