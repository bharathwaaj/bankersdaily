package in.bankersdaily.model;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import in.bankersdaily.network.ApiClient;

/**
 * Handle nested rendered json fields
 * Replace "main_field": {"rendered": "required text"} as
 * "main_field": "required text"
 */

public class PostDeserializer implements JsonDeserializer<Post> {

    @Override
    public Post deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        Gson gson = ApiClient.getGsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();

        JsonObject jsonObject = json.getAsJsonObject();
        Post post = gson.fromJson(json, Post.class);
        post.setTitle(getRenderedString(jsonObject, "title"));
        post.setContent(getRenderedString(jsonObject, "content"));
        return post;
    }

    private String getRenderedString(JsonObject jsonObject, String key) {
        return jsonObject.getAsJsonObject().getAsJsonObject(key).get("rendered").getAsString();
    }
}
