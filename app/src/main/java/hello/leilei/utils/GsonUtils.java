/*
 * Copyright (c) 2016. Hefei Royalstar Electronic Appliance Group Co., Ltd. All rights reserved.
 */

package hello.leilei.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;

/**
 * USER: liulei
 * DATE: 2015/3/13.
 * TIME: 14:20
 */
public class GsonUtils {

    static Gson gson;

    public static Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    private GsonUtils() {
        gson = new Gson();
    }

    public static String parseObjectToJsonString(Object object) {
        return getGson().toJson(object);
    }

    public static <T> List<T> parseJsonArray(Class<T> clazz, String result) {
        try {
            JsonParser parser = new JsonParser();
            JsonElement parse = parser.parse(result);
            JsonArray Jarray = parse.getAsJsonArray();
            List<T> list = new ArrayList<T>(5);
            for (JsonElement element : Jarray) {
                T object = getGson().fromJson(element, clazz);
                list.add(object);
            }
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static <T> List<T> parseJsonArray(Class<T> clazz, JsonArray jsonArray) {
        List<T> list = new ArrayList<T>(5);
        for (JsonElement element : jsonArray) {
            T object = getGson().fromJson(element, clazz);
            list.add(object);
        }
        return list;
    }

    public static JsonObject parseJsonObject(String json) {
        JsonObject jsonObject = null;
        JsonParser jsonParser = new JsonParser();
        try {
            boolean isJsonObject = jsonParser.parse(json).isJsonObject();
            if (isJsonObject) {
                jsonObject = jsonParser.parse(json).getAsJsonObject();
            }
        } catch (JsonSyntaxException se) {
            se.printStackTrace();
        }
        return jsonObject;
    }

    public static <T> T parseJson(String gson, Class<T> clazz) {
        try {
            if (TextUtils.isEmpty(gson))
                return null;
            return getGson().fromJson(gson, clazz);
        } catch (JsonSyntaxException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static <T> T parseJson(JsonElement element, Class<T> clazz) {
        try {
            return getGson().fromJson(element, clazz);
        } catch (JsonSyntaxException ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
