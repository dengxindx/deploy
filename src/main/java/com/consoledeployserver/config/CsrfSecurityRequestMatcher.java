package com.consoledeployserver.config;

import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class CsrfSecurityRequestMatcher implements RequestMatcher {

//    private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");

    @Override
    public boolean matches(HttpServletRequest request) {
        List<String> unExecludeUrls = new ArrayList<>();
        unExecludeUrls.add("/deploy");//允许该post请求的url路径

        if (unExecludeUrls != null && unExecludeUrls.size() > 0) {
            String servletPath = request.getServletPath();
            for (String url : unExecludeUrls) {
                if (servletPath.contains(url)) {
                    return false;
                }
            }
        }
//        return allowedMethods.matcher(request.getMethod()).matches();
        return false;
    }
}