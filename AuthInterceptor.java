package com.rt.shiro;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rt.entity.ChannelPartnerMember;
import com.rt.exception.BusinessException;
import com.rt.exception.code.BaseResponseCode;
import com.rt.service.ChannelPartnerService;
import com.rt.service.RedisService;
import com.rt.utils.DataResult;
import com.rt.utils.HttpTools;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * @author yangkai
 * date 2020/9/15
 */
public class AuthInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private RedisService redisService;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 拦截处理代码
        System.out.println("拦截到了");
        String token  = request.getHeader("Authorization");
        // 如果是OPTIONS请求则结束
        /*if (HttpMethod.OPTIONS.toString().equals(request.getMethod())) {
            response.setStatus(HttpStatus.OK.value());
            return true;
        }*/
        try{
            if (StringUtils.isBlank(token)) {
                throw new BusinessException(BaseResponseCode.TOKEN_LOSE);
            } else {
                String mobile = (String) redisService.get(token);
                if(StringUtils.isBlank(mobile)) {
                    JSONObject json = HttpTools.validToken(token);
                    mobile = json.get("mobile").toString();
                    if(StringUtils.isBlank(mobile))
                        throw new BusinessException(BaseResponseCode.TOKEN_LOSE);
                    else
                        redisService.set(token,mobile,1, TimeUnit.HOURS);
                }

                request.setAttribute("mobile", mobile);
            }
            return true;
        }catch (Exception e) {
            throw new BusinessException(BaseResponseCode.TOKEN_LOSE);
        }
    }
}
