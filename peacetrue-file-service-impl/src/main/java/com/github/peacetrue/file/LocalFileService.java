package com.github.peacetrue.file;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Objects;

import static com.github.peacetrue.util.DateTimeFormatterUtils.*;

/**
 * 本地文件服务
 *
 * @author : xiayx
 * @since : 2020-11-23 10:58
 **/
@Data
@Slf4j
public class LocalFileService implements FileService {

    /** 绝对物理路径 */
    private String basePath;

    public LocalFileService(String basePath) {
        this.basePath = Objects.requireNonNull(basePath, "basePath must not be null").trim();
        //TODO create base path if not exists
        //TODO use Path replace String
    }

    private void handleBasePath(String basePath) {
        Path path = Paths.get(basePath);
        if (Files.exists(path)) return;
        try {
            Files.createFile(path);
        } catch (IOException e) {
            throw new RuntimeException("创建文件异常", e);
        }
    }

    /** formatter: yyyy/MM/dd */
    public static DateTimeFormatter COMMON_DATE_TIME = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(YEAR)
            .appendLiteral('/')
            .append(MONTH)
            .appendLiteral('/')
            .append(DATE)
            .toFormatter();

    @Override
    public String buildRelativeFilePath(String filename) {
        return COMMON_DATE_TIME.format(LocalDateTime.now()) + "/" + filename;
    }

    @Override
    public String getAbsoluteFilePath(String relativeFilePath) {
        return basePath + "/" + relativeFilePath;
    }

    @Override
    public FileVO upload(FileUploadDTO params) throws IOException {
        Resource resource = null;
        log.info("上传文件[{}]到本地", resource);
        String filename = resource.getFilename();
        String filePath = basePath + "/" + filename;
        Path path = Paths.get(filePath);
        if (Files.notExists(path)) Files.createFile(path);
        FileCopyUtils.copy(resource.getInputStream(), Files.newOutputStream(path));
        return new FileVO(filename, filePath);
    }


}
