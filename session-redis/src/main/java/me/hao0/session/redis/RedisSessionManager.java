package me.hao0.session.redis;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import me.hao0.session.api.SessionIdGenerator;
import me.hao0.session.core.AbstractSessionManager;
import me.hao0.session.exception.SessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Redis-Based Session Manager
 */
public class RedisSessionManager extends AbstractSessionManager {

    private final static Logger log = LoggerFactory.getLogger(RedisSessionManager.class);

    private static final String SENTINEL_MODE = "sentinel";

    private String sessionPrefix;

    private volatile RedisExecutor executor;

    public RedisSessionManager() throws IOException {}

    /**
     * @param propertiesFile properties file in classpath, default is session.properties
     */
    public RedisSessionManager(String propertiesFile) throws IOException {
        super(propertiesFile);
    }

    /**
     * init subclass
     */
    @Override
    protected void init(Properties props) {
        this.sessionPrefix = props.getProperty("session.redis.prefix", "rsession");
        initJedisPool(props);
    }

    /**
     * init jedis pool with properties
     * @param props
     */
    private void initJedisPool(Properties props) {
        JedisPoolConfig config = new JedisPoolConfig();

        config.setTestOnBorrow(true);

        Integer maxIdle = Integer.parseInt(props.getProperty("session.redis.pool.max.idle", "2"));
        config.setMaxIdle(maxIdle);

        Integer maxTotal = Integer.parseInt(props.getProperty("session.redis.pool.max.total", "5"));
        config.setMaxTotal(maxTotal);

        final String mode = props.getProperty("session.redis.mode");
        if (Objects.equal(mode, SENTINEL_MODE)){
            // sentinel
            this.executor = new RedisExecutor(config, true, props);
        } else {
            // standalone
            this.executor = new RedisExecutor(config, false, props);
        }
    }

    /**
     * persist session to session store
     * @param id session id
     * @param snapshot session attributes' snapshot
     * @param maxInactiveInterval session max life(seconds)
     * @return true if save successfully, or false
     */
    public Boolean persist(final String id, final Map<String, Object> snapshot, final int maxInactiveInterval) {
        final String sid = sessionPrefix + ":" + id;
        try {
            this.executor.execute(new RedisCallback<Void>() {
                public Void execute(Jedis jedis) {
                    if (snapshot.isEmpty()) {
                        // delete session
                        jedis.del(sid);
                    } else {
                        // set session
                        jedis.setex(sid, maxInactiveInterval, serializer.serialize(snapshot));
                    }
                    return null;
                }
            });
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("failed to persist session(id={}, snapshot={}), cause:{}",
                    sid, snapshot, Throwables.getStackTraceAsString(e));
            return Boolean.FALSE;
        }
    }

    /**
     * load session by id
     * @param id session id
     * @return session map object
     */
    public Map<String, Object> loadById(String id) {
        final String sid = sessionPrefix + ":" + id;
        try {
            return this.executor.execute(new RedisCallback<Map<String, Object>>() {
                public Map<String, Object> execute(Jedis jedis) {
                    String session = jedis.get(sid);
                    if (!Strings.isNullOrEmpty(session)) {
                        return serializer.deserialize(session);
                    }
                    return Collections.emptyMap();
                }
            });
        } catch (Exception e) {
            log.error("failed to load session(key={}), cause:{}", sid, Throwables.getStackTraceAsString(e));
            throw new SessionException("load session failed", e);
        }
    }

    /**
     * delete session physically
     * @param id session id
     */
    public void deleteById(String id) {
        final String sid = sessionPrefix + ":" + id;
        try {
            this.executor.execute(new RedisCallback<Void>() {
                public Void execute(Jedis jedis) {
                    jedis.del(sid);
                    return null;
                }
            });
        } catch (Exception e) {
            log.error("failed to delete session(key={}) in redis,cause:{}", sid, Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * expired session
     * @param sid current session id
     * @param maxInactiveInterval max life(seconds)
     */
    public void expire(String sid, final int maxInactiveInterval) {
        final String sessionId = sessionPrefix + ":" + sid;
        try {
            this.executor.execute(new RedisCallback<Void>() {
                public Void execute(Jedis jedis) {
                    jedis.expire(sessionId, maxInactiveInterval);
                    return null;
                }
            });
        } catch (Exception e) {
            log.error("failed to refresh expire time session(key={}) in redis,cause:{}",
                    sessionId, Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * session manager destroy when filter destroy
     * destroy executor
     */
    public void destroy() {
        if (executor != null) {
            executor.getJedisPool().destroy();
        }
    }

    /**
     * get session id generator
     * @return session id generator
     */
    public SessionIdGenerator getSessionIdGenerator() {
        return this.sessionIdGenerator;
    }
}
