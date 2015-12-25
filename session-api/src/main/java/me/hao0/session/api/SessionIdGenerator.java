package me.hao0.session.api;

import javax.servlet.http.HttpServletRequest;

/**
 * Session ID生成器
 */
public interface SessionIdGenerator {

    /**
     * 生成Session Id
     * @param request 请求对象
     * @return session id
     */
    String generate(HttpServletRequest request);
}
