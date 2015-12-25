package me.hao0.session.redis;

import redis.clients.jedis.Jedis;

/**
 * Jedis Execute Callback
 * @param <V> Return Type
 */
public interface RedisCallback<V> {

    /**
     * execute jedis operation
     * @param jedis jedis instance
     * @return result
     */
    V execute(Jedis jedis);
}
