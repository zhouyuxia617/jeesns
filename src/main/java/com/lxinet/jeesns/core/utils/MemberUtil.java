package com.lxinet.jeesns.core.utils;

import com.lxinet.jeesns.modules.mem.entity.Member;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by zchuanzhao on 16/9/29.
 */
public class MemberUtil {

    public static Member getLoginMember(HttpServletRequest request){
        Member loginMember = (Member) request.getSession().getAttribute(Const.SESSION_MEMBER);
        return loginMember;
    }

    public static void setLoginMember(HttpServletRequest request,Member member){
        request.getSession().setAttribute(Const.SESSION_MEMBER,member);
    }
}
