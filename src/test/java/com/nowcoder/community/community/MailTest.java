package com.nowcoder.community.community;

import com.nowcoder.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
public class MailTest {
    private String mailAddress = "1150063458@qq.com";
    private String subject = "Test";
    private String content = "This is a test email";

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail(){
        mailClient.sendMail(mailAddress,subject,content);
    }

    @Test
    public void testHtmlMail(){

        Context context = new Context();
        context.setVariable("username","sunday");
        String content = templateEngine.process("/mail/demo",context);
        System.out.println(content);
//        System.out.println(content);
        mailClient.sendMail(mailAddress,subject,content);
    }

}
