
package me.hao0.session.core;

import me.hao0.session.api.SessionManager;
import me.hao0.session.util.WebUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * HttpServletRequest Wrapper
 */
public class HttpServletRequest2 extends javax.servlet.http.HttpServletRequestWrapper {

    private final static Logger log = LoggerFactory.getLogger(HttpServletRequest2.class);

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private final SessionManager sessionManager;

    private HttpSession2 session;

    private String sessionCookieName;

    private String cookieDomain;

    private String cookieContextPath;

    private int maxInactiveInterval;

    private int cookieMaxAge;

    public HttpServletRequest2(HttpServletRequest request, HttpServletResponse response, SessionManager sessionManager) {
        super(request);
        this.request = request;
        this.response = response;
        this.sessionManager = sessionManager;
    }

    @Override
    public HttpSession getSession(boolean create) {
        return doGetSession(create);
    }

    /**
     * get session instance, create new one if not exsit
     */
    @Override
    public HttpSession getSession() {
        return doGetSession(true);
    }


    /**
     * get session id name in cookie
     */
    public String getSessionCookieName() {
        return sessionCookieName;
    }

    /**
     * set session id in cookie
     * @param sessionCookieName session name in cookie
     */
    public void setSessionCookieName(String sessionCookieName) {
        this.sessionCookieName = sessionCookieName;
    }

    /**
     * get cookie cookie's domain
     * @return cookie's store domain
     */
    public String getCookieDomain() {
        return this.cookieDomain;
    }

    /**
     * set cookie cookie's domain
     */
    public void setCookieDomain(String cookieDomain) {
        this.cookieDomain = cookieDomain;
    }

    /**
     * get cookie's store path
     * @return cookie's store path
     */
    public String getCookieContextPath() {
        return cookieContextPath;
    }

    /**
     * set cookie's store path
     */
    public void setCookieContextPath(String cookieContextPath) {
        this.cookieContextPath = cookieContextPath;
    }


    /**
     * set session inactive life (seconds)
     */
    public void setMaxInactiveInterval(int maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }

    /**
     * set cookie max age
     * @param cookieMaxAge cookie max age
     */
    public void setCookieMaxAge(int cookieMaxAge) {
        this.cookieMaxAge = cookieMaxAge;
    }

    /**
     * get current session
     * @return the current session instance
     */
    public HttpSession2 currentSession() {
        return session;
    }

    /**
     * get session from session cookie name
     * @param create if true create
     * @return session
     */
    private HttpSession doGetSession(boolean create) {
        if (session == null) {
            Cookie cookie = WebUtil.findCookie(this, getSessionCookieName());
            if (cookie != null) {
                String value = cookie.getValue();
                log.debug("discovery session id from cookie: {}", value);
                session = buildSession(value, false);
            } else {
                session = buildSession(create);
            }
        } else {
            log.debug("Session[{}] was existed.", session.getId());
        }
        return session;
    }

    /**
     * build a new  session from session id
     * @param id session id
     * @param refresh refresh cookie or not
     * @return session
     */
    private HttpSession2 buildSession(String id, boolean refresh) {
        HttpSession2 session = new HttpSession2(id, sessionManager, request.getServletContext());
        session.setMaxInactiveInterval(maxInactiveInterval);
        if (refresh) {
            WebUtil.addCookie(this, response, getSessionCookieName(), id,
                    getCookieDomain(), getCookieContextPath(), cookieMaxAge, true);
        }
        return session;
    }

    /**
     * build a new session
     * @param create create session or not
     * @return session
     */
    private HttpSession2 buildSession(boolean create) {
        if (create) {
            session = buildSession(sessionManager.getSessionIdGenerator().generate(request), true);
            log.debug("Build new session[{}].", session.getId());
            return session;
        } else {
            return null;
        }
    }
}
