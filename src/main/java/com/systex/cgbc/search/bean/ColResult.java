package com.systex.cgbc.search.bean;

import java.util.Map;

public class ColResult {
    private Map<String, Object> result;

    private Double score;

    public Map<String, Object> getResult() {
        return result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
