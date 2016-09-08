package com.systex.cgbc.search.util;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * search请求的形式 client.prepareSearch("index1", "index2") 指定索引 .setTypes("type1",
 * "type2") 指定索引的类型 .addFields() 指定要返回的字段
 * .setSearchType(SearchType.DFS_QUERY_THEN_FETCH) 定义搜索类类型
 * .setQuery(QueryBuilders.termQuery("multi", "test")) 查询语句（QueryBuilders
 * .setPostFilter(FilterBuilders.rangeFilter("age").from(12).to(18)) 过滤器
 * .setFrom(0).setSize(60).setExplain(true) 返回结果大小 ，from to .execute()
 * .actionGet();
 *
 * @author yukaiwan
 */
public class QueryAPI {

	private Client client;

	private Logger logger = LoggerFactory.getLogger(QueryAPI.class);

	/**
	 * 初始化生成client对象
	 */
	public void init() {
		client = new ClientAPI().getClient();
	}

	/**
	 * match查询
	 */
	public void matchSearch(String field, String value) {

		QueryBuilder qb = matchQuery(field, QueryParser.escape(value));
		this.parseResult(this.searchQueryBuilder(qb));
	}

	/**
	 * match查询
	 */
	public void matchSearch(String field, String value, String indexName,
		String indexType) {

		QueryBuilder qb = matchQuery(field, QueryParser.escape(value));
		this.parseResultModeTest(this.searchQueryBuilder(qb, indexName,
			indexType, 60));
	}

	/**
	 * match查询
	 */
	public SearchHits matchSearch(String field, String value, String indexName,
		String indexType, int size, float minScore) {
		QueryBuilder qb = matchQuery(field, QueryParser.escape(value));
		SearchHits hits = this.searchQueryBuilder(qb, indexName, indexType,
			size, minScore).getHits();
		return hits;
	}

	/**
	 * match查询
	 */
	public SearchHits matchSearch(String field, String value, String indexName,
		String indexType, int size) {
		QueryBuilder qb = matchQuery(field, QueryParser.escape(value));
		SearchHits hits = this.searchQueryBuilder(qb, indexName, indexType,
			size).getHits();
		return hits;
	}

	/**
	 * match查询
	 */
	public SearchHit[] matchSearch(String field, String value,
		String indexName, String indexType, int size, float minScore,
		String flag) {
		QueryBuilder qb = matchQuery(field, QueryParser.escape(value));
		SearchHits hits = this.searchQueryBuilder(qb, indexName, indexType,
			size, minScore).getHits();
		SearchHit[] searchHitArray = hits.getHits();
		return searchHitArray;
	}

	/**
	 * match查询
	 */
	public void matchSearchStr(String field, String value, String indexName,
		String indexType, int size) {

		QueryBuilder qb = matchQuery(field, QueryParser.escape(value));
		this.parseResultModeTest(this.searchQueryBuilder(qb, indexName,
			indexType, size));
	}

	/**
	 * @param field 查询字段
	 * @param value 查询值
	 */
	public void multiMatchSearch(String value, String[] field) {
		QueryBuilder qb = multiMatchQuery(QueryParser.escape(value), field);
		this.parseResultModeTest(this.searchQueryBuilder(qb));
	}

	/**
	 * term 查询
	 *
	 * @param term
	 * @param field
	 */
	public SearchHits termSearch(String term, String field, String indexName,
		String indexType, int size) {
		QueryBuilder queryBuilder = termsQuery(field, term);

		SearchHits hits = this.searchQueryBuilder2(queryBuilder, indexName, indexType,
				size).getHits();
		
		/*SearchResponse searchResponse = client.prepareSearch(indexName)
				.setTypes(indexType).setQuery(queryBuilder).setFrom(0)
				.setSize(size).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.execute().actionGet();
		return searchResponse.getHits();*/
		return hits;
	}

	/**
	 * term 查询
	 *
	 * @param term
	 * @param field
	 */
	public boolean termSearchBoolean(String term, String field,
		String indexName, String indexType, int size) {
		QueryBuilder queryBuilder = termsQuery(field, term);
		// .minimumMatch(1);
		return this.searchQueryBuilder(queryBuilder, indexName, indexType, size)
			.getHits().getTotalHits() > 0;
	}

	/**
	 * term 查询
	 *
	 * @param term
	 * @param field
	 */
	public SearchHits termSearch(String term, String field, String indexName,
		String indexType) {
		QueryBuilder queryBuilder = termsQuery(field, term);
		SearchResponse searchResponse = client.prepareSearch(indexName)
			.setTypes(indexType).setQuery(queryBuilder)
			.setSearchType(SearchType.DFS_QUERY_THEN_FETCH).execute()
			.actionGet();
		return searchResponse.getHits();
	}

	/**
	 * @param term
	 * @param field
	 * @param indexName
	 * @param indexType
	 * @param size
	 */
	public void wildcardSearch(String term, String field, String indexName,
		String indexType, int size) {
		String wildcardValue = "*" + term + "*";
		QueryBuilder queryBuilder = wildcardQuery(field, wildcardValue);
		this.parseResultModeTest(this.searchQueryBuilder(queryBuilder,
			indexName, indexType, size));
	}

	/**
	 * 类似于批量索引的操作，一次执行多个查询请求
	 */
	public void multiSearch() {

		SearchRequestBuilder searchRequestBuilder1 = client.prepareSearch()
			.setQuery(QueryBuilders.matchQuery("CADD1", "深圳")).setSize(1);
		SearchRequestBuilder searchRequestBuilder2 = client.prepareSearch()
			.setQuery(QueryBuilders.matchQuery("CADD1", "梅州")).setSize(1);

		MultiSearchResponse multiSearchResponses = client.prepareMultiSearch()
			.add(searchRequestBuilder1).add(searchRequestBuilder2)
			.execute().actionGet();
		// 返回结果包含多个查询请求的响应结果
		for (MultiSearchResponse.Item item : multiSearchResponses
			.getResponses()) {
			this.parseResult(item.getResponse());
		}
	}

	/**
	 * 前缀查询
	 *
	 * @param field 查询字段
	 * @param value 查询值
	 */
	public void prefixSearch(String field, String value, String indexName,
		String indexType, int size) {
		QueryBuilder qb = prefixQuery(field, QueryParser.escape(value));
		this.parseResultModeTest(this.searchQueryBuilder(qb, indexName,
			indexType, size));
	}

	/**
	 * 区间查询
	 *
	 * @param field 字段
	 * @param start 开始值
	 * @param end   结束值
	 */
	public void rangeSearch(String field, int start, int end, String indexName,
		String indexType, int size) {
		QueryBuilder qb = rangeQuery(field).from(start).to(end)
			.includeLower(true).includeUpper(false);
		this.parseResultModeTest(this.searchQueryBuilder(qb, indexName,
			indexType, size));
	}

	/**
	 * 模糊查询
	 *
	 * @param fields
	 * @param value
	 * @param maxQueryTersm
	 */
	public void fuzzySearch(String[] fields, String value, int maxQueryTersm,
		String indexName, String indexType, int size) {
		QueryBuilder qb = moreLikeThisQuery(fields).addLikeText(
			QueryParser.escape(value)).maxQueryTerms(maxQueryTersm);
		this.parseResultModeTest(this.searchQueryBuilder(qb, indexName,
			indexType, size));
	}

	/**
	 * @param fields
	 * @param value
	 * @param indexName
	 * @param indexType
	 * @param size
	 */
	public void queryString(String[] fields, String value, String indexName,
		String indexType, int size) {

		QueryBuilder qb = queryStringQuery(fields[0] + ":" + value);
		this.parseResultModeTest(this.searchQueryBuilder(qb, indexName,
			indexType, size));
	}

	/**
	 * 以 QueryBuilder为参的查询 主要是有些查询传参比较麻烦，直接在外部构造QueryBuilder然后查询更方便。
	 *
	 * @param queryBuilder
	 * @param index
	 * @param indexType
	 * @param size
	 */
	public void queryBuilderSearch(QueryBuilder queryBuilder, String index,
		String indexType, int size) {
		this.parseResultModeTest(this.searchQueryBuilder(queryBuilder, index,
			indexType, size));
	}

	/**
	 * 底层查询函数
	 *
	 * @param queryBuilder
	 */
	public SearchResponse searchQueryBuilder(QueryBuilder queryBuilder) {

		// SearchResponse response =
		// client.prepareSearch(config.getProperty("indexName"))
		SearchResponse response = client.prepareSearch("guangfa-10-20150619")
			.setTypes("test")
			.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			.setQuery(queryBuilder)
			// Query 8698127
			.setFrom(8698107).setSize(10).setExplain(true).execute()
			.actionGet();

		return response;
	}

	/**
	 * 底层查询函数
	 *
	 * @param queryBuilder
	 */
	public SearchResponse searchQueryBuilder(QueryBuilder queryBuilder,
		String indexName, String indexType, int size) {

		String[] fields = new String[] {"GDB_NBR", "MADD_CITY",
			"MADD_PROVINCE", "CADD1", "CADD2", "GLOCAL_NAME", "EXIGEN_DN",
			"EXIGEN_PHONE", "EXIGEN_MOBILE", "LINEAL_NAME", "LINEAL_DN",
			"LINEAL_PHONE", "LINEAL_EXTEN", "LINEAL_MOBILE",
			"ADD_PROVINCE", "ADD_CITY", "ADDR1", "ADDR2", "ADDR3",
			"GEMP_NAME", "GADD_PROVINCE", "GADD_CITY", "GEMP_ADDR1",
			"GEMP_DN", "GEMP_PHONE", "GEMP_EXTEN", "MADD_DN", "MADD_PHONE",
			"MADD_EXTEN", "LS_DN", "LITTLE_SMART", "MOBILE_NBR", "ID_TYPE",
			"ID_NBR", "EMAIL_ADDR", "FLAG", "CENSUS_PROVINCE",
			"CENSUS_CITY", "CENSUS_ADDR", "NATIVE_PHONE", "NOMINATE_PHONE",
			"NOMINATE_NAME", "LINEAL_RELATION", "FORMAT_ID", "TLBMLW",
			"BRANCH_NBR", "BRANCH_NO", "STAFF_ID", "DATA_TP_CODE"};

		SearchRequestBuilder searchRequestBuilder = client
			.prepareSearch(indexName).setTypes(indexType).addFields(fields)
			// .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			.setQuery(queryBuilder).setFrom(0).setSize(size)
			.setExplain(true);

		for (String field : fields) {
			searchRequestBuilder.addHighlightedField(field, 70, 1);
		}

		SearchResponse response = searchRequestBuilder
			.setHighlighterPreTags("<span class='highFiled'>")
			.setHighlighterPostTags("</span>")
			.execute().actionGet();
		return response;
	}

	/**
	 * 底层查询函数
	 *
	 * @param queryBuilder
	 */
	public SearchResponse searchQueryBuilder(QueryBuilder queryBuilder,
		String indexName, String indexType, int size, float minScore) {

		String[] fields = new String[] {"GDB_NBR", "MADD_CITY",
			"MADD_PROVINCE", "CADD1", "CADD2", "GLOCAL_NAME", "EXIGEN_DN",
			"EXIGEN_PHONE", "EXIGEN_MOBILE", "LINEAL_NAME", "LINEAL_DN",
			"LINEAL_PHONE", "LINEAL_EXTEN", "LINEAL_MOBILE",
			"ADD_PROVINCE", "ADD_CITY", "ADDR1", "ADDR2", "ADDR3",
			"GEMP_NAME", "GADD_PROVINCE", "GADD_CITY", "GEMP_ADDR1",
			"GEMP_DN", "GEMP_PHONE", "GEMP_EXTEN", "MADD_DN", "MADD_PHONE",
			"MADD_EXTEN", "LS_DN", "LITTLE_SMART", "MOBILE_NBR", "ID_TYPE",
			"ID_NBR", "EMAIL_ADDR", "FLAG", "CENSUS_PROVINCE",
			"CENSUS_CITY", "CENSUS_ADDR", "NATIVE_PHONE", "NOMINATE_PHONE",
			"NOMINATE_NAME", "LINEAL_RELATION", "FORMAT_ID", "TLBMLW",
			"BRANCH_NBR", "BRANCH_NO", "STAFF_ID", "DATA_TP_CODE"};

		SearchRequestBuilder searchRequestBuilder = client
			.prepareSearch(indexName).setTypes(indexType).addFields(fields)
			// .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			.setQuery(queryBuilder).setFrom(0).setSize(size)
			.setExplain(true);

		for (String field : fields) {
			searchRequestBuilder.addHighlightedField(field, 70, 1);
		}

		SearchResponse response = searchRequestBuilder.setMinScore(minScore)
			.setHighlighterPreTags("<span class='highFiled'>")
			.setHighlighterPostTags("</span>")
			.execute().actionGet();
		return response;
	}

	/**
	 * 底层查询函数
	 *
	 * @param queryBuilder
	 */
	public SearchResponse searchQueryBuilder2(QueryBuilder queryBuilder,
		String indexName, String indexType, int size) {

		String[] fields = new String[] {"GDB_NBR", "MADD_CITY",
			"MADD_PROVINCE", "CADD1", "CADD2", "GLOCAL_NAME", "EXIGEN_DN",
			"EXIGEN_PHONE", "EXIGEN_MOBILE", "LINEAL_NAME", "LINEAL_DN",
			"LINEAL_PHONE", "LINEAL_EXTEN", "LINEAL_MOBILE",
			"ADD_PROVINCE", "ADD_CITY", "ADDR1", "ADDR2", "ADDR3",
			"GEMP_NAME", "GADD_PROVINCE", "GADD_CITY", "GEMP_ADDR1",
			"GEMP_DN", "GEMP_PHONE", "GEMP_EXTEN", "MADD_DN", "MADD_PHONE",
			"MADD_EXTEN", "LS_DN", "LITTLE_SMART", "MOBILE_NBR", "ID_TYPE",
			"ID_NBR", "EMAIL_ADDR", "FLAG", "CENSUS_PROVINCE",
			"CENSUS_CITY", "CENSUS_ADDR", "NATIVE_PHONE", "NOMINATE_PHONE",
			"NOMINATE_NAME", "LINEAL_RELATION", "FORMAT_ID", "TLBMLW",
			"BRANCH_NBR", "BRANCH_NO", "STAFF_ID", "DATA_TP_CODE"};

		SearchRequestBuilder searchRequestBuilder = client
			.prepareSearch(indexName).setTypes(indexType).addFields(fields)
			// .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			.setQuery(queryBuilder).setFrom(0).setSize(size)
			.setExplain(true);

		for (String field : fields) {
			searchRequestBuilder.addHighlightedField(field, 70, 1);
		}

		SearchResponse response = searchRequestBuilder
			.setHighlighterPreTags("<span class='highFiled'>")
			.setHighlighterPostTags("</span>")
			.execute().actionGet();
		return response;
	}

	/**
	 * 底层查询函数
	 *
	 * @param queryBuilder
	 */
	public SearchResponse searchQueryBuilder(QueryBuilder queryBuilder,
		String[] indexName, String indexType, int size) {

      //long startTime = System.currentTimeMillis();

		SearchResponse response = client.prepareSearch(indexName)
			.setTypes(indexType)
			.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			.setQuery(queryBuilder)
			// Query
			.setFrom(0).setSize(size).setExplain(true).execute()
			.actionGet();
      //long endTime = System.currentTimeMillis();
      return response;
	}

	/**
	 * 解析查询结果，简单获取匹配到的总记录数
	 *
	 * @param response
	 */
	public void parseResult(SearchResponse response) {
		SearchHits hits = response.getHits();
		System.out.println(hits.getTotalHits());
		for (int i = 0; i < hits.getHits().length; i++) {
			System.out.println(hits.getHits()[i].getSourceAsString());
		}
	}

	/**
	 * 解析查询结果，输出各项关键信息，用以支持索引查询测试
	 *
	 * @param response
	 */
	public void parseResultModeTest(SearchResponse response) {
		SearchHits hits = response.getHits();
		for (int i = 0; i < hits.getHits().length; i++) {
			// System.out.println("result:"+i+"  "+hits.getHits()[i].getSourceAsString()
			// + "	"+hits.getHits()[i].getScore());
		}
		// String testInfor =
		// "	totalHits:"+hits.getTotalHits()+"	Hits:"+hits.getHits().length;
		// FileUtil.write( testInfor);
		// hits.getHits()[0].field("").getValue();
	}

}
