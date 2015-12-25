package me.hao0.session.redis;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Pool;

import java.util.Properties;
import java.util.Set;

/**
 * Redis Executor
 */
public class RedisExecutor {

    /**
     * jedis instance pool
     */
    private volatile Pool<Jedis> jedisPool;

    public RedisExecutor(JedisPoolConfig config, boolean sentinel, Properties props) {
        if (sentinel) {
            // if sentinel
            String sentinelProps = props.getProperty("session.redis.sentinel.hosts");
            Iterable<String> hosts = Splitter.on(',').trimResults().omitEmptyStrings().split(sentinelProps);
            final Set<String> sentinelHosts = Sets.newHashSet(hosts);
            String masterName = props.getProperty("session.redis.sentinel.master.name");
            this.jedisPool = new JedisSentinelPool(masterName, sentinelHosts, config);
        } else {
            // if standalone
            String redisHost = props.getProperty("session.redis.host");
            int redisPort = Integer.parseInt(props.getProperty("session.redis.port"));
            this.jedisPool = new JedisPool(config, redisHost, redisPort);
        }
    }

    /**
     * execute a redis operation
     * @param cb callback
     * @param <V> return type
     * @return result
     */
    public <V> V execute(RedisCallback<V> cb) {
        Jedis jedis = jedisPool.getResource();
        boolean success = true;
        try {
            return cb.execute(jedis);
        } catch (JedisException e) {
            success = false;
            if (jedis != null) {
                jedisPool.returnBrokenResource(jedis);
            }
            throw e;
        } finally {
            if (success) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * return the jedis pool
     * @return current jedis pool
     */
    public Pool<Jedis> getJedisPool() {
        return jedisPool;
    }
}
