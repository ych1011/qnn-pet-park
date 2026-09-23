package com.qnnpet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddScoreResponse {

    private Long logId;
    private Integer newScore;
    private Integer newLevel;
    private Boolean leveledUp;
    private Integer oldLevel;
}