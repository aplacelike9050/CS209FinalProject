package org.study.stackoverflow.restapi;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.hash.Hash;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.text.csv.CsvWriter;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.study.stackoverflow.stackexchange.api.StackExchangeApi;
import org.study.stackoverflow.stackexchange.pojo.Questions;
import org.study.stackoverflow.stackexchange.pojo.UserStatisticsData;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class AnalyzesController {

    final CsvReader reader = CsvUtil.getReader();

    private String fileSeparator = File.separator;


    @GetMapping("/{tag}/questions")
    private Object questionsAnalyzes(@PathVariable("tag") String tag, String status) {
        final List<Questions> questions = reader.read(
                ResourceUtil.getUtf8Reader(System.getProperty("user.dir") + fileSeparator + "csv" + fileSeparator + "questions.csv"), Questions.class);
        Map<String, Object> result = new HashMap<>();


        if (Objects.equals("no-answers", status)) {
            long noAnswersCount = questions.stream().map(Questions::getAnswerCount).filter(count -> count == 0).count();
            int size = questions.size();
            log.info("no-answers >>> noAnswersCount:{} ,total:{} ", noAnswersCount, size);

            result.put("value", NumberUtil.round(NumberUtil.div(noAnswersCount, size) * 100, 2));
        } else if (Objects.equals("avg-answers", status)) {
            double avgAnswerCount = questions.stream().map(Questions::getAnswerCount).mapToInt(Long::intValue).average().getAsDouble();
            int maxAnswersCount = questions.stream().map(Questions::getAnswerCount).mapToInt(Long::intValue).max().getAsInt();

            result.put("meanValue", NumberUtil.round(avgAnswerCount, 2));
            result.put("maxValue", maxAnswersCount);
        } else if (Objects.equals("distribution-answers", status)) {
            return questions.stream()
                    .collect(Collectors.groupingBy(Questions::getAnswerCount, Collectors.counting()))
                    .entrySet().stream()
                    .map(e -> new KeyValueVO("answersCount-" + e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
        }

        return result;
    }

    @GetMapping("/{tag}/answers")
    private Object answersAnalyzes(@PathVariable("tag") String tag, String status) {
        final List<Questions> questions = reader.read(
                ResourceUtil.getUtf8Reader(System.getProperty("user.dir") + fileSeparator + "csv" + fileSeparator + "questions.csv"), Questions.class);
        Map<String, Object> result = new HashMap<>();

        if (Objects.equals("accepted", status)) {
            // is_answered:true accepted_answer_id:not null
            long count = questions.stream().filter(q -> Objects.equals(Boolean.TRUE, q.getIsAnswered()) && q.getAcceptedAnswerId() != null).count();

            int size = questions.size();
            log.info("no-answers >>> count:{} ,total:{} ", count, size);

            result.put("value", NumberUtil.round(NumberUtil.div(count, size) * 100, 2));
        } else if (Objects.equals("distribution-accepted", status)) {
            long total = questions.stream().filter(q -> Objects.equals(Boolean.TRUE, q.getIsAnswered()) && q.getAcceptedAnswerId() != null).count();

            Map<String, Long> value = questions.stream()
                    .filter(q -> Objects.equals(Boolean.TRUE, q.getIsAnswered()) && q.getAcceptedAnswerId() != null)
                    .collect(Collectors.groupingBy(
                            q -> {
                                String type = "1周以上";
                                long between = DateUtil.between(new Date(q.getLastActivityDate() * 1000), new Date(q.getCreationDate() * 100), DateUnit.DAY);
                                if (between <= 1) {
                                    type = "1天内";
                                } else if (between <= 3) {
                                    type = "3天内";
                                } else if (between <= 7) {
                                    type = "7天内";
                                }
                                return type;
                            },
                            Collectors.counting()));

            // 数据填充
            value.put("1天内", value.getOrDefault("1天内", 0L));
            value.put("3天内", value.getOrDefault("3天内", 0L));
            value.put("7天内", value.getOrDefault("7天内", 0L));
            value.put("1周以上", value.getOrDefault("1周以上", 0L));

//            result.put("total", total);
//            result.put("value", value.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> NumberUtil.round(NumberUtil.div(e.getValue().longValue(), total) * 100, 2))));
            Map<String, BigDecimal> collect = value.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> NumberUtil.round(NumberUtil.div(e.getValue().longValue(), total) * 100, 2)));
            ArrayList<Map<String, Object>> list = new ArrayList<>();
            Set<String> strings = collect.keySet();
            for (String name : strings) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("name", name);
                map.put("value", collect.get(name));
                list.add(map);
            }
            return list;
        } else if (Objects.equals("unaccepted", status)) {
            // is_answered:true accepted_answer_id: null
            // up_vote_count:not null > down_vote_count:not null
            long total = questions.stream().filter(q -> Objects.equals(Boolean.TRUE, q.getIsAnswered()) && q.getAcceptedAnswerId() == null).count();


            long count = questions.stream()
                    .filter(q -> Objects.equals(Boolean.TRUE, q.getIsAnswered()) && q.getAcceptedAnswerId() == null
                            && q.getUpVoteCount() != null && q.getDownVoteCount() != null
                            && q.getUpVoteCount() > q.getDownVoteCount()
                    )
                    .count();
            result.put("value", NumberUtil.round(NumberUtil.div(count, total) * 100, 2));
        }

        return result;
    }


    @GetMapping("/{tag}/tags")
    private Object tagsAnalyzes(@PathVariable("tag") String tag, String status,
                                @RequestParam(value = "top", defaultValue = "10") Integer top) {
        final List<Questions> questions = reader.read(
                ResourceUtil.getUtf8Reader(System.getProperty("user.dir") + fileSeparator + "csv" + fileSeparator + "questions.csv"), Questions.class);
        Map<String, Object> result = new HashMap<>();

        if (Objects.equals("often", status)) {
            // get tags then remove java
            return questions.stream()
                    .filter(q -> q.getTags() != null)
                    .flatMap(q -> q.getTags().stream())
                    .filter(t -> !Objects.equals("java", t))
                    .collect(Collectors.groupingBy(t -> t, Collectors.counting()))
                    // tag java often desc
                    .entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .map(entry -> new KeyValueVO(entry.getKey(), entry.getValue()))
                    .limit(top)
                    .collect(Collectors.toList());
        } else if (Objects.equals("votes", status)) {
            // question_id -> votes
            Map<Long, Long> questionId2votes = questions.stream()
                    .filter(q -> q.getUpVoteCount() != null)
                    .collect(Collectors.toMap(Questions::getQuestionId, Questions::getUpVoteCount));

            // question_id -> tags
            Map<Long, List<String>> questionId2tags = questions.stream()
                    .filter(q -> q.getTags() != null)
                    .collect(Collectors.toMap(Questions::getQuestionId,
                            q -> {
                                q.getTags().remove("java");
                                return q.getTags();
                            })
                    );

            Map<String, Long> tagsMap = new HashMap<>();
            for (Long questionId : questionId2tags.keySet()) {
                List<String> tags = questionId2tags.get(questionId).stream().sorted().collect(Collectors.toList());
                Long value = questionId2votes.getOrDefault(questionId, 0L);

                // only java tag
                if (tags.size() == 0) {
                    Long oldValue = tagsMap.getOrDefault("only-java", 0L);
                    tagsMap.put("only-java", oldValue + value);

                    continue;
                }

                // tag
                for (String t : tags) {
                    Long oldValue = tagsMap.getOrDefault(t, 0L);
                    tagsMap.put(t, oldValue + value);
                }

                // tag combine
                String tagsStr = tags.toString();
                Long oldValue = tagsMap.getOrDefault(tagsStr, 0L);
                tagsMap.put(tagsStr, oldValue + value);
            }

            return tagsMap.entrySet()
                    .stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .map(entry -> new KeyValueVO(entry.getKey(), entry.getValue()))
                    .limit(top)
                    .collect(Collectors.toList());

        } else if (Objects.equals("views", status)) {
            // question_id -> views
            Map<Long, Long> questionId2views = questions.stream()
                    .filter(q -> q.getViewCount() != null)
                    .collect(Collectors.toMap(Questions::getQuestionId, Questions::getViewCount));

            // question_id -> tags
            Map<Long, List<String>> questionId2tags = questions.stream()
                    .filter(q -> q.getTags() != null)
                    .collect(Collectors.toMap(Questions::getQuestionId,
                            q -> {
                                q.getTags().remove("java");
                                return q.getTags();
                            })
                    );

            Map<String, Long> tagsMap = new HashMap<>();
            for (Long questionId : questionId2tags.keySet()) {
                List<String> tags = questionId2tags.get(questionId).stream().sorted().collect(Collectors.toList());
                Long value = questionId2views.getOrDefault(questionId, 0L);


                // only java tag
                if (tags.size() == 0) {
                    Long oldValue = tagsMap.getOrDefault("only-java", 0L);
                    tagsMap.put("only-java", oldValue + value);

                    continue;
                }

                // tag
                for (String t : tags) {
                    Long oldValue = tagsMap.getOrDefault(t, 0L);
                    tagsMap.put(t, oldValue + value);
                }

                // tag combine
                String tagsStr = tags.toString();
                Long oldValue = tagsMap.getOrDefault(tagsStr, 0L);
                tagsMap.put(tagsStr, oldValue + value);
            }

            List<KeyValueVO> collect = tagsMap.entrySet()
                    .stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .map(entry -> new KeyValueVO(entry.getKey(), entry.getValue()))
                    .limit(top)
                    .collect(Collectors.toList());
            ArrayList<Object> x = new ArrayList<>();
            ArrayList<Object> y = new ArrayList<>();
            for (KeyValueVO keyValueVO : collect) {
                x.add(keyValueVO.getName());
                y.add(keyValueVO.getValue());
            }
            result.put("x", x);
            result.put("y", y);
        }

        return result;
    }

    @GetMapping("/{tag}/users")
    private Object usersAnalyzes(@PathVariable("tag") String tag, String status,
                                 @RequestParam(value = "top", defaultValue = "10") Integer top) {
        final List<UserStatisticsData> userStatistics = reader.read(
                ResourceUtil.getUtf8Reader(System.getProperty("user.dir") + fileSeparator + "csv" + fileSeparator + "user-statistics.csv"), UserStatisticsData.class);
        Map<String, Object> result = new HashMap<>();

        if (Objects.equals("distribution", status)) {
            Map<String, Predicate<UserStatisticsData>> map = new HashMap<>();
            map.put("questions", us -> us.getQuestionsCount() != null && us.getQuestionsCount() > 0);
            map.put("answers", us -> us.getAnswersCount() != null && us.getAnswersCount() > 0);
            map.put("comments", us -> us.getCommentsCount() != null && us.getCommentsCount() > 0);

            return map.entrySet().stream().map(e -> new KeyValueVO(e.getKey(), userStatistics.stream().filter(e.getValue()).count()));
        } else if (Objects.equals("most-active", status)) {
            Comparator<UserStatisticsData> comparator = Comparator.comparing(UserStatisticsData::getAnswersCount, Comparator.reverseOrder())
                    .thenComparing(UserStatisticsData::getCommentsCount, Comparator.reverseOrder())
                    .thenComparing(UserStatisticsData::getQuestionsCount, Comparator.reverseOrder());
            userStatistics.sort(comparator);

            List<UserStatisticsData> collect = userStatistics.stream().limit(top).collect(Collectors.toList());
            ArrayList<Object> userIds = new ArrayList<>();
            ArrayList<Object> questionsCounts = new ArrayList<>();
            ArrayList<Object> answersCounts = new ArrayList<>();
            for (UserStatisticsData userStatisticsData : collect) {
                userIds.add(userStatisticsData.getUserId());
                questionsCounts.add(userStatisticsData.getQuestionsCount());
                answersCounts.add(userStatisticsData.getAnswersCount());
            }
            result.put("userIds", userIds);
            result.put("questionsCounts", questionsCounts);
            result.put("answersCounts", answersCounts);
        }

        return result;
    }

    @GetMapping("test1")
    public String syncQuestion1() {
        final List<Questions> questions = reader.read(
                ResourceUtil.getUtf8Reader(System.getProperty("user.dir") + fileSeparator + "csv" + fileSeparator + "questions.csv"), Questions.class);

        List<Long> collect = questions.stream().map(Questions::getOwnerId).collect(Collectors.toList());

        CompletableFuture.runAsync(() -> syncUser(collect));


        return "1";

    }

    private void syncUser(List<Long> userList) {
        log.info("syncUser >>>>>>>>>");
        Map<String, BiConsumer<UserStatisticsData, Integer>> map = new HashMap<>();
        map.put("questions", UserStatisticsData::setQuestionsCount);
        map.put("answers", UserStatisticsData::setAnswersCount);
        map.put("comments", UserStatisticsData::setCommentsCount);

        List<UserStatisticsData> userStatisticsDataList = new ArrayList<>();
        boolean flag = false;
        for (Long userId : userList) {
            UserStatisticsData userStatisticsData = new UserStatisticsData();

            for (String type : map.keySet()) {
                String questions = StackExchangeApi.getUserStatistics(type, userId, "creation");
                if (questions != null && questions.contains("error_id")) {
                    log.error("syncUser error :{}", questions);
                    flag = true;
                    break;
                }
                // 解析数据
                JSONObject json = JSONUtil.parseObj(questions);

                map.get(type).accept(userStatisticsData, json.getJSONArray("items").size());

                if (!json.getBool("has_more") && json.getInt("quota_remaining") == 0) {
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

}
