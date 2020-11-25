package com.github.peacetrue.file;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author xiayx
 */
@Data
@ConfigurationProperties(prefix = "peacetrue.file")
public class ServiceFileProperties {

    /** 文件存储基础路径 */
    private String basePath;


}
