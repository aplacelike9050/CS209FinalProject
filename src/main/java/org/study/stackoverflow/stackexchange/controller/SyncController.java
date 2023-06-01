package org.study.stackoverflow.stackexchange.controller;

import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.text.csv.CsvWriter;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.study.stackoverflow.stackexchange.api.StackExchangeApi;
import org.study.stackoverflow.stackexchange.pojo.Questions;
import org.study.stackoverflow.stackexchange.pojo.QuestionsBO;
import org.study.stackoverflow.stackexchange.pojo.UserStatisticsData;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/stackexchange/sync")
public class SyncController {


    private String fileSeparator = File.separator;

    @GetMapping("questions")
    public String syncQuestion(@RequestParam("fromdate") String fromdate,
                               @RequestParam("todate") String todate,
                               @RequestParam(value = "totalPage", defaultValue = "25") Integer totalPage) {

//        activity – last_activity_date
//        creation – creation_date
//        votes – score
//        hot – by the formula ordering the hot tab
//        Does not accept or minmax
//        week – by the formula ordering the week tab
//        Does not accept or minmax
//        month – by the formula ordering the month tab
//        Does not accept or minmax
//        activity is the default sort.

        String msg = "success";
        List<QuestionsBO> questions = new ArrayList<>();
        for (Integer i = 1; i <= totalPage; i++) {
            String result = StackExchangeApi.getQuestion(fromdate, todate, "creation", i);
            if (result != null && result.contains("error_id")) {
//                return result;
                log.error("syncQuestion error :{}", questions);
                break;
            }

            // 解析数据
            JSONObject json = JSONUtil.parseObj(result);

            questions.addAll(json.getBeanList("items", QuestionsBO.class));

            if (!json.getBool("has_more")) {
                msg = "interrupt";
                break;
            }
        }

        long count = questions.stream().map(QuestionsBO::getQuestionId).distinct().count();
        log.info("distinct:{}", count);
        if (questions.size() == 0) {
            return "sync count: 0";
        }

        // 写入csv
        // question_id,tags,is_answered,view_count,answer_count,score,last_activity_date,creation_date,content_license,link,title
        CsvWriter writer = CsvUtil.getWriter(System.getProperty("user.dir") + fileSeparator + "csv" + fileSeparator + "questions.csv", CharsetUtil.CHARSET_UTF_8);
        List<Questions> collect = questions.stream().map(this::convertBO).collect(Collectors.toList());
        writer.writeBeans(collect);

        // users
        CompletableFuture.runAsync(()->syncUser(collect.stream().map(Questions::getOwnerId).limit(100).collect(Collectors.toList())));
        return msg + ", sync count : " + questions.size();
    }

    private void syncUser(List<Long> userList) {
        log.info("syncUser >>>>>>>>>");
        Map<String, BiConsumer<UserStatisticsData,Integer>> map = new HashMap<>();
        map.put("questions", UserStatisticsData::setQuestionsCount);
        map.put("answers", UserStatisticsData::setAnswersCount);
        map.put("comments", UserStatisticsData::setCommentsCount);

        List<UserStatisticsData> userStatisticsDataList = new ArrayList<>();
        boolean flag = false;
        for (Long userId : userList) {
            UserStatisticsData userStatisticsData = new UserStatisticsData();
            userStatisticsData.setUserId(userId);

            for (String type : map.keySet()) {
                String questions = StackExchangeApi.getUserStatistics(type, userId, "creation");
                if (questions != null && questions.contains("error_id")) {
                    log.error("syncUser error :{}", questions);
                    flag = true;
                    break;
                }
                // 解析数据
                JSONObject json = JSONUtil.parseObj(questions);

                map.get(type).accept(userStatisticsData,json.getJSONArray("items").size());

                if (!json.getBool("has_more") && json.getInt("quota_remaining") == -1) {
                    flag = true;
                    break;
                }
            }

            if (flag) {
                break;
            }
            userStatisticsDataList.add(userStatisticsData);
        }

        CsvWriter writer = CsvUtil.getWriter(System.getProperty("user.dir") + fileSeparator + "csv" + fileSeparator + "user-statistics.csv", CharsetUtil.CHARSET_UTF_8);
        writer.writeBeans(userStatisticsDataList);

        log.info("syncUser <<<<<<<<<");
    }


    private Questions convertBO(QuestionsBO questionsBO){
        Questions questions = new Questions();
        BeanUtils.copyProperties(questionsBO, questions);
        questions.setOwnerId(questionsBO.getOwner().getUserId());

        return questions;
    }

}
