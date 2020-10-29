package com.amobile.emqx.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.Header;

import com.amobile.emqx.properties.MqttProperties;

@Configuration
@IntegrationComponentScan
public class MqttConfig {
	@Autowired
	private MqttProperties mqtt;

	@Bean
	public MqttConnectOptions mqttConnect() {
		MqttConnectOptions connectOptions = new MqttConnectOptions();
		connectOptions.setUserName(mqtt.getUsername());
		connectOptions.setPassword(mqtt.getPassword().toCharArray());
		connectOptions.setServerURIs(mqtt.getUrl());
		connectOptions.setKeepAliveInterval(3);
		return connectOptions;
	}

	@Bean
	public MqttPahoClientFactory mqttClientFactory() {
		DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
		factory.setConnectionOptions(mqttConnect());
		return factory;
	}

	/* 建立發送通道 */
	@Bean
	public MessageChannel mqttOutput() {
		return new PublishSubscribeChannel();
	}

	/* 監聽Restful請求訊息並發布至Mqtt Broker */
	@Bean
	@ServiceActivator(inputChannel = "mqttOutput")
	public MessageHandler mqttOutputHandler() {
		MqttPahoMessageHandler handler = new MqttPahoMessageHandler(mqtt.getSentclientID(), mqttClientFactory());
		handler.setAsync(true);
		handler.setDefaultTopic(mqtt.getDefaultTopic());
		handler.setDefaultQos(2);
		return handler;
	}

	/* 建立接收通道 */
	@Bean
	public MessageChannel mqttInput() {
		return new DirectChannel();
	}

	/* 向MQTT Broker訂閱主題並接收訊息 */
	@Bean
	public MessageProducer messageProducer() {
		MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(mqtt.getReceiveclientID(),
				mqttClientFactory(), mqtt.getSubscription());
		adapter.setCompletionTimeout(mqtt.getConnectionTimeout());
		adapter.setConverter(new DefaultPahoMessageConverter());
		adapter.setQos(2);
		adapter.setOutputChannel(mqttInput());
		return adapter;
	}

	@Bean
	@ServiceActivator(inputChannel = "mqttInput")
	public MessageHandler mqttInputHandler() {
	    return new MessageHandler() {
	    @Override
	    public void handleMessage(Message<?> message) throws MessagingException {
	       String topic = message.getHeaders().get("mqtt_receivedTopic").toString();
	       String msg = message.getPayload().toString();
	       System.out.println("\nTopic:" + topic + "\nMessage:" + msg+"\n");	        
	       }};
	        	 
	}   
	
	@MessagingGateway(defaultRequestChannel = "mqttOutput")
	public interface MqttGateway {
	    void send(String data, @Header(MqttHeaders.TOPIC) String topic,@Header(MqttHeaders.QOS) Integer qos);
	}
}
