package com.cxylk.agent2.common.util;


import com.cxylk.agent2.common.json.JsonReader;
import com.cxylk.agent2.common.json.JsonWriter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tommy on 2018/3/8.
 */
public class JsonUtil {
    public static String toJson(Object obj) {
        Map<String, Object> item = new HashMap<String, Object>();
        item.put("TYPE", false);
        item.put(JsonWriter.SKIP_NULL_FIELDS, true);
        String json = JsonWriter.objectToJson(obj, item);
        return json;
    }

    /**
     * jsonText 必须由 json-io 工具类生成 并且包含@type 属性
     *
     * @param tClass
     * @param jsonText
     * @param <T>
     * @return
     */
    public static <T> T toObject(Class<T> tClass, String jsonText) {
        return (T) JsonReader.jsonToJava(jsonText);
    }
}
