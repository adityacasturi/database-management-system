package model;

import com.google.gson.*;
import java.lang.reflect.Type;

public class ColumnSchemaTypeAdapter implements JsonDeserializer<ColumnSchema>, JsonSerializer<ColumnSchema> {
    @Override
    public ColumnSchema deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("columnType").getAsString();

        if ("INT_TYPE".equals(type)) {
            return context.deserialize(json, IntColumnSchema.class);
        } else if ("STRING_TYPE".equals(type)) {
            return context.deserialize(json, StringColumnSchema.class);
        } else {
            throw new JsonParseException("Unknown column type: " + type);
        }
    }

    @Override
    public JsonElement serialize(ColumnSchema src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src);
    }
}