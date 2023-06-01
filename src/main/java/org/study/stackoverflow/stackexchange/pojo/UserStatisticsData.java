package org.study.stackoverflow.stackexchange.pojo;


import lombok.Data;

@Data
public class UserStatisticsData {
    private Long userId;
    private Integer questionsCount;
    private Integer answersCount;
    private Integer commentsCount;
}
