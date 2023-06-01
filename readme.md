### 网页访问
127.0.0.1:8080

### 数据存储
存储在项目的csv目录下，其中question.csv主要拥有问题分析的数据，user-statistics.csv为用户统计数据

### 数据同步
- 数据同步地址：http://localhost:8080/stackexchange/sync/questions?fromdate=2022-02-01&todate=2023-05-31


### 接口请求即返回结果
部分接口会携带top参数，如果不携带后端接口会默认设置成10！


1. http://localhost:8080/java/questions?status=no-answers
```json
{
    "value": 59.32
}
```

2. http://localhost:8080/java/questions?status=avg-answers
```json
{
    "maxValue": 6,
    "meanValue": 0.52
}
```

3. http://localhost:8080/java/questions?status=distribution-answers
```json
[
    {
        "name": "answersCount-0",
        "value": 1483
    },
    {
        "name": "answersCount-1",
        "value": 793
    },
    {
        "name": "answersCount-2",
        "value": 177
    },
    {
        "name": "answersCount-3",
        "value": 39
    },
    {
        "name": "answersCount-4",
        "value": 4
    },
    {
        "name": "answersCount-5",
        "value": 2
    },
    {
        "name": "answersCount-6",
        "value": 2
    }
]
```

4. http://localhost:8080/java/answers?status=accepted
```json
{
    "value": 14.4
}
```

5. http://localhost:8080/java/answers?status=distribution-accepted
```json
{
    "1周以上": 100,
    "3天内": 0,
    "1天内": 0,
    "7天内": 0
}
```

6. http://localhost:8080/java/answers?status=unaccepted
```json
{
    "value": 5.53
}
```

7. http://localhost:8080/java/tags?status=often&top=10
```json
[
    {
        "name": "spring-boot",
        "value": 428
    },
    {
        "name": "spring",
        "value": 284
    },
    {
        "name": "android",
        "value": 260
    },
    {
        "name": "maven",
        "value": 94
    },
    {
        "name": "hibernate",
        "value": 87
    },
    {
        "name": "kotlin",
        "value": 86
    },
    {
        "name": "spring-data-jpa",
        "value": 68
    },
    {
        "name": "swing",
        "value": 63
    },
    {
        "name": "jpa",
        "value": 62
    },
    {
        "name": "android-studio",
        "value": 59
    }
]
```

8. http://localhost:8080/java/tags?status=views&top=10
```json
[
    {
        "name": "spring-boot",
        "value": 13796
    },
    {
        "name": "spring",
        "value": 8522
    },
    {
        "name": "android",
        "value": 6554
    },
    {
        "name": "only-java",
        "value": 4133
    },
    {
        "name": "maven",
        "value": 3347
    },
    {
        "name": "hibernate",
        "value": 2656
    },
    {
        "name": "kotlin",
        "value": 2541
    },
    {
        "name": "spring-data-jpa",
        "value": 2404
    },
    {
        "name": "javafx",
        "value": 2256
    },
    {
        "name": "swing",
        "value": 2145
    }
]
```

9. http://localhost:8080/java/tags?status=votes&top=10
```json
[
    {
        "name": "[double, floating-point]",
        "value": 7
    },
    {
        "name": "java-time",
        "value": 7
    },
    {
        "name": "double",
        "value": 7
    },
    {
        "name": "floating-point",
        "value": 7
    },
    {
        "name": "[java-time]",
        "value": 7
    },
    {
        "name": "only-java",
        "value": 7
    },
    {
        "name": "redis",
        "value": 6
    },
    {
        "name": "[amazon-elasticache, local, redis]",
        "value": 6
    },
    {
        "name": "amazon-elasticache",
        "value": 6
    },
    {
        "name": "local",
        "value": 6
    }
]
```

10. http://localhost:8080/java/users?status=distribution
```json
[
    {
        "name": "comments",
        "value": 60
    },
    {
        "name": "questions",
        "value": 91
    },
    {
        "name": "answers",
        "value": 37
    }
]
```


11. http://localhost:8080/java/users?status=most-active&top=10
```json
[
    {
        "userId": 817543,
        "questionsCount": 86,
        "answersCount": 100,
        "commentsCount": 100
    },
    {
        "userId": 10908274,
        "questionsCount": 18,
        "answersCount": 100,
        "commentsCount": 100
    },
    {
        "userId": 4842504,
        "questionsCount": 12,
        "answersCount": 68,
        "commentsCount": 100
    },
    {
        "userId": 3139567,
        "questionsCount": 6,
        "answersCount": 57,
        "commentsCount": 100
    },
    {
        "userId": 4380384,
        "questionsCount": 12,
        "answersCount": 55,
        "commentsCount": 100
    },
    {
        "userId": 4380384,
        "questionsCount": 12,
        "answersCount": 55,
        "commentsCount": 100
    },
    {
        "userId": 448779,
        "questionsCount": 9,
        "answersCount": 55,
        "commentsCount": 100
    },
    {
        "userId": 2676598,
        "questionsCount": 68,
        "answersCount": 36,
        "commentsCount": 100
    },
    {
        "userId": 8709603,
        "questionsCount": 14,
        "answersCount": 34,
        "commentsCount": 46
    },
    {
        "userId": 2996452,
        "questionsCount": 22,
        "answersCount": 33,
        "commentsCount": 100
    }
]
```
