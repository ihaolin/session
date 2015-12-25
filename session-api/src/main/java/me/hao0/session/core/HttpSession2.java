
package me.hao0.session.core;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import me.hao0.session.api.SessionManager;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

/**
 * Http Session Wrapper
 */
public class HttpSession2 implements HttpSession {

    /**
     * session id
     */
    private final String id;

    /**
     * session created time
     */
    private final long createdAt;

    /**
     * session last access time
     */
    private volatile long lastAccessedAt;

    /**
     * session max active
     */
    private int maxInactiveInterval;

    private final ServletContext servletContext;

    /**
     * the session manager
     */
    private final SessionManager sessionManager;

    /**
     * the new attributes of the current request
     */
    private final Map<String, Object> newAttributes = Maps.newHashMap();

    /**
     * the deleted attributes of the current request
     */
    private final Set<String> deleteAttribute = Sets.newHashSet();

    /**
     * session attributes store
     */
    private final Map<String, Object> sessionStore;

    /**
     * true if session invoke invalidate()
     */
    private volatile boolean invalid;

    /**
     * true if session attrs updated
     */
    private volatile boolean dirty;

    public HttpSession2(String id, SessionManager sessionManager, ServletContext servletContext) {
        this.id = id;
        this.sessionManager = sessionManager;
        this.sessionStore = sessionManager.loadById(id);
        this.servletContext = servletContext;
        this.createdAt = System.currentTimeMillis();
        this.lastAccessedAt = createdAt;
    }

    @Override
    public long getCreationTime() {
        return createdAt;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getLastAccessedTime() {
        return lastAccessedAt;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    @Deprecated
    public HttpSessionContext getSessionContext() {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        checkValid();
        if (newAttributes.containsKey(name)) {
            return newAttributes.get(name);
        } else if (deleteAttribute.contains(name)) {
            return null;
        }
        return sessionStore.get(name);
    }

    @Override
    public Object getValue(String name) {
        return getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        checkValid();
        Set<String> names = Sets.newHashSet(sessionStore.keySet());
        names.addAll(newAttributes.keySet());
        names.removeAll(deleteAttribute);
        return Collections.enumeration(names);
    }

    @Override
    public String[] getValueNames() {
        checkValid();
        Set<String> names = Sets.newHashSet(sessionStore.keySet());
        names.addAll(newAttributes.keySet());
        names.removeAll(deleteAttribute);
        return names.toArray(new String[0]);
    }

    @Override
    public void setAttribute(String name, Object value) {
        checkValid();
        if (value != null) {
            newAttributes.put(name, value);
            deleteAttribute.remove(name);
        } else {
            deleteAttribute.add(name);
            newAttributes.remove(name);
        }
        dirty = true;
    }

    @Override
    public void putValue(String name, Object value) {
        setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        checkValid();
        deleteAttribute.add(name);
        newAttributes.remove(name);
        dirty = true;
    }

    @Override
    public void removeValue(String name) {
        removeAttribute(name);
        dirty = true;
    }

    @Override
    public void invalidate() {
        invalid = true;
        dirty = true;
        sessionManager.deleteById(this.getId());
    }

    public boolean isNew() {
        return Boolean.TRUE;
    }

    protected void checkValid() throws IllegalStateException {
        if (invalid){
            throw new IllegalStateException();
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    /**
     * get session attributes' snapshot
     * @return session attributes' map object
     */
    public Map<String, Object> snapshot() {
        Map<String, Object> snap = Maps.newHashMap();
        snap.putAll(sessionStore);
        snap.putAll(newAttributes);
        for (String name : deleteAttribute) {
            snap.remove(name);
        }
        return snap;
    }

    /**
     * the session is valid or not
     * @return return true if the session is valid, or false
     */
    public boolean isValid() {
        return !invalid;
    }
}