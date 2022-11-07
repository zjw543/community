package com.nowcoder.community.community;

import com.nowcoder.community.filter.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SensitiveWordTset {
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void test(){
        String text = "这里可以吸毒，可以-开-票-。keyifu☆c☆kaaa";
        System.out.println(sensitiveFilter.filter(text));
    }
}
