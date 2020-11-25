package com.github.peacetrue.file;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Objects;

/**
 * @author xiayx
 */
@Configuration
@EnableConfigurationProperties(ServiceFileProperties.class)
@ComponentScan(basePackageClasses = ServiceFileAutoConfiguration.class)
@PropertySource("classpath:/application-file-service.yml")
public class ServiceFileAutoConfiguration {

    private ServiceFileProperties properties;

    public ServiceFileAutoConfiguration(ServiceFileProperties properties) {
        this.properties = Objects.requireNonNull(properties);
    }

    @Bean
    public LocalFileService localFileService() {
        return new LocalFileService(properties.getBasePath());
    }

}
