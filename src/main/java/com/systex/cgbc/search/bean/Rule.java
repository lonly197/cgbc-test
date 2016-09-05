package com.systex.cgbc.search.bean;

/**
 * 匹配规则
 * @author 泳
 */
public class Rule {
    /**
     * 规则ID
     */
    private Long ruleId;
    /**
     * 列名
     */
    private String colName;
    /**
     * 匹配规则 1 精确 2 模糊
     */
    private Byte patternType;
    /**
     * 匹配个数
     */
    private Integer matchNum;
    /**
     * Top Num
     */
    private Integer takeNum = 5;
    /**
     * 最低分，默认95.0f
     */
    private Float score = 95.0f;

    /**
     * 是否可用，1禁用 2正常
     */
    private Byte isDisable = 2;

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public Byte getPatternType() {
        return patternType;
    }

    public void setPatternType(Byte patternType) {
        this.patternType = patternType;
    }

    public Integer getMatchNum() {
        return matchNum;
    }

    public void setMatchNum(Integer matchNum) {
        this.matchNum = matchNum;
    }

    public Integer getTakeNum() {
        return takeNum;
    }

    public void setTakeNum(Integer takeNum) {
        this.takeNum = takeNum;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public Byte getIsDisable() {
        return isDisable;
    }

    public void setIsDisable(Byte isDisable) {
        this.isDisable = isDisable;
	}
}
