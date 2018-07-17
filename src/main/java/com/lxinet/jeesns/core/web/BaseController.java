package com.lxinet.jeesns.core.web;

import com.lxinet.jeesns.core.utils.Const;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Controller基类
 * Created by zchuanzhao on 2016/11/26.
 */
public class BaseController {
	//ggg 在这里request是要传入到Page对象里面自动拿到分页的第几页，和总页数，方便读取但是增加了耦合度
    @Resource
    protected HttpServletRequest request;
    @Resource
    protected HttpServletResponse response;


    protected String getErrorMessages(BindingResult result) {
        StringBuffer errorMessages = new StringBuffer();
        List<FieldError> list = result.getFieldErrors();
        int count = 0;
        for (FieldError error : list) {
            errorMessages.append(error.getDefaultMessage());
            count ++;
            if(count < list.size()){
                errorMessages.append("<br/>");
            }
        }
        return errorMessages.toString();
    }

    protected String errorModel(Model model,String msg){
        model.addAttribute("msg",msg);
        return Const.MANAGE_ERROR_FTL_PATH;
    }

}
