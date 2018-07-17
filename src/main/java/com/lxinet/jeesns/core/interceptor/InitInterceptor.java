package com.lxinet.jeesns.core.interceptor;

import com.lxinet.jeesns.core.annotation.After;
import com.lxinet.jeesns.core.annotation.Before;
import com.lxinet.jeesns.core.annotation.Clear;
import com.lxinet.jeesns.core.utils.*;
import com.lxinet.jeesns.modules.mem.entity.Member;
import com.lxinet.jeesns.modules.mem.service.IMemberService;
import com.lxinet.jeesns.modules.sys.entity.Config;
import com.lxinet.jeesns.modules.sys.service.IConfigService;
import com.lxinet.jeesns.modules.sys.service.impl.ConfigServiceImpl;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zchuanzhao on 16/9/26.
 * sss springmvc 2 中的拦截器,每次请求都拦截用来设置freemarker里面的公共变量,譬如${base} ${SITE_KEYS}等
 */
public class InitInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
        if(StringUtils.isEmpty(Const.PROJECT_PATH)){
            Const.PROJECT_PATH = httpServletRequest.getContextPath();
        }
        //sss freemarker 5. 用来设置freemarker模板里面的${base}
        httpServletRequest.setAttribute("base",Const.PROJECT_PATH);
        JeesnsConfig jeesnsConfig = SpringContextHolder.getBean("jeesnsConfig");
        httpServletRequest.setAttribute("jeesnsConfig",jeesnsConfig);
        String managePath = Const.PROJECT_PATH + "/" + jeesnsConfig.getManagePath();
        
        //sss freemarker 6. 用来设置freemarker模板(ftl)里面的访问的 ${managerPath}
        httpServletRequest.setAttribute("managePath",managePath);
        ConfigServiceImpl configService = SpringContextHolder.getBean("configService");
        
        //得到当前登录系统用户
        Member loginUser = MemberUtil.getLoginMember(httpServletRequest);
        httpServletRequest.setAttribute("loginUser",loginUser);
        if (loginUser != null) {
        	//是否已激活，0未激活，1已激活
            if(loginUser.getIsActive() == 0){
                Map<String,String> configMap = configService.getConfigToMap();
                if(Integer.parseInt(configMap.get(ConfigUtil.MEMBER_EMAIL_VALID)) == 1){
                    if(!(httpServletRequest.getServletPath().indexOf("member/active") != -1 || httpServletRequest.getServletPath().indexOf("member/logout") != -1 ||
                            httpServletRequest.getServletPath().indexOf("member/sendEmailActiveValidCode") != -1 || httpServletRequest.getServletPath().indexOf("/res/") != -1 ||
                            httpServletRequest.getServletPath().indexOf("/upload/") != -1)){
                    	//如果不是上述页面,那么就转换到激活页面
                        httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/member/active");
                        return false;
                    }
                }
            }
        }
        //从数据库拿出配置信息,
        List<Config> configList = configService.allList();
        for (Config config : configList) {
        	//sss freemarker 4. 把数据库中拿出的配置信息的所有key转化成大写,并且放入到request里面,方便在freemarker模版里面的调访问用 如${SITE_KEYS} ${SITE_DESCRIPTION}等
            httpServletRequest.setAttribute(config.getJkey().toUpperCase(),config.getJvalue());
        }

        if(handler != null){
            List<Annotation> annotationList = new ArrayList<>();
            if(handler.getClass().isAssignableFrom(HandlerMethod.class)){
                Class clazz = ((HandlerMethod)handler).getMethod().getDeclaringClass();
                Annotation[] classAnnotations = clazz.getAnnotations();
                for (Annotation annotation : classAnnotations){
                    annotationList.add(annotation);
                }
                Annotation[] methodAnnotations = ((HandlerMethod) handler).getMethod().getAnnotations();
                for (Annotation annotation : methodAnnotations){
                    annotationList.add(annotation);
                }
                for (int i = 0;i < annotationList.size();i ++){
                    boolean hasClear = false;
                    Annotation annotation = annotationList.get(i);
                    //获取Before注解
                    Before before = null;
                    try {
                        before = (Before) annotation;
                    }catch (Exception e){

                    }
                    if(before != null){
                        for (int j = i+1;j < annotationList.size();j ++){
                            Annotation annotation1 = annotationList.get(j);
                            Clear clear = null;
                            try {
                                clear = (Clear) annotation1;
                            }catch (Exception e){

                            }
                            if(clear != null){
                                hasClear = true;
                                break;
                            }
                        }
                        //在@Before注解后面如果有@Clear注解，该注解就无效
                        if(!hasClear){
                            Class<? extends JeesnsInterceptor> interceptorlll = before.value();
                            Object object = Class.forName(interceptorlll.getCanonicalName()).newInstance();
                            Class[] clazzs = new Class[]{HttpServletRequest.class,HttpServletResponse.class,Object.class};
                            Method method = object.getClass().getMethod("interceptor",clazzs);
                            Object[] params = new Object[]{httpServletRequest,httpServletResponse,handler};
                            boolean result = (boolean) method.invoke(object,params);
                            return result;
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler, Exception e) throws Exception {
        if(handler != null){
            List<Annotation> annotationList = new ArrayList<>();
            if(handler.getClass().isAssignableFrom(HandlerMethod.class)){
                Class clazz = ((HandlerMethod)handler).getMethod().getDeclaringClass();
                Annotation[] classAnnotations = clazz.getAnnotations();
                for (Annotation annotation : classAnnotations){
                    annotationList.add(annotation);
                }
                Annotation[] methodAnnotations = ((HandlerMethod) handler).getMethod().getAnnotations();
                for (Annotation annotation : methodAnnotations){
                    annotationList.add(annotation);
                }
                for (int i = 0;i < annotationList.size();i ++){
                    boolean hasClear = false;
                    Annotation annotation = annotationList.get(i);
                    //获取After注解
                    After after = null;
                    try {
                        after = (After) annotation;
                    }catch (Exception e1){

                    }
                    if(after != null){
                        for (int j = i+1;j < annotationList.size();j ++){
                            Annotation annotation1 = annotationList.get(j);
                            Clear clear = null;
                            try {
                                clear = (Clear) annotation1;
                            }catch (Exception e1){

                            }
                            if(clear != null){
                                hasClear = true;
                                break;
                            }
                        }
                        //在@After注解后面如果有@Clear注解，该注解就无效
                        if(!hasClear){
                            Class<? extends JeesnsInterceptor> interceptorlll = after.value();
                            Object object = Class.forName(interceptorlll.getCanonicalName()).newInstance();
                            Class[] clazzs = new Class[]{HttpServletRequest.class,HttpServletResponse.class,Object.class};
                            Method method = object.getClass().getMethod("interceptor",clazzs);
                            Object[] params = new Object[]{httpServletRequest,httpServletResponse,handler};
                            method.invoke(object,params);
                        }
                    }
                }
            }
        }
    }
}
