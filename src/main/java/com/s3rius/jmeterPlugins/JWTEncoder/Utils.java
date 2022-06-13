package com.s3rius.jmeterPlugins.JWTEncoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class Utils {
    /***
     * Converts JsonElement to Object.
     * 
     * For arrays it returns ArrayList,
     * for json objects it returns HashMap<String, Object>,
     * for privitives it returns primitive java type.
     */
    public static Object jsonToObject(JsonElement element) {
        if (element.isJsonNull()) {
            return null;
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            ArrayList<Object> result = new ArrayList<>();
            for (JsonElement object : array) {
                result.add(jsonToObject(object));
            }
            return result;
        } else if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            HashMap<String, Object> result = new HashMap<>();
            for (Entry<String, JsonElement> entry : object.entrySet()) {
                result.put(entry.getKey(), jsonToObject(entry.getValue()));
            }
            return result;
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) {
                return primitive.getAsString();
            } else if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            } else if (primitive.isNumber()) {
                Number number = primitive.getAsNumber();
                if (number.toString().contains(".")) {
                    return number.doubleValue();
                }
                return number.longValue();
            }
        }
        return null;
    }


}
