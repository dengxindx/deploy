package com.consoledeployserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

@Controller
@Slf4j
public class HomeController {

    @GetMapping("/login")
    public String login(HttpServletRequest request){
        log.info("【{}】请求登录", request.getRemoteAddr());
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) throws ServletException {
        log.info("【{}】请求退出，当前用户为：【{}】", request.getRemoteAddr(), request.getUserPrincipal() == null ? null : request.getUserPrincipal().getName());
        if (request.getUserPrincipal() != null){
            request.logout();
        }
        return "login";
    }
}
