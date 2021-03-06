package com.lxinet.jeesns.modules.mem.service.impl;

import com.lxinet.jeesns.core.dto.ResponseModel;
import com.lxinet.jeesns.core.entity.Page;
import com.lxinet.jeesns.core.interceptor.PageInterceptor;
import com.lxinet.jeesns.core.utils.*;
import com.lxinet.jeesns.modules.cms.service.IArticleService;
import com.lxinet.jeesns.modules.group.dao.IGroupTopicDao;
import com.lxinet.jeesns.modules.group.service.IGroupFansService;
import com.lxinet.jeesns.modules.group.service.IGroupService;
import com.lxinet.jeesns.modules.group.service.IGroupTopicService;
import com.lxinet.jeesns.modules.mem.dao.IMemberDao;
import com.lxinet.jeesns.modules.mem.dao.IMemberFansDao;
import com.lxinet.jeesns.modules.mem.entity.Member;
import com.lxinet.jeesns.modules.mem.entity.ValidateCode;
import com.lxinet.jeesns.modules.mem.service.IMemberFansService;
import com.lxinet.jeesns.modules.mem.service.IMemberService;
import com.lxinet.jeesns.modules.mem.service.IValidateCodeService;
import com.lxinet.jeesns.modules.sys.service.IActionLogService;
import com.lxinet.jeesns.modules.sys.service.IConfigService;
import com.lxinet.jeesns.modules.weibo.entity.Weibo;
import com.lxinet.jeesns.modules.weibo.service.IWeiboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;
import java.util.Map;


/**
 * Created by zchuanzhao on 16/9/29.
 */
@Service("memberService")
public class MemberServiceImpl implements IMemberService {
	//sss spring Resource注解是按照名称注入，Autowired是按照类型注入，一般是按照名称注入的比较多
    @Resource
    private IMemberDao memberDao;
    @Resource
    private IValidateCodeService validateCodeService;
    @Resource
    private IConfigService configService;
    @Resource
    private IActionLogService actionLogService;
    @Resource
    private IMemberFansService memberFansService;
    @Resource
    private IArticleService articleService;
    @Resource
    private IGroupTopicService groupTopicService;
    @Resource
    private IGroupFansService groupFansService;
    @Resource
    private IWeiboService weiboService;

    @Override
    public ResponseModel login(Member member, HttpServletRequest request) {
        Map<String,String> config = configService.getConfigToMap();
        if("0".equals(config.get(ConfigUtil.MEMBER_LOGIN_OPEN))){
            return new ResponseModel(-1,"登录功能已关闭");
        }
        String password = member.getPassword();
        member.setPassword(Md5.getMD5Code(member.getPassword()));
        Member findMember = memberDao.login(member);
        if(findMember != null){
            if(findMember.getStatus() == -1){
                return new ResponseModel(-1,"该账户已被禁用");
            }
            //登录成功更新状态
            memberDao.loginSuccess(findMember.getId(),IpUtils.getIpAddress(request));
            findMember = this.findById(findMember.getId());
            MemberUtil.setLoginMember(request,findMember);
            actionLogService.save(findMember.getCurrLoginIp(),findMember.getId(),ActionUtil.MEMBER_LOGIN);
            return new ResponseModel(2,"登录成功",request.getServletContext().getContextPath()+"/member/");
        }
        actionLogService.save(IpUtils.getIpAddress(request),0,ActionUtil.MEMBER_LOGIN_ERROR,"登录用户名："+member.getName()+"，登录密码："+password);
        return new ResponseModel(-1,"用户名或密码错误");
    }

    @Override
    public Member manageLogin(Member member,HttpServletRequest request) {
        String password = member.getPassword();
        member.setPassword(Md5.getMD5Code(member.getPassword()));
        Member findMember = memberDao.manageLogin(member);
        if(findMember != null){
            //登录成功更新状态
            memberDao.loginSuccess(findMember.getId(),IpUtils.getIpAddress(request));
            findMember = this.findById(findMember.getId());
        }else {
            actionLogService.save(IpUtils.getIpAddress(request),0,ActionUtil.MEMBER_LOGIN_ERROR,"登录用户名："+member.getName()+"，登录密码："+password);
        }
        return findMember;
    }

    @Override
    public Member findById(int id) {
        return memberDao.findById(id);
    }

    @Override
    public ResponseModel register(Member member, HttpServletRequest request) {
        if(memberDao.findByName(member.getName()) != null){
            return new ResponseModel(-1,"该用户名已被注册");
        }
        if(memberDao.findByEmail(member.getEmail()) != null){
            return new ResponseModel(-1,"该邮箱已被注册");
        }
        member.setRegip(IpUtils.getIpAddress(request));
        member.setPassword(Md5.getMD5Code(member.getPassword()));
        member.setAvatar(Const.DEFAULT_AVATAR);
        if(memberDao.register(member) == 1){
            actionLogService.save(member.getRegip(),member.getId(),ActionUtil.MEMBER_REG);
            return new ResponseModel(2,"注册成功",request.getServletContext().getContextPath()+"/member/login");
        }
        return new ResponseModel(-1,"注册失败");
    }

    @Override
    public ResponseModel update(Member member) {
        if(memberDao.update(member) == 1){
            return new ResponseModel(3,"更新成功");
        }
        return new ResponseModel(-1,"更新失败");
    }

    @Override
    public ResponseModel delete(int id) {
        if(memberDao.delete(id) == 1){
            return new ResponseModel(1,"删除成功");
        }
        return new ResponseModel(-1,"删除失败");
    }

    @Override
    public ResponseModel<Member> listByPage(Page page, String key) {
        if (StringUtils.isNotBlank(key)){
            key = "%"+key.trim()+"%";
        }
        List<Member> list = memberDao.listByPage(page, key);
        ResponseModel model = new ResponseModel(0,page);
        model.setData(list);
        return model;
    }

    /**
     * 会员启用、禁用操作
     * @param id
     * @return
     */
    @Override
    public ResponseModel isenable(int id) {
        if(memberDao.isenable(id) == 1){
            return new ResponseModel(1,"操作成功");
        }
        return new ResponseModel(-1,"操作失败");
    }

    /**
     * 后台修改密码
     * @param id
     * @param password
     * @return
     */
    @Override
    public ResponseModel changepwd(Member loginMember, int id, String password) {
        if(StringUtils.isBlank(password)){
            return new ResponseModel(-1,"密码不能为空");
        }
        if(password.length() < 6){
            return new ResponseModel(-1,"密码不能少于6个字符");
        }
        password = Md5.getMD5Code(password);
        if(memberDao.changepwd(id,password) == 1){
            actionLogService.save(loginMember.getCurrLoginIp(),loginMember.getId(),ActionUtil.CHANGE_PWD);
            return new ResponseModel(3,"密码修改成功");
        }
        return new ResponseModel(-1,"密码修改失败");
    }

    /**
     * 会员修改密码
     * @param loginMember
     * @param oldPassword
     * @param newPassword
     * @return
     */
    @Override
    public ResponseModel changepwd(Member loginMember, String oldPassword, String newPassword) {
        if(StringUtils.isBlank(newPassword)){
            return new ResponseModel(-1,"密码不能为空");
        }
        if(newPassword.length() < 6){
            return new ResponseModel(-1,"密码不能少于6个字符");
        }
        oldPassword = Md5.getMD5Code(oldPassword);
        Member member = memberDao.findById(loginMember.getId());
        if(!oldPassword.equals(member.getPassword())){
            return new ResponseModel(-1,"旧密码错误");
        }
        return this.changepwd(loginMember,member.getId(),newPassword);
    }

    /**
     * 修改头像
     * @param member
     * @param oldAvatar 旧头像
     * @param request
     * @return
     */
    @Override
    public ResponseModel updateAvatar(Member member,String oldAvatar,HttpServletRequest request) {
        int result = memberDao.updateAvatar(member.getId(),member.getAvatar());
        if(result == 1){
            if(StringUtils.isNotEmpty(oldAvatar) && !Const.DEFAULT_AVATAR.equals(oldAvatar)){
                //头像真实路径
                String realPath = request.getServletContext().getRealPath(oldAvatar);
                //删除旧头像
                File file = new File(realPath);
                if(file.exists()){
                    file.delete();
                }
            }
            return new ResponseModel(0,"头像修改成功");
        }
        return new ResponseModel(-1,"头像修改失败，请重试");
    }

    /**
     * 修改会员基本信息
     * @param member 登录会员
     * @param name  昵称
     * @param sex  性别
     * @param introduce  个人说明
     * @return
     */
    @Override
    public ResponseModel editBaseInfo(Member member,String name,String sex,String introduce) {
        if (name != null && !name.equals(member.getName())){
            if(this.findByName(name) != null){
                return new ResponseModel(-1,"昵称已被占用，请更换一个");
            }
        }
        member.setName(name);
        member.setSex(sex);
        member.setIntroduce(introduce);
        if(memberDao.editBaseInfo(member) == 1){
            return new ResponseModel(0,"修改成功");
        }
        return new ResponseModel(-1,"修改失败");
    }

    /**
     * 修改会员其他信息
     * @param loginMember 登录会员
     * @param birthday
     * @param qq
     * @param wechat
     * @param contactPhone
     * @param contactEmail
     * @param website
     * @return
     */
    @Override
    public ResponseModel editOtherInfo(Member loginMember,String birthday,String qq,String wechat,String contactPhone,
                                       String contactEmail,String website) {
        loginMember.setBirthday(birthday);
        loginMember.setQq(qq);
        loginMember.setWechat(wechat);
        loginMember.setContactPhone(contactPhone);
        loginMember.setContactEmail(contactEmail);
        loginMember.setWebsite(website);
        if(memberDao.editOtherInfo(loginMember) == 1){
            return new ResponseModel(0,"修改成功");
        }
        return new ResponseModel(-1,"修改失败");
    }

    @Override
    public Member findByName(String name) {
        return memberDao.findByName(name);
    }

    @Override
    public ResponseModel sendEmailActiveValidCode(Member loginMember) {
        loginMember = this.findById(loginMember.getId());
        if(loginMember.getIsActive() == 1){
            return new ResponseModel(-1,"您的账号已经激活，无需重复激活");
        }
        String randomCode = RandomCodeUtil.randomCode6();
        ValidateCode validateCode = new ValidateCode(loginMember.getEmail(),randomCode,2);
        if(validateCodeService.save(validateCode)){
            if(EmailSenderUtils.activeMember(loginMember.getEmail(),randomCode)){
                return new ResponseModel(0,"邮件发送成功");
            }
        }
        return new ResponseModel(-1,"邮件发送失败，请重试");
    }

    @Transactional
    @Override
    public ResponseModel active(Member loginMember, String randomCode, HttpServletRequest request) {
        try {
            loginMember = this.findById(loginMember.getId());
            if(loginMember.getIsActive() == 1){
                return new ResponseModel(-1,"您的账号已经激活，无需重复激活");
            }
            ValidateCode validateCode = validateCodeService.valid(loginMember.getEmail(),randomCode,2);
            if(validateCode == null){
                return new ResponseModel(-1,"验证码错误");
            }

            if(validateCodeService.used(validateCode.getId())){
                if(memberDao.active(loginMember.getId()) == 1){
                    loginMember.setIsActive(1);
                    MemberUtil.setLoginMember(request,loginMember);
                    return new ResponseModel(2,"激活成功，正在进入会员中心...",request.getContextPath()+"/member/");
                }
            }
            return new ResponseModel(-1,"激活失败，请重试");
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseModel(-1,"激活失败，请重试");
        }
    }

    @Override
    public Member findByNameAndEmail(String name, String email) {
        return memberDao.findByNameAndEmail(name,email);
    }

    @Override
    public ResponseModel forgetpwd(String name, String email) {
        Member member = this.findByNameAndEmail(name,email);
        if(member == null){
            return new ResponseModel(-1,"会员不存在");
        }
        String randomCode = RandomCodeUtil.uuid();
        ValidateCode validateCode = new ValidateCode(email,randomCode,1);
        if(validateCodeService.save(validateCode)){
            if(EmailSenderUtils.forgetpwd(email,randomCode)){
                return new ResponseModel(0,"邮件发送成功");
            }
        }
        return new ResponseModel(-1,"邮件发送失败，请重试");
    }

    @Transactional
    @Override
    public ResponseModel resetpwd(String email,String token,String password, HttpServletRequest request) {
        Member member = memberDao.findByEmail(email);
        if(member == null){
            return new ResponseModel(-1,"会员不存在");
        }
        ValidateCode validateCode = validateCodeService.valid(email,token,1);
        if(validateCode == null){
            return new ResponseModel(-1,"验证码错误");
        }
        password = Md5.getMD5Code(password);
        if(memberDao.changepwd(member.getId(),password) == 1){
            validateCodeService.used(validateCode.getId());
            actionLogService.save(IpUtils.getIpAddress(request),member.getId(),ActionUtil.FIND_PWD);
            return new ResponseModel(2,"密码重置成功",request.getContextPath()+"/member/login");
        }
        return new ResponseModel(-1,"密码重置失败");
    }

    @Transactional
    @Override
    public ResponseModel follows(Member loginMember, Integer followWhoId) {
        if(loginMember == null){
            return new ResponseModel(-1,"请先登录");
        }
        if(this.findById(followWhoId) == null){
            return new ResponseModel(-1,"关注的会员不存在");
        }
        if(loginMember.getId().intValue() == followWhoId.intValue()){
            return new ResponseModel(-1,"不能关注自己");
        }
        if(memberFansService.find(loginMember.getId(),followWhoId) == null){
            //关注
            memberFansService.save(loginMember.getId(),followWhoId);
            memberDao.follows(loginMember.getId());
            memberDao.fans(followWhoId);
            return new ResponseModel(1,"关注成功");
        }else {
            //取消关注
            memberFansService.delete(loginMember.getId(),followWhoId);
            memberDao.follows(loginMember.getId());
            memberDao.fans(followWhoId);
            return new ResponseModel(0,"取消关注成功");
        }
    }

    @Override
    public ResponseModel isFollowed(Member loginMember, Integer followWhoId) {
        int loginMemberId = 0;
        if(loginMember != null){
            loginMemberId = loginMember.getId().intValue();
        }
        if(memberFansService.find(loginMemberId,followWhoId) == null){
            return new ResponseModel(0,"未关注");
        }else {
            return new ResponseModel(1,"已关注");
        }
    }


    /**
     * 会员主页
     * @param loginMember 登录会员
     * @param page 分页信息
     * @param memberId 被查看的会员ID
     * @param type 类型
     * @return
     */
    @Override
    public ResponseModel home(Member loginMember, Page page, Integer memberId,String type) {
        int loginMemberId = 0;
        if(loginMember != null){
            loginMemberId = loginMember.getId().intValue();
        }
        if("article".equals(type)){
            return articleService.listByPage(page,"",0,1, memberId);
        } else if("groupTopic".equals(type)){
            return groupTopicService.listByPage(page,"",0,1, memberId);
        } else if("group".equals(type)){
            return groupFansService.listByMember(page, memberId);
        } else if("weibo".equals(type)){
            return weiboService.listByPage(page,memberId,loginMemberId,"");
        } else if("follows".equals(type)){
            return memberFansService.followsList(page,memberId);
        } else if("fans".equals(type)){
            return memberFansService.fansList(page,memberId);
        }
        return new ResponseModel(-1);
    }

	@Override
	public ResponseModel etregister(Member member, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Member> userList() {

		return memberDao.allList();
	}
}
