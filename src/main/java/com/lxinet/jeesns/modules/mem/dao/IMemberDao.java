package com.lxinet.jeesns.modules.mem.dao;

import com.lxinet.jeesns.core.dao.IBaseDao;
import com.lxinet.jeesns.core.entity.Page;
import com.lxinet.jeesns.modules.mem.entity.Member;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会员DAO接口
 * Created by zchuanzhao on 16/9/26.
 */
public interface IMemberDao extends IBaseDao<Member> {

    Member login(Member member);

    Member manageLogin(Member member);

    List<Member> listByPage(@Param("page") Page page, @Param("key") String key);

    int register(Member member);

    /**
     * 会员启用禁用操作
     * @param id
     * @return
     */
    int isenable(@Param("id") Integer id);

    /**
     * 修改密码
     * @param id
     * @param password
     * @return
     */
    int changepwd(@Param("id") Integer id,@Param("password") String password);

    /**
     * 修改头像
     * @param id
     * @param avatar
     * @return
     */
    int updateAvatar(@Param("id") Integer id,@Param("avatar") String avatar);

    /**
     * 修改会员基本信息
     * @param member
     * @return
     */
    int editBaseInfo(Member member);

    /**
     * 修改会员其他信息
     * @param member
     * @return
     */
    int editOtherInfo(Member member);

    Member findByName(@Param("name") String name);

    Member findByEmail(@Param("email") String email);

    Member findByNameAndEmail(@Param("name") String name,@Param("email") String email);

    /**
     * 账号激活
     * @param id
     * @return
     */
    int active(@Param("id") Integer id);

    int loginSuccess(@Param("id") Integer id,@Param("currLoginIp") String currLoginIp);

    /**
     * 关注
     * @param id
     * @return
     */
    int follows(@Param("id") Integer id);

    /**
     * 粉丝
     * @param id
     * @return
     */
    int fans(@Param("id") Integer id);
}