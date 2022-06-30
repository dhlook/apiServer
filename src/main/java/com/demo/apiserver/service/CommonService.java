package com.demo.apiserver.service;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.demo.apiserver.Constants;

@Service
public class CommonService {
    private final Logger logger = LoggerFactory.getLogger(CommonService.class);
    protected int pagesize;
    protected int scrollPagesize = 50;
    
  public String getNgramQuery(String searchKeyword) {
		
		String str = "";
		int ngramSize = 16;
		String ngramStr = "";
		
		searchKeyword = searchKeyword.toLowerCase();

		if (searchKeyword.length() > ngramSize) {

			for (int i = 0; i <= searchKeyword.length() - ngramSize; i++) {
				ngramStr += "("+reformSearchWord(searchKeyword.substring(i, i + ngramSize)) + ")";
			}

			str = ngramStr;
		} else {
			str = reformSearchWord(searchKeyword);
		}
		
		return str;
    }
	
	public String reformSearchWord(String src) {

        //query_string 예약어 검색어로 인식하도록 예외처리
        // 예약어 + - = && || > < ! ( ) { } [ ] ^ " ~ * ? : \ /
        String esReservedRegExp = "[\\+\\-\\=\\>\\<\\!\\(\\)\\{\\}\\[\\]\\^\\~\\?\\/\\\\]|(?:&&)|(?:\\|\\|)";
        StringBuffer buffer = new StringBuffer();
        Pattern pattern = Pattern.compile(esReservedRegExp);
        Matcher m = pattern.matcher(src);

        while (m.find()) {
            String findStr = m.group();
            String replaceStr = "";
            if (StringUtils.isNotBlank(findStr)) {
                if(findStr.equals("\\")){
                    replaceStr = "\\\\\\"+findStr;
                } else {
                    replaceStr = "\\\\"+findStr;
                }

                m.appendReplacement(buffer, replaceStr);
            }
        }
        m.appendTail(buffer);
        
        return buffer.toString();
    }
	
	public Map<String, Object> searchWordString(String searchStr) {
		
		String nGramQuery = StringUtils.EMPTY;
		String whiteSpaceQuery = StringUtils.EMPTY;
		String searchWord = StringUtils.EMPTY;
		
		if (StringUtils.isNotBlank(searchStr)) {
			searchWord = searchStr.trim().toLowerCase();
			nGramQuery = searchWord;
			whiteSpaceQuery = searchWord;
			
			String tempStr = "";
			
			tempStr = replaceQueryStr(nGramQuery, 1);
			
			if (StringUtils.isNotEmpty(tempStr)) {
				nGramQuery = tempStr;
			}
			
			tempStr = replaceQueryStr(whiteSpaceQuery, 2);
			if (StringUtils.isNotEmpty(tempStr)) {
				whiteSpaceQuery = tempStr;
			}
			
			
			if (nGramQuery != null) {
				nGramQuery = new StringJoiner("", "+(", ")").add(nGramQuery).toString();
			}
			if (whiteSpaceQuery != null) {
				whiteSpaceQuery = new StringJoiner("", "+(", ")").add(whiteSpaceQuery).toString();
			}
		}
		
		Map<String, Object> queryMap = new HashMap<String, Object>();
		queryMap.put("nGramQuery", nGramQuery);
		queryMap.put("whiteSpaceQuery", whiteSpaceQuery);
		
		if (nGramQuery == null) {
            queryMap = Collections.emptyMap();
        }
		
		return queryMap;
	}
    

	public String replaceQueryStr(String searchWord, int analyzerType) {

	    String tempStr = "";
	    int ngramSize = 16;
	
	    // 괄호 앞뒤 공백 추가하여 단어만 추출
	    if (searchWord.contains("(")) {
	        searchWord = searchWord.replaceAll("\\(", " ( ");
	    }
	    if (searchWord.contains(")")) {
	        searchWord = searchWord.replaceAll("\\)", " ) ");
	    }
	
	    String[] strArr = searchWord.split(" ");
	
	    for (String str : strArr) {
	        if (!str.replaceAll("\\s", "").equals("")) {
	
	            // ngram index 설정인 16자 이상의 문자일 경우 한문자씩 증가하여 AND 조건으로 붙임
	            if (analyzerType == 1) {
	
	                String ngramStr = "";
	                if (str.length() > ngramSize) {
	
	                    for (int i = 0; i <= str.length() - ngramSize; i++) {
	                        ngramStr += reformSearchWord(str.substring(i, i + ngramSize)) + " AND ";
	                    }
	
	                    // 마지막 AND 조건 제거
	                    ngramStr = "(" + ngramStr.substring(0, ngramStr.lastIndexOf(" AND ")) + ")";
	
	                    str = ngramStr;
	                } else {
	                    str = reformSearchWord(str);
	                }
	
	            } else {
	                // whitespace일 경우 단어 뒤에 * 붙임
	                str = "*" + reformSearchWord(str) + "*";
	            }
	
	            // 각 단어 사이를 띄움
	            tempStr += str + " ";
	        }
	    }
	
	    if (StringUtils.isNotEmpty(tempStr)) {
	        tempStr = tempStr.substring(0, tempStr.length() - 1);
	    }
	
	    return tempStr;
	}
	
	public BoolQueryBuilder makeSearchBoolQuery(Map<String, Object> queryMap, BoolQueryBuilder searchBoolQuery) {
        String nGramQuery = (String) queryMap.get("nGramQuery");
        String whiteSpaceQuery = (String) queryMap.get("whiteSpaceQuery");

        BoolQueryBuilder boolSearchQuery = new BoolQueryBuilder();

        QueryStringQueryBuilder nGramQueryBuilder = queryStringQuery(nGramQuery).defaultOperator(Operator.OR);
        QueryStringQueryBuilder whiteSpaceQueryBuilder = queryStringQuery(whiteSpaceQuery).defaultOperator(Operator.OR);


        for (String field : Constants.NGRAM_FIELDS) {
            nGramQueryBuilder.field(field);
        }
        for (String field : Constants.WHITESPACE_FIELDS) {
            whiteSpaceQueryBuilder.field(field);
        }

        // Master BoolQuery
        boolSearchQuery.should(nGramQueryBuilder).should(whiteSpaceQueryBuilder).minimumShouldMatch(1);
        searchBoolQuery.must(boolSearchQuery);
        
        return searchBoolQuery;

    }
	
	public BoolQueryBuilder searchCondition(String searchStr) {
        BoolQueryBuilder searchBoolQuery = QueryBuilders.boolQuery();

        if (StringUtils.isNotBlank(searchStr)) {
            Map<String, Object> searchWordMap = searchWordString(searchStr);

            if (!searchWordMap.isEmpty()) {
                searchBoolQuery = makeSearchBoolQuery(searchWordMap, searchBoolQuery);
            }
        }

        return searchBoolQuery;
    }
	
}
