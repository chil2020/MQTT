package com.amobile.emqx.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.amobile.emqx.config.MqttConfig.MqttGateway;
import com.amobile.emqx.properties.MqttProperties;

@RestController
public class Mqtt {
	
	@Autowired
	private MqttGateway mqttGate;
	@Autowired
	private MqttProperties mqtt;	
	private boolean hastopic;

	@PostMapping("/MqttPublish")
	public String MqttPublish(String data,String Topic,Integer qos)
	{
		for (String sub : mqtt.getSubscription()) 
		{
			if(sub.equals(Topic))
				hastopic=true;
		} 
		if(hastopic)
		{
			mqttGate.send(data, Topic, qos);
			hastopic=false;
			return "Success";
		}	
		else 			
			return "Failed";			
	}
}
