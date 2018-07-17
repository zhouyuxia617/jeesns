
/**
 * Created by zchuanzhao on 2016/10/13.
 */

// sss jquery 系统在加载的时候就调用的Jqeruy的初始化函数,用来动态绑定form的验证,提交等.
$(function () {
    jeesns.submitForm();
    jeesns.jeesnsLink();
});



var jeesns = {
    // sss jquery 验证 用来定义from提交时候验证规则 ,用正则表达式来定义
    reg_rule : {
        'selected'   :    /.+/,
        'require'    :    /.+/,
        'email'      :    /^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/,
        'url'        :    /^http|https:\/\/[A-Za-z0-9]+\.[A-Za-z0-9]+[\/=\?%\-&_~`@[\]\':+!]*([^<>\"\"])*$/,
        'currency'   :    /^\d+(\.\d+)?$/,
        'number'     :    /^\d+$/,
        'zip'        :    /^\d{6}$/,
        'integer'    :    /^[-\+]?\d+$/,
        'double'     :    /^[-\+]?\d+(\.\d+)?$/,
        'letter'     :    /^[A-Za-z]+$/
    },
     /*
      * sss jquery 验证 用来定义当验证不通过时候提示信息
      */
    reg_rule_msg : {
        'selected'   :    "必须选择",
        'require'    :    "不能为空",
        'email'      :    "格式不正确",
        'url'        :    "格式不正确",
        'currency'   :    /^\d+(\.\d+)?$/,
        'number'     :    /^\d+$/,
        'zip'        :    /^\d{6}$/,
        'integer'    :    "必须为整数",
        'double'     :    "必须为数字",
        'letter'     :    "必须为字母"
    },

    getOptions : function(){
        var index;
        var options = {
            dataType : 'json',
            timeout : 20000,
            beforeSubmit : function (){
                $(":submit").attr("disabled","disabled");
                // form.find('.jeesns-submit').attr("disabled","disabled");
                index = jeesnsDialog.loading();
            },
            error:function(){
                jeesnsDialog.close(index);
                $(":submit").removeAttr("disabled");
                // form.find('.jeesns-submit').removeAttr("disabled");
                jeesnsDialog.tips('请求失败 ！');
            },
            success:function(res){
                jeesnsDialog.close(index);
                if(res.code==0){
                    $(":submit").removeAttr("disabled");
                    jeesnsDialog.successTips(res.message);
                }else if(res.code==1){
                    jeesnsDialog.successTips(res.message);
                    setTimeout(function(){
                        window.location.href=window.location.href;
                    },3000);
                }else if(res.code==2){
                    jeesnsDialog.successTips(res.message);
                    setTimeout(function(){
                        window.location.href=res.url;
                    },3000);
                }else if(res.code==3){
                    jeesnsDialog.successTips(res.message);
                    // var index = parent.layer.getFrameIndex(window.name); //获取窗口索引
                    // parent.layer.close(index);
                    setTimeout(function(){
                        parent.window.location.reload();
                    },3000);
                }else if(res.code==-1){
                    $(":submit").removeAttr("disabled");
                    jeesnsDialog.errorTips(res.message);
                }else{
                    $(":submit").removeAttr("disabled");
                    jeesnsDialog.tips(res.message);
                }
                // $(":submit").removeAttr("disabled");
                // form.find('.jeesns-submit').removeAttr("disabled");
            }
        };
        return options;
    },
    submitForm : function(form){
        if(arguments[0]){//如果传入了form，马上对form进行ajax提交
            var form = typeof(form)=='object' ? $(form) : $('#'+form);
            if(jeesns.checkForm(form)==false) return false;
            var options = getOptions();
            form.ajaxSubmit(options);
        }else{//否则，对标志有class="jeesns_form"的表单进行ajax提交的绑定操作
            $('.jeesns_form').bind('submit',function(){
                var form = $(this);
                if(jeesns.checkForm(form)==false) return false;
                var options = jeesns.getOptions();
                form.ajaxSubmit(options);
                return false;
            });
        }
    },
// sss jquery 通过data-type  data-rule真正用来处理表单提交时候的校验,出错了用placeholder+验证错误信息提示
    checkForm : function(form){
        var check = true;
        //sss jquery 遍历页面输入框元素
        form.find("input,textarea,select,redio,checkbox").each(function(){
            var val = $.trim($(this).val());
            //sss jquery 拿到这个元素的data-rule值
            var rule = $(this).attr('data-rule');
            //sss 拿出这个元素的data-type值,用来做数据校验的依据
            var type = $(this).attr('data-type');
            
            if(type != "" && type != undefined){
                var alt = $(this).attr('alt');
                if(alt == "" || alt == undefined){
                    alt = $(this).attr("placeholder");
                }
                if(alt == undefined){
                    alt = "";
                }
                if(rule != "" && rule != undefined){
                    if(rule.indexOf("equal") != -1){
                        var equalid = rule.substring(rule.indexOf("[")+1,rule.indexOf("]"));
                        var equalValue = $("#"+equalid).val();
                        if(val != equalValue){
                            jeesnsDialog.errorTips(alt);
                            $(this).focus();
                            check = false;
                            return false;
                        }
                    }
                }

                if(type.indexOf(",") != -1){
                    var types = type.split(",");
                    for (var i=0;i<types.length;i++){
                        type = types[i];
                        if(!jeesns.reg_rule[type].test(val)){
                            jeesnsDialog.errorTips(alt+jeesns.reg_rule_msg[type]);
                            $(this).focus();
                            check = false;
                            return false;
                        }
                    }
                }else{
                    if(!jeesns.reg_rule[type].test(val)){
                        jeesnsDialog.errorTips(alt+jeesns.reg_rule_msg[type]);
                        $(this).focus();
                        check = false;
                        return false;
                    }
                }
            }
        });
        return check;
    },

    jeesnsLink : function (){
        $('a[target="_jeesnsLink"]').on('click',function() {
            var url = $(this).attr('href');
            var title = $(this).attr('confirm');
            if (title) {
                jeesnsDialog.confirm(title, function () {
                    jeesns.jeesnsAjax(url,"GET",null);
                });
            }else {
                jeesns.jeesnsAjax(url,"GET",null);
            }
            return false;
        });

        $('a[target="_jeesnsOpen"]').on('click',function() {
            var url = $(this).attr('href');
            var title = $(this).attr('title');
            var width = $(this).attr('width');
            var height = $(this).attr('height');
            if(width == undefined || width == ""){
                width = "500px";
            }
            if(height == undefined || height == ""){
                height = "300px";
            }
            jeesnsDialog.open(url,title,width,height);
            return false;
        });
    },

    jeesnsAjax : function(url,type,data){
        var index;
        //sss jquery jquery中的最复杂的调用ajax的方式, 还有两种方式是$.get() 和$.post()
        $.ajax({
            url: url,
            type: type,
            data: data,
            cache: false,
            dataType: "json",
            timeout: 20000,
            beforeSend: function(){
                index = jeesnsDialog.loading();
            },
            error: function(){
                jeesnsDialog.close(index);
                jeesnsDialog.errorTips("请求失败")
            },
            success:function(res){
                jeesnsDialog.close(index);
                if(res.code == 0){
                    jeesnsDialog.successTips(res.message);
                }else if(res.code == -1){
                    jeesnsDialog.errorTips(res.message)
                }else if(res.code==1){
                    jeesnsDialog.successTips(res.message);
                    setTimeout(function(){
                        window.location.href=window.location.href;
                    },2000);
                }else if(res.code==2){
                    jeesnsDialog.successTips(res.message);
                    setTimeout(function(){
                        window.location.href=res.url;
                    },2000);
                }else{
                    jeesnsDialog.tips(res.message);
                }
            }
        });
    }
};

var jeesnsDialog = {
    loading : function () {
        //加载层
        return parent.layer.load(0);
    },

    close : function (index) {
        parent.layer.close(index);
    },

    closeAll : function () {
        parent.layer.closeAll();
    },

    alert : function(msg) {
        parent.layer.alert(msg);
    },

    confirm : function(msg,confirmFun) {
        parent.layer.confirm(msg, function(){
            confirmFun();
        }, function(){

        });
    },

    tips : function(msg){
        parent.layer.msg(msg);
    },

    tips : function(msg,type){
        if(type == "error"){
            parent.layer.msg(msg, {icon: 5});
        }else if(type == "success"){
            parent.layer.msg(msg, {icon: 6});
        }else{
            parent.layer.msg(msg);
        }
    },

    errorTips : function(msg) {
        jeesnsDialog.tips(msg,"error");
    },

    successTips : function(msg) {
        jeesnsDialog.tips(msg, "success");
    },

    open : function (url,title,width,height) {
        layer.open({
            title: title,
            type: 2,
            area: [width,height],
            fix: true,
            maxmin: false,
            content: url,
            scrollbar: false
            // cancel: function(){
            //     window.location.href = window.location.href;
            // }
        });
    }
};


