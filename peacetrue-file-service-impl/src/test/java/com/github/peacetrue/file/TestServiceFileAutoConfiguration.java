package com.github.peacetrue.file;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author : xiayx
 * @since : 2020-11-23 13:15
 **/
@Configuration(proxyBeanMethods = false)
@ImportAutoConfiguration({

})
@EnableAutoConfiguration
@ActiveProfiles("attachment-service-test")
public class TestServiceFileAutoConfiguration {
}
