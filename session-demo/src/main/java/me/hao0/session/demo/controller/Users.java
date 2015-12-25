package me.hao0.session.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: haolin
 * Email: haolin.h0@gmail.com
 * Date: 25/12/15
 */
@Controller
@RequestMapping("/users")
public class Users {

    private static final String USER_SESSION_ID = "uid";

    private final Map<String, Long> userStore;

    private Users(){
        userStore = new HashMap<>();
        userStore.put("admin", 1L);
        userStore.put("haolin", 2L);
    }

    @RequestMapping("/index")
    public String index(){
        return "index";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(String username, String passwd, HttpSession session){

        if (userStore.containsKey(username)){
            if (username.equals(passwd)){
                // put use session id when login successfully
                session.setAttribute(USER_SESSION_ID, userStore.get(username));
                session.setAttribute("user", username);
            }
        }

        return "index";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpSession session){

        session.invalidate();

        return "index";
    }
}
