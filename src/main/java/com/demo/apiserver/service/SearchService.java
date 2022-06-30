package com.demo.apiserver.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.demo.apiserver.Constants;
import com.demo.apiserver.ElasticsearchConfiguration;

@Service
public class SearchService extends CommonService {

	static final Logger logger = LoggerFactory.getLogger(SearchService.class);
	
	@Autowired
	ElasticsearchConfiguration esConfig;
	
	@Value("${spring.data.elasticsearch.index}")
	String index;
	@Value("${spring.data.elasticsearch.type}")
	String type;
	

	public ArrayList<Map<String, Object>> getSearchList(Map<String, Object> param) {
		
		String searchStr = (String)param.get("searchStr");
		int page = Integer.parseInt(param.get("page").toString());
		ArrayList<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		
		BoolQueryBuilder boolQuery = searchCondition(searchStr);
		
		RestHighLevelClient client = esConfig.getRestClient();
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
			.query(boolQuery)
			.from(page * scrollPagesize)
			.size(scrollPagesize)
			.sort("@timestamp", SortOrder.DESC)
			.fetchSource(Constants.SEARCH_FIELDS, Constants.EXCLUDE_FIELDS);

		SearchRequest searchRequest = new SearchRequest(index + "*").types(type).source(searchSourceBuilder);
		SearchResponse searchResponse = new SearchResponse();
		try {
			searchResponse = client.search(searchRequest);
		} catch(Exception e) {
			logger.info(e.getMessage());
		}
		
		for(SearchHit hit : searchResponse.getHits()) {
			
			Map<String, Object> m = new HashMap<String, Object>();
			
			m = hit.getSourceAsMap();
			m.put("_id", hit.getId());
			
			Map<String, HighlightField> highlightFields = hit.getHighlightFields();
			for (String field : Constants.SEARCH_FIELDS) {
				HighlightField highlightContentsField = highlightFields.get(field);
				if (highlightContentsField != null && highlightContentsField.fragments() != null && field.equals("agentName") && field.equals("agentDeptName")) {
	                m.put(field, highlightContentsField.fragments()[0].string());
	            }
			}
			
			for (Entry<String, DocumentField> field : hit.getFields().entrySet()) {
				if (field.getKey().equals("contents") && field.getValue().getValues().size() > 0) {
					m.put("contents", field.getValue().getValues().get(0));
				}
			}
			
			resultList.add(m);
		}
		return resultList;
	}
	
}
