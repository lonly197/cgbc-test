package com.systex.cgbc.search.bean;

import java.util.ArrayList;

public class SResult {

    /**
     * 配置类型，1 精确匹配 2模糊匹配
     */
    public Byte patternType;

    /**
     * 查询字段名称
     */
    public String queryField;

    /**
     * 总结果数
     */
    public Long totalHits;

    /**
     * 查询的目标值
     */
    public String queryTarget;

    /**
     * 查询结果、评分
     */
    public ArrayList<ColResult> hits;

    public Byte getPatternType() {
        return patternType;
    }

    public void setPatternType(Byte patternType) {
        this.patternType = patternType;
    }

    public Long getTotalHits() {
        return totalHits;
    }

    public void setTotalHits(Long totalHits) {
        this.totalHits = totalHits;
    }

    public ArrayList<ColResult> getHits() {
        return hits;
    }

    public void setHits(ArrayList<ColResult> hits) {
        this.hits = hits;
    }

    public String getQueryField() {
        return queryField;
    }

    public void setQueryField(String queryField) {
        this.queryField = queryField;
    }

    public String getQueryTarget() {
        return queryTarget;
    }

    public void setQueryTarget(String queryTarget) {
        this.queryTarget = queryTarget;
    }
}
