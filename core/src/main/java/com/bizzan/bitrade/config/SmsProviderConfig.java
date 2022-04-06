package com.bizzan.bitrade.config;

import com.bizzan.bitrade.vendor.provider.support.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bizzan.bitrade.vendor.provider.SMSProvider;

@Configuration
public class SmsProviderConfig {

    @Value("${sms.gateway:}")
    private String gateway;
    @Value("${sms.username:}")
    private String username;
    @Value("${sms.password:}")
    private String password;
    @Value("${sms.sign:}")
    private String sign;
    @Value("${sms.ensign:}")
    private String ensign;
    @Value("${sms.internationalGateway:}")
    private String internationalGateway;
    @Value("${sms.internationalUsername:}")
    private String internationalUsername;
    @Value("${sms.internationalPassword:}")
    private String internationalPassword;
    @Value("${access.key.id:}")
    private String accessKey;
    @Value("${access.key.secret:}")
    private String accessSecret;

    @Bean
    public SMSProvider getSMSProvider(@Value("${sms.driver:}") String driverName) {

        if (driverName.equalsIgnoreCase(YunpianSMSProvider.getName())) {
            return new YunpianSMSProvider(gateway, password);
        }

        return new YunpianSMSProvider(gateway, password);
    }
}
