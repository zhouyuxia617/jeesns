package com.lxinet.jeesns.core.utils;

import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zchuanzhao on 2016/12/26.
 */
public class ErrorUtil {
    private static Map<Integer,String> errors = new HashMap<>();
    static {
        errors.put(-1000,"系统异常");
        errors.put(-1001,"没有权限");
        errors.put(-1002,"群组不存在");
        errors.put(-1003,"先关注群组才能发帖");
        errors.put(-1004,"帖子不存在");
        errors.put(-1005,"会员不存在");
        errors.put(-1006,"群组已关闭发帖功能");
        errors.put(-1007,"微博不存在");
    }

    public static String error(Model model, Integer errorcode, String ftlPath){
        model.addAttribute("msg",errors.get(errorcode));
        return ftlPath;
    }
}
