package org.study.stackoverflow.stackexchange.pojo;

import lombok.Data;

import java.util.List;

@Data
public class Questions {

    private Long questionId;
    private List<String> tags;

    private Long ownerId;

    private Boolean isAnswered;
    private Long viewCount;
    private Long answerCount;

    private Long acceptedAnswerId;

    private Long downVoteCount;

    private Long upVoteCount;

//    private Long favorite_count;

    private Long lastActivityDate;
    private Long creationDate;
//    closed_date
//            closed_reason
//    private String contentLicense;
    private String link;
    private String title;

}
