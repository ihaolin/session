package me.hao0.session.api;

import me.hao0.session.core.HttpSession2;
import java.util.Map;

/**
 * Session Manager
 */
public interface SessionManager {

    /**
     * persist session to session store
     * @param id session id
     * @param snapshot session attributes' snapshot
     * @param maxInactiveInterval session max life(seconds)
     * @return true if save successfully, or false
     */
    Boolean persist(final String id, final Map<String, Object> snapshot, final int maxInactiveInterval);

    /**
     * load session by id
     * @param id session id
     * @return the session map object
     */
    Map<String, Object> loadById(String id);

    /**
     * delete session physically
     * @param id session id
     */
    void deleteById(String id);

    /**
     * set session expired time
     * @param sid current session id
     * @param maxInactiveInterval max life(seconds)
     */
    void expire(String sid, final int maxInactiveInterval);

    /**
     * session manager destroy when filter destroy
     */
    void destroy();

    /**
     * get session id generator
     * @return session id generator
     */
    SessionIdGenerator getSessionIdGenerator();
}
