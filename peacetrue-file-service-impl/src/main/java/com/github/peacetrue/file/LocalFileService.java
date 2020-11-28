package com.github.peacetrue.file;

import com.github.peacetrue.util.DateTimeFormatterUtils2;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Objects;

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

    @Override
    public String buildRelativeFilePath(String filename) {
        return DateTimeFormatterUtils2.SEPARATOR_DATE_TIME.format(LocalDateTime.now())
                + (filename.startsWith(File.separator) ? "" : File.separator)
                + filename;
    }

    @Override
    public String getAbsoluteFilePath(String relativeFilePath) {
        return basePath + (relativeFilePath.startsWith(File.separator) ? "" : File.separator) + relativeFilePath;
    }

    @Override
    public FileVO upload(FileUploadDTO params) throws IOException {
        Resource resource = null;
        log.info("上传文件[{}]到本地", resource);
        String filename = resource.getFilename();
        String filePath = basePath + File.separator + filename;
        Path path = Paths.get(filePath);
        if (Files.notExists(path)) Files.createFile(path);
        FileCopyUtils.copy(resource.getInputStream(), Files.newOutputStream(path));
        //return new FileVO(filename, filePath);
        return null;
    }


}
