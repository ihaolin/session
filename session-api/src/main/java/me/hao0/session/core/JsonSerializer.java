package me.hao0.session.core;

import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.Throwables;
import me.hao0.common.json.Jsons;
import me.hao0.session.api.Serializer;
import me.hao0.session.exception.SerializeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

/**
 * Json Serializer
 */
public final class JsonSerializer implements Serializer {

    private final static Logger log = LoggerFactory.getLogger(JsonSerializer.class);

    private static final JavaType MAP_TYPE = Jsons.DEFAULT.createCollectionType(Map.class, String.class, Object.class);

    @Override
    public String serialize(Object o) {
        try {
            return Jsons.DEFAULT.toJson(o);
        } catch (Exception e) {
            log.error("failed to serialize http session {} to json,cause:{}", o, Throwables.getStackTraceAsString(e));
            throw new SerializeException("failed to serialize http session to json", e);
        }
    }

    @Override
    public Map<String,Object> deserialize(String o) {
        try {
            return Jsons.DEFAULT.fromJson(o, MAP_TYPE);
        } catch (Exception e) {
            log.error("failed to deserialize string  {} to http session,cause:{} ", o, Throwables.getStackTraceAsString(e));
            throw new SerializeException("failed to deserialize string to http session", e);
        }
    }
}
