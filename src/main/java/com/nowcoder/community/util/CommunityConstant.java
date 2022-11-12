package com.nowcoder.community.util;

public interface CommunityConstant {

    //状态码
    int ACTIVATION_SUCCESS = 0;
    int ACTIVATION_REPEAT = 1;
    int ACTIVATION_FAILED = 2;

    //默认状态的登录凭证超时时间
    int DEFAULT_EXPIRED_SECOND = 3600*12;

    //记住状态的登录凭证超时时间
    int REMEMBER_EXPIRED_SECOND = 3600*12*30;
    //实体类型：帖子
    int ENTITY_TYPE_POST=1;
    //实体类型：评论
    int ENTITY_TYPE_COMMENT = 2;
}
