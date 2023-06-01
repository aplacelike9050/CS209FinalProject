package org.study.stackoverflow.stackexchange.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;

/**
 * stack overflow rest api
 */
@Slf4j
public class StackExchangeApi {

    private final static String BASE_URI = "https://api.stackexchange.com/";

    private final static String API_VERSION = "2.3";

    private final static String QUESTION_API = BASE_URI + API_VERSION + "/questions";
    private final static String USER_API = BASE_URI + API_VERSION + "/users/{ids}/";
//    https://api.stackexchange.com/docs/questions-on-users#order=desc&sort=activity&ids=18196409%3B21946093&filter=default&site=stackoverflow

    public static String getQuestion(String fromdate, String todate, String sort, Integer page){
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("fromdate", DateUtil.beginOfDay(DateUtil.parseDate(fromdate)));
        paramMap.put("todate", DateUtil.beginOfDay(DateUtil.parseDate(todate)));
        paramMap.put("page", page);
        paramMap.put("pagesize", 100);
        paramMap.put("sort", sort);
        paramMap.put("tagged", "java");
        paramMap.put("site", "stackoverflow");
        paramMap.put("order", "desc");

        String result = HttpUtil.get(QUESTION_API, paramMap);
        log.info("getQuestion result >>> {}", result);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /**
     * @param type questions answers comments
     * @param userId
     * @param sort
     * @return
     */
    public static String getUserStatistics(String type, Long userId, String sort) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("page", 1);
        // max 100
        paramMap.put("pagesize", 100);
        paramMap.put("sort", sort);
        paramMap.put("site", "stackoverflow");
        paramMap.put("order", "desc");

        String result = HttpUtil.get(USER_API.replace("{ids}", userId + "") + type, paramMap);
        log.info("getUserStatistics {} result >>> {}", type, result);

        // we consider > 30 request/sec per IP to be very abusive and thus cut the requests off very harshly.
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return result;
    }


}
