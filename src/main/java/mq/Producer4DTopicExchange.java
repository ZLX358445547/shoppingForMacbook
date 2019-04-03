package mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/*
* 生产者--投递消息
* */
public class Producer4DTopicExchange {
    public static void main(String[] args) {
        //step1:ConnectionFactory
        ConnectionFactory connectionFactory =  new ConnectionFactory();
        //ip
        connectionFactory.setHost("39.96.66.122");
        //port
        connectionFactory.setPort(5672);
        //virtualhost
        connectionFactory.setVirtualHost("/");
        //username
        connectionFactory.setUsername("root");
        //password
        connectionFactory.setPassword("123456789");

        //step2:Connection
        Connection connection = null;
        Channel channel = null;
        try {
            connection = connectionFactory.newConnection();
            //step3:Channel
             channel = connection.createChannel();
            String routingkey = "test.direct";
            String exchangeName  = "test_direct_exchange";
            for (int i = 0;i<5;i++){
                String msg = "hello,rabbitmq"+i;
                //step4:发消息
                channel.basicPublish(exchangeName,routingkey,null,msg.getBytes());

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }finally {
            try {
                channel.close();
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }

        }



    }

}
