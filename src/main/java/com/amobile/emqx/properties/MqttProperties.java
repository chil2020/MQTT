package com.amobile.emqx.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties("mqtt")
public class MqttProperties {
	private String username;
	private String password;
	private String[] url;
	private String defaultTopic;
	private String sentclientID;
	private String receiveclientID;
	private Long connectionTimeout;
	private String[] subscription;
}
