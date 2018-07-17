package com.lxinet.jeesns.modules.mem.service;

import java.util.List;

import com.lxinet.jeesns.core.entity.User;

public interface UserService {
	
	boolean deletUser(int id);
	
	boolean updateUser(User user);
	
	List <User>userlist();
	
	boolean addUser(User user);
}
