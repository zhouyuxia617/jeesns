package com.lxinet.jeesns.core.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.lxinet.jeesns.core.entity.User;
@Repository
public interface UserDao {
	int deletUser(int id);
	int updateUser(User user);
	List <User>userlist();
	int addUser(User user);
}
