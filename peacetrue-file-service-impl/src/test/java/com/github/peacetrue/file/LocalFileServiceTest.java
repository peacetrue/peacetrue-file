package com.github.peacetrue.file;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;

/**
 * @author : xiayx
 * @since : 2020-11-23 13:13
 **/

@SpringBootTest(
        classes = TestServiceFileAutoConfiguration.class,
        properties = {
                "peacetrue.file.basePath=/Users/xiayx/Documents/Projects/peacetrue-file/peacetrue-file-service-impl/src/test/resources"
        }
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LocalFileServiceTest {

    @Autowired
    private FileService fileService;

    @Test
    void upload() throws Exception {
        FileVO fileVO = fileService.upload(new FileUploadDTO(new FileSystemResource("/Users/xiayx/Documents/Projects/peacetrue-file/peacetrue-file-service-impl/src/test/java/com/github/peacetrue/file/LocalFileServiceTest.java")));
        System.out.println(fileVO);
    }
}
