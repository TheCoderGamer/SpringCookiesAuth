package com.ignacio.authcookies.controller;

import com.ignacio.authcookies.model.StaticData;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;


@Controller
//@RequestMapping("correo")
public class AppController {

    ModelAndView mAV = new ModelAndView();

    @ModelAttribute
    public Map<String, String> getUsers() {
        return StaticData.getUsers();
    }



    @GetMapping("/")
    public ModelAndView redirect() {
        mAV.setViewName("redirect:/login");
        return mAV;
    }




    @GetMapping("/login")
    public ModelAndView login(HttpServletRequest request) {

        mAV = new ModelAndView();

        // Check cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {

            ArrayList<String> lastUsers = new ArrayList<>();

            for (Cookie cookie : cookies) {

                // Check cookie
                if (cookie.getName().startsWith("user-") && cookie.getValue().equals("logged-in")) {

                    // User logged in, redirect to main
                    mAV.addObject("user", cookie.getName().replace("user-", ""));
                    mAV.setViewName("redirect:/main");
                    return mAV;
                } else if (cookie.getName().startsWith("user-") && cookie.getValue().equals("logged-out")) {

                    // User logged out, continue checking
                    lastUsers.add(cookie.getName().replace("user-", ""));
                }
            }

            // If there are users logged out, show them
            if (lastUsers.size() > 0) {
                mAV.addObject("lastUsers", lastUsers);
                mAV.setViewName("login");
                return mAV;
            }
        }

        // Goto login
        mAV.setViewName("login");
        return mAV;
    }

    @PostMapping("/login")
    public ModelAndView login(
            @RequestParam String username,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String lastUser,
            HttpServletResponse response,
            HttpServletRequest request) {


        mAV = new ModelAndView();

        if (password == null) {
            // -- Phase 1 -- Request user
            // Check user
            if (getUsers().containsKey(username)){
                // User found, request password
                mAV.addObject("user", username);
                mAV.setViewName("login");
                return mAV;
            }else {
                // User not found
                mAV.addObject("error", "User not found");
                mAV.setViewName("login");
                return mAV;
            }
        }else {
            // -- Phase 2 -- Request password
            // Check user (only happens if lastUser exists in view)
            if (!getUsers().containsKey(username)){
                // User not found
                if (lastUser != null) {
                    // Send back list of last users
                    String[] lastUsers = lastUser.substring(0, lastUser.length()-1).split(", ");
                    mAV.addObject("lastUsers", lastUsers);
                }
                mAV.addObject("error", "User not found");
                mAV.setViewName("login");
                return mAV;
            // Check password
            } else if (getUsers().get(username).equals(password)) {
                // Auth correct, set cookie
                Cookie cookie = new Cookie("user-"+username, "logged-in");
                response.addCookie(cookie);
                mAV.setViewName("redirect:/");
                return mAV;
            }else {
                // Auth incorrect
                mAV.addObject("user", username);
                mAV.addObject("error", "Password incorrect");

                // Check cookie if other user is logged out
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    ArrayList<String> lastUsers = new ArrayList<>();
                    for (Cookie cookie : cookies) {
                        if (cookie.getName().startsWith("user-") && cookie.getValue().equals("logged-out")) {
                            lastUsers.add(cookie.getName().replace("user-", ""));
                        }
                    }
                    if (lastUsers.size() > 0) {
                        mAV.addObject("lastUsers", lastUsers);
                    }
                }

                // Goto login
                mAV.setViewName("login");
                return mAV;
            }
        }
    }


    @PostMapping("/logout")
    public ModelAndView logout(
            @RequestParam String username,
            HttpServletResponse response) {

        mAV = new ModelAndView();

        // Set user as logged-out
        Cookie cookie = new Cookie("user-"+username, "logged-out");
        response.addCookie(cookie);

        mAV.setViewName("redirect:/");
        return mAV;
    }


    @PostMapping("/logout-force")
    public ModelAndView forceLogout(
            @RequestParam String username,
            HttpServletResponse response) {

        mAV = new ModelAndView();

        // Delete cookie
        Cookie cookie = new Cookie("user-"+username, "");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        mAV.setViewName("redirect:/");
        return mAV;
    }

    @GetMapping("/delete-last-users")
    public ModelAndView deleteLastUsers(
            HttpServletResponse response,
            HttpServletRequest request) {

        mAV = new ModelAndView();

        // Delete cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().startsWith("user-") && cookie.getValue().equals("logged-out")) {
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }

        mAV.setViewName("redirect:/");
        return mAV;
    }

    @GetMapping("/main")
    public ModelAndView main(@RequestParam String user) {

        mAV = new ModelAndView();
        mAV.addObject("user", user);
        mAV.setViewName("main");
        return mAV;
    }
}

