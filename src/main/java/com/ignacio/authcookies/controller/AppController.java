package com.ignacio.authcookies.controller;

import com.ignacio.authcookies.model.StaticData;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import eu.bitwalker.useragentutils.*;

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
                if (cookie.getName().startsWith("user-") && cookie.getValue().startsWith("logged_in")) {

                    // User logged in, redirect to main
                    mAV.addObject("user", cookie.getName().replace("user-", ""));
                    mAV.setViewName("redirect:/main");
                    return mAV;
                } else if (cookie.getName().startsWith("user-") && cookie.getValue().equals("logged_out")) {

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
                Cookie cookie = new Cookie("user-"+username, "logged_in-1");
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
                        if (cookie.getName().startsWith("user-") && cookie.getValue().equals("logged_out")) {
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
        Cookie cookie = new Cookie("user-"+username, "logged_out");
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

    @GetMapping("/main")
    public ModelAndView main(@RequestParam String user, HttpServletRequest request, HttpServletResponse response) {

        mAV = new ModelAndView();

        // Check cookie logged times and increment
        Cookie[] cookies = request.getCookies();
        String times = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("user-"+user) && cookie.getValue().startsWith("logged_in")) {
                    int times2 = Integer.parseInt(cookie.getValue().split("-")[1]);
                    times2++;
                    times = String.valueOf(times2);
                    cookie.setValue("logged_in-" + times);
                    response.addCookie(cookie);
                    break;
                }
            }
        }

        if (times != null){
            mAV.addObject("timesLoggedIn", times);
        }

        // Obtain brand, model and version of browser
        String userAgentString = request.getHeader("User-Agent");
        UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);

        String marca = userAgent.getBrowser().getManufacturer().toString();
        String modelo = userAgent.getBrowser().getName();
        String version = userAgent.getBrowserVersion().toString();
        mAV.addObject("marca", marca);
        mAV.addObject("modelo", modelo);
        mAV.addObject("version", version);


        mAV.addObject("user", user);
        mAV.setViewName("main");
        System.out.println("User logged in: " + user);
        return mAV;
    }

    @PostMapping("/recover-password")
    @ResponseBody
    public String recoverPassword(@RequestBody Map<String, String> usuario) {
        String username = usuario.get("usuario");
        String pass = getUsers().get(username);
        return pass;
    }

    @PostMapping("/recover-user")
    @ResponseBody
    public String recoverUser() {
        // Recover all users
        StringBuilder users = new StringBuilder();
        for (String user : getUsers().keySet()) {
            users.append(user).append(", ");
        }

        return users.substring(0, users.length()-2);
    }
}

