package com.h.domain.dto;


import java.io.Serializable;

/**
 * 用户登录DTO
 */
public class UserLoginDTO implements Serializable {

	private static final long serialVersionUID = 1L;


	private Long userId;
	/**
	 * 用户名
	 */
	private String userName;

	/**
	 * 密码
	 */
	private String password;

	/**
	 * 角色：0-普通用户、1-管理员
	 */
	private Integer role;

	/**
	 * 权限：执行器ID列表，多个逗号分割
	 */
	private String permission;




	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getRole() {
		return role;
	}

	public void setRole(Integer role) {
		this.role = role;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}
}
