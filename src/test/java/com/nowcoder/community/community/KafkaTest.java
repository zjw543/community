package com.nowcoder.community.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;



@SpringBootTest
public class KafkaTest {
    @Autowired
    private kafkaProducer kafkaProducer;

    @Test
    public void test(){
        kafkaProducer.sendMessage("test","hello");
        kafkaProducer.sendMessage("test","hello2");
        try {
            Thread.sleep(1000*10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}
@Component
class kafkaProducer{
    @Autowired
    private KafkaTemplate kafkaTemplate;
    public void sendMessage(String topic ,String content){
        kafkaTemplate.send(topic,content);
    }
}

@Component
class kafkaConsumer{
    @KafkaListener(topics = {"test"})
    public void handleMessage(ConsumerRecord record){
        System.out.println(record.value());
    }
}