package me.hao0.session.core;

import com.google.common.base.Strings;
import me.hao0.session.api.SessionManager;
import me.hao0.session.util.WebUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Session Filter
 */
public abstract class SessionFilter implements Filter{

    private final static Logger log = LoggerFactory.getLogger(SessionFilter.class);

    protected final static String SESSION_COOKIE_NAME = "sessionCookieName";

    protected final static String DEFAULT_SESSION_COOKIE_NAME = "sfid";

    /**
     * session cookie name
     */
    protected String sessionCookieName;

    protected final static String MAX_INACTIVE_INTERVAL = "maxInactiveInterval";

    /**
     * default 30 mins
     */
    protected final static int DEFAULT_MAX_INACTIVE_INTERVAL = 30 * 60;

    /**
     * max inactive interval
     */
    protected int maxInactiveInterval;

    /**
     * cookie domain
     */
    protected final static String COOKIE_DOMAIN = "cookieDomain";

    /**
     * cookie name
     */
    protected String cookieDomain;

    /**
     * cookie context path
     */
    protected final static String COOKIE_CONTEXT_PATH = "cookieContextPath";

    /**
     * default cookie context path
     */
    protected final static String DEFAULT_COOKIE_CONTEXT_PATH = "/";

    /**
     * cookie's context path
     */
    protected String cookieContextPath;

    /**
     * cookie max age
     */
    protected final static String COOKIE_MAX_AGE = "cookieMaxAge";

    /**
     * default cookie max age
     */
    protected final static int DEFAULT_COOKIE_MAX_AGE = -1;

    /**
     * cookie's life
     */
    protected int cookieMaxAge;

    /**
     * session manager
     */
    protected SessionManager sessionManager;

    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            sessionManager = createSessionManager();
            initAttrs(filterConfig);
        } catch (Exception ex) {
            log.error("failed to init session filter.", ex);
            throw new ServletException(ex);
        }
    }

    /**
     * subclass create session manager
     * @return session manager
     */
    protected abstract SessionManager createSessionManager() throws IOException;

    /**
     * init basic attribute
     * @param config the filter config
     */
    private void initAttrs(FilterConfig config){

        String param  = config.getInitParameter(SESSION_COOKIE_NAME);
        sessionCookieName = Strings.isNullOrEmpty(param) ? DEFAULT_SESSION_COOKIE_NAME : param;

        param = config.getInitParameter(MAX_INACTIVE_INTERVAL);
        maxInactiveInterval = Strings.isNullOrEmpty(param) ? DEFAULT_MAX_INACTIVE_INTERVAL : Integer.parseInt(param);

        cookieDomain = config.getInitParameter(COOKIE_DOMAIN);

        param = config.getInitParameter(COOKIE_CONTEXT_PATH);
        cookieContextPath = Strings.isNullOrEmpty(param) ? DEFAULT_COOKIE_CONTEXT_PATH : param;

        param = config.getInitParameter(COOKIE_MAX_AGE);
        cookieMaxAge = Strings.isNullOrEmpty(param) ? DEFAULT_COOKIE_MAX_AGE : Integer.parseInt(param);

        log.info("SessionFilter (sessionCookieName={},maxInactiveInterval={},cookieDomain={})", sessionCookieName, maxInactiveInterval, cookieDomain);
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (request instanceof HttpServletRequest2) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest2 request2 = new HttpServletRequest2(httpRequest, httpResponse, sessionManager);
        request2.setSessionCookieName(sessionCookieName);
        request2.setMaxInactiveInterval(maxInactiveInterval);
        request2.setCookieDomain(cookieDomain);
        request2.setCookieContextPath(cookieContextPath);
        request2.setCookieMaxAge(cookieMaxAge);

        // do other filter
        chain.doFilter(request2, response);

        // update session when request is handled
        HttpSession2 session = request2.currentSession();
        if (session != null) {
            if(!session.isValid()){
                // if invalidate , delete session
                log.debug("session is invalid, will delete.");
                WebUtil.failureCookie(httpRequest, httpResponse, sessionCookieName, cookieDomain, cookieContextPath);
            } else if (session.isDirty()) {
                // should flush to store
                log.debug("try to flush session to session store");
                Map<String, Object> snapshot = session.snapshot();
                if (sessionManager.persist(session.getId(), snapshot, maxInactiveInterval)) {
                    log.debug("succeed to flush session {} to store, key is:{}", snapshot, session.getId());
                } else {
                    log.error("failed to persist session to redis");
                    WebUtil.failureCookie(httpRequest, httpResponse, sessionCookieName, cookieDomain, cookieContextPath);
                }
            } else {
                // refresh expire time
                sessionManager.expire(session.getId(), maxInactiveInterval);
            }
        }
    }

    public void destroy() {
        sessionManager.destroy();
        log.debug("filter is destroy.");
    }
}
