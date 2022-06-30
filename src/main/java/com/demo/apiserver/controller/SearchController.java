package com.demo.apiserver.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.demo.apiserver.service.SearchService;

@Controller
public class SearchController {

	static final Logger logger = LoggerFactory.getLogger(SearchController.class);

	@Autowired
	SearchService searchService;
	
	@RequestMapping({"/api/search/list"})
	@ResponseBody
	ArrayList<Map<String, Object>> searchList(HttpServletRequest request, HttpServletResponse response) {
		
		Map<String, Object> params = new HashMap<String, Object>();
		String searchStr = request.getParameter("searchStr");
		String page = request.getParameter("page");
		params.put("searchStr", searchStr);
		params.put("page", page);
		
		return searchService.getSearchList(params);
		
	}
	
}
