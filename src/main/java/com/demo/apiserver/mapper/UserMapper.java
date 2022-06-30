package com.demo.apiserver.mapper;

import java.util.Map;

public interface UserMapper {

	public Map<String, String> getUser(String userId)throws Exception ;
	
}
