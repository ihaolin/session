package me.hao0.session.redis;

import me.hao0.session.api.SessionManager;
import me.hao0.session.core.SessionFilter;
import java.io.IOException;

/**
 * Redis Session Filter
 */
public class RedisSessionFilter extends SessionFilter {

    /**
     * subclass create session manager
     * @return session manager
     */
    @Override
    protected SessionManager createSessionManager() throws IOException{
        return new RedisSessionManager();
    }
}
