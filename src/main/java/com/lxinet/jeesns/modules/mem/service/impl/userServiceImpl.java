package com.lxinet.jeesns.modules.mem.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lxinet.jeesns.core.dao.UserDao;
import com.lxinet.jeesns.core.entity.User;
import com.lxinet.jeesns.modules.mem.service.UserService;
@Service
public class userServiceImpl implements UserService{
	@Autowired
	UserDao userdao;
	
	@Override
	public boolean deletUser(int id) {
		
		return userdao.deletUser(id)>1;
	}

	@Override
	public boolean updateUser(User user) {
		
		return userdao.updateUser(user)>1;
	}

	@Override
	public List<User> userlist() {
		
		return userdao.userlist();
	}

	@Override
	public boolean addUser(User user) {
	
		return userdao.addUser(user)>1;
	}
	
}
