package com.systex.cgbc.search.query.batch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.systex.cgbc.search.bean.ColResult;
import com.systex.cgbc.search.bean.Rule;
import com.systex.cgbc.search.bean.SResult;
import com.systex.cgbc.search.util.CgbcConstants;
import com.systex.cgbc.search.util.QueryAPI;
import com.systex.cgbc.search.util.ScoreUtil;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.highlight.HighlightField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleQueryThread implements Callable<String> {

    private static String indexName = CgbcConstants.indexName;
    private static String indexType = CgbcConstants.indexType;
    private static Logger logger = LoggerFactory.getLogger(SingleQueryThread.class);
    private JSONObject jsonObject;
    private QueryAPI queryAPI;
    private AtomicInteger precisyCount;
    private AtomicInteger fuzzyyCount;
    private AtomicInteger nonCount;
    private String _field = "_all";
    private List<Rule> rules;

    public SingleQueryThread(QueryAPI queryAPI, JSONObject jsonObject, List<Rule> rules,
        AtomicInteger precisyCount,
        AtomicInteger fuzzyyCount, AtomicInteger nonCount) {
        this.queryAPI = queryAPI;
        this.jsonObject = jsonObject;
        this.rules = rules;
        this.precisyCount = precisyCount;
        this.fuzzyyCount = fuzzyyCount;
        this.nonCount = nonCount;
    }

    public SearchHits fuzzySearch(String index, String type, String field, String value, int topN,
        float minScore) {
        SearchHits hits = queryAPI.matchSearch(_field, value, index, type, topN, minScore);
        return hits;
    }

    public SearchHits fuzzySearch(String index, String type, String field, String value, int topN) {
        SearchHits hits = queryAPI.matchSearch(_field, value, index, type, topN);
        return hits;
    }

    public SearchHits preciseSearch(String index, String type, String field, String value,
        int topN) {
        SearchHits hits = queryAPI.termSearch(value, _field, index, type, topN);
        return hits;
    }

    @SuppressWarnings("unchecked")
    public String call() throws Exception {
        List<SResult> searceResult = new ArrayList<SResult>();
        for (Rule rule : rules) {
            String queryField = rule.getColName();
            Byte patternType = rule.getPatternType();
            SResult result = new SResult();
            result.setQueryField(queryField);
            result.setPatternType(patternType);
            if (Byte.valueOf("1").equals(patternType)) {
                if (jsonObject.containsKey(queryField)) {
                    // 搜索的
                    String value = jsonObject.getString(queryField);
                    result.setQueryTarget(value);
                    int topN = rule.getTakeNum();
                    SearchHits searchHits =
                        preciseSearch(indexName, indexType, _field, value, topN);
                    result.setTotalHits(searchHits.getTotalHits());
                    SearchHit[] hitArray = searchHits.getHits();
                    if (hitArray.length > 0) {
                        precisyCount.incrementAndGet();
                    } else {
                        nonCount.incrementAndGet();
                    }
                    ArrayList<ColResult> hits = new ArrayList<ColResult>();

                    for (int i = 0; i < hitArray.length; i++) {
                        SearchHit searchHit = hitArray[i];
                        Map<String, Object> resMap = chgToHighLights(searchHit);
                        ColResult colResult = new ColResult();
                        colResult.setResult(resMap);
                        colResult.setScore(100.00);
                        hits.add(colResult);
                    }
                    result.setHits(hits);
                }
            } else if (Byte.valueOf("2").equals(patternType)) {
                int topN = rule.getTakeNum();
                float minScore = rule.getScore();
                if (jsonObject.containsKey(queryField)) {
                    String value = jsonObject.getString(queryField);
                    result.setQueryTarget(value);
                    SearchHits searchHits = null;
                    searchHits = fuzzySearch(indexName, indexType, _field, value, topN);
                    result.setTotalHits(searchHits.getTotalHits());
                    SearchHit[] hitArray = searchHits.getHits();
                    if (hitArray.length > 0) {
                        fuzzyyCount.incrementAndGet();
                    } else {
                        nonCount.incrementAndGet();
                    }
                    ArrayList<ColResult> hits = new ArrayList<ColResult>();
                    for (int i = 0; i < hitArray.length; i++) {
                        SearchHit searchHit = searchHits.getAt(i);
                        String fieldResult = searchHit != null ?
                            searchHit.field(queryField).getValue().toString() :
                            "";
                        double score = ScoreUtil.getDistance(value, fieldResult) * 100;
                        if (searchHit != null) {
                            List<Object> resList = chgToHighLights(searchHit, value, score);
                            score = Double.valueOf(resList.get(1).toString());
                            if (score >= minScore) {
                                Map<String, Object> resMap = (Map<String, Object>) resList.get(0);
                                ColResult colResult = new ColResult();
                                colResult.setResult(resMap);
                                colResult.setScore(score);
                                hits.add(colResult);
                            }
                        }
                    }
                    result.setHits(hits);
                }
            }
            searceResult.add(result);
        }
        Map<String, List<SResult>> billRes = new HashMap<String, List<SResult>>();

        billRes.put(jsonObject.getString("billId"), searceResult);
        String resultJson = JSON.toJSONString(billRes, false);
        return resultJson;
    }

    /**
     * 获取高亮字段的带标签值，替换原值
     *
     * @param searchHit
     * @return
     */
    private List<Object> chgToHighLights(SearchHit searchHit, String searchValue, double score) {
        HashMap<String, Object> highMap = new HashMap<String, Object>();
        List<Object> resList = new ArrayList<Object>();
        Map<String, SearchHitField> hitfieldMap = searchHit.getFields();
        Map<String, HighlightField> highlightFieldMap = searchHit.highlightFields();
        double temp = 0.0;
        Iterator<String> keyIterator = highlightFieldMap.keySet().iterator();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            HighlightField highlightField = highlightFieldMap.get(key);
            if (highlightField != null) {
                String original = hitfieldMap.get(key).value().toString();
                double newScore = ScoreUtil.getDistance(searchValue, original) * 100;
                if (newScore > temp) {
                    temp = newScore;
                }
                highMap.put(key, original);
            }
        }
        resList.add(highMap);
        resList.add(temp);
        return resList;
    }

    /**
     * 获取高亮字段的带标签值，替换原值
     *
     * @param searchHit
     * @return
     */
    private HashMap<String, Object> chgToHighLights(SearchHit searchHit) {
        HashMap<String, Object> resMap = new HashMap<String, Object>();
        Map<String, SearchHitField> hitfieldMap = searchHit.getFields();
        Map<String, HighlightField> highlightFieldMap = searchHit.highlightFields();
        Iterator<String> keyIterator = highlightFieldMap.keySet().iterator();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            HighlightField highlightField = highlightFieldMap.get(key);
            if (highlightField != null) {
                String original = hitfieldMap.get(key).value().toString();
                resMap.put(key, original);
            }
        }
        return resMap;
    }

}
