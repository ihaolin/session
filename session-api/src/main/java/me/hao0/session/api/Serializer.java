package me.hao0.session.api;

import java.util.Map;

/**
 * 序列化接口
 */
public interface Serializer {

    /**
     * serialize object to json string
     * @param o object
     * @return json string
     */
    String serialize(Object o);

    /**
     * deserialize json string to map
     * @param json json string
     * @return map
     */
    Map<String, Object> deserialize(String json);
}