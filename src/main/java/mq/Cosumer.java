package mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Cosumer {
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
            //step4:声明一个队列
            //根据routingkey进行匹配查找交换机
            String queueName = "testmq";
            channel.queueDeclare(queueName,true,false,false,null);
            //step5:生命一个消费者，绑定channel
            QueueingConsumer queueingConsumer = new QueueingConsumer(channel);

            channel.basicConsume(queueName,true,queueingConsumer);

            while (true){
                //step6：消费者取消息
                QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
                String msg   = new String (delivery.getBody());
                System.out.println("==-===cosumer"+msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
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
