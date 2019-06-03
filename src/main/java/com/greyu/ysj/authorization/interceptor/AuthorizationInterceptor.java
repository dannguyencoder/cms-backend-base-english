package com.greyu.ysj.authorization.interceptor;

import com.greyu.ysj.authorization.annotation.Authorization;
import com.greyu.ysj.authorization.manager.TokenManager;
import com.greyu.ysj.authorization.model.TokenModel;
import com.greyu.ysj.config.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @Description: Customize the interceptor to determine if the request has permission
 * @see com.greyu.ysj.authorization.annotation.Authorization
 * @Author: gre_yu@163.com
 * @Date: Created in 8:48 2018/2/1
 */
@Component
public class AuthorizationInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private TokenManager manager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println(request.getRequestURI());
//        If not mapped to the method passed directly
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
//        Get the token from the header
        String authorization = request.getHeader(Constants.AUTHORIZATION);

//        Verify token
        TokenModel model = this.manager.getToken(authorization);

        String url = request.getRequestURI();
        if (method.getAnnotation(Authorization.class) != null) {
            if (null == model) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            // Not admin can't access /admin interface
            if (url.split("/")[1].equals("admin")) {
                if (String.valueOf(model.getUserId()).length() != 3) {
                    System.out.println("ss");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return false;
                }
            }

//            if (url.split("/")[1].equals("user")) {
//                if (String.valueOf(model.getUserId()).length() != 8) {
//                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//                    return false;
//                }
//            }
        }


        if (this.manager.checkToken(model)) {
//            If the token verification succeeds, the userId corresponding to the token is stored in the request, which is convenient for later injection.
            request.setAttribute(Constants.CURRENT_USER_ID, model.getUserId());
            return true;
        }
//        If the verification token fails and the method indicates Authorization, a 401 error is returned.
        if (method.getAnnotation(Authorization.class) != null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        return true;
    }
}
