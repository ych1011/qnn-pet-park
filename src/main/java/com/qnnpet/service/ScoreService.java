package com.qnnpet.service;

import com.qnnpet.dto.AddScoreRequest;
import com.qnnpet.dto.AddScoreResponse;

import java.util.List;
import java.util.Map;

public interface ScoreService {

    AddScoreResponse addScore(AddScoreRequest request, Long teacherId);

    void undoLatestScore(Long teacherId);

    List<Map<String, Object>> getRanking(Long classId, Long teacherId);

    Map<String, Object> getScoreLogs(Long studentId, Integer page, Integer size, Long teacherId);
}
