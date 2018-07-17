package com.lxinet.jeesns.modules.mem.web.index;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.lxinet.jeesns.core.entity.User;
import com.lxinet.jeesns.modules.mem.service.UserService;

@Controller
@RequestMapping("/user")
public class userController {
	
	@Autowired
	UserService userservice;
	
	@RequestMapping("/add")
	public String add() {
		return "/member/add";
	}
	
	@RequestMapping("/update")
	public String update(Integer id ,ModelMap modelmap) {
		modelmap.put("userId", id);
		return "/member/update";
	}
	
	@RequestMapping("/addUser")
	public String addUser(User user) {
		System.out.println(user);
		userservice.addUser(user);
		return "/member/list";
	}
	
	@RequestMapping("/deletUser")
	public String deletUser(Integer id) {
		System.out.println(id);
		userservice.deletUser(id);
		return "/member/list";
	}
	
	@RequestMapping("/updateUser")
	public String updateUser(User user) {
		System.out.println(user);
		userservice.updateUser(user);
		return "/member/list";
	}
	
	@RequestMapping(value="/userlist",method=RequestMethod.GET)
	public String userlist(ModelMap modelmap) {
		System.out.println("aaaa");
		List<User>list=userservice.userlist();
		modelmap.put("userlist", list);
		return "/member/list";
	}
	
}
