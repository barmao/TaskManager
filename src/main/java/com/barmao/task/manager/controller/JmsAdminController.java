package com.barmao.task.manager.controller;

import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.apache.activemq.broker.jmx.TopicViewMBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller to provide ActiveMQ broker statistics and information
 */
@RestController
@RequestMapping("/api/jms")
public class JmsAdminController {

    private final JmsTemplate jmsTemplate;

    @Autowired
    public JmsAdminController(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    /**
     * Get statistics about the ActiveMQ broker and its destinations
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getJmsStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Get the MBean server connection
            MBeanServerConnection mbeanServer = ManagementFactory.getPlatformMBeanServer();

            // Get the broker bean
            ObjectName brokerObjectName = new ObjectName(
                    "org.apache.activemq:type=Broker,brokerName=embedded");
            BrokerViewMBean broker = MBeanServerInvocationHandler.newProxyInstance(
                    mbeanServer, brokerObjectName, BrokerViewMBean.class, true);

            // Get basic broker info
            stats.put("brokerName", broker.getBrokerName());
            stats.put("brokerVersion", broker.getBrokerVersion());
            stats.put("uptime", broker.getUptime());
            stats.put("memoryPercentUsage", broker.getMemoryPercentUsage());
            stats.put("storePercentUsage", broker.getStorePercentUsage());

            // Get topic stats
            Map<String, Object> topicStats = new HashMap<>();
            for (ObjectName topicName : broker.getTopics()) {
                TopicViewMBean topic = MBeanServerInvocationHandler.newProxyInstance(
                        mbeanServer, topicName, TopicViewMBean.class, true);

                Map<String, Object> topicInfo = new HashMap<>();
                topicInfo.put("name", topic.getName());
                topicInfo.put("producerCount", topic.getProducerCount());
                topicInfo.put("consumerCount", topic.getConsumerCount());
                topicInfo.put("queueSize", topic.getQueueSize());
                topicInfo.put("messageCount", topic.getEnqueueCount());

                topicStats.put(topic.getName(), topicInfo);
            }
            stats.put("topics", topicStats);

            // Get queue stats
            Map<String, Object> queueStats = new HashMap<>();
            for (ObjectName queueName : broker.getQueues()) {
                QueueViewMBean queue = MBeanServerInvocationHandler.newProxyInstance(
                        mbeanServer, queueName, QueueViewMBean.class, true);

                Map<String, Object> queueInfo = new HashMap<>();
                queueInfo.put("name", queue.getName());
                queueInfo.put("producerCount", queue.getProducerCount());
                queueInfo.put("consumerCount", queue.getConsumerCount());
                queueInfo.put("queueSize", queue.getQueueSize());
                queueInfo.put("messageCount", queue.getEnqueueCount());

                queueStats.put(queue.getName(), queueInfo);
            }
            stats.put("queues", queueStats);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            stats.put("error", "Failed to get JMS stats: " + e.getMessage());
            return ResponseEntity.ok(stats);
        }
    }

    /**
     * Send a test message to verify JMS is working
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> sendTestMessage() {
        Map<String, String> response = new HashMap<>();

        try {
            // Send a test message to a topic
            jmsTemplate.convertAndSend("test-topic", "Test message sent at " + System.currentTimeMillis());
            response.put("status", "success");
            response.put("message", "Test message sent to 'test-topic'");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to send test message: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
