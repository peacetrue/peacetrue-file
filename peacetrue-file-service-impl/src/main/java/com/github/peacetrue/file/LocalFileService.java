package com.github.peacetrue.file;

import com.github.peacetrue.util.DateTimeFormatterUtils2;
import com.github.peacetrue.util.FileUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.util.FileSystemUtils;
import reactor.core.publisher.Mono;

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
    private final String basePath;

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
    public Mono<FileVO> upload(FilePart params) {
        String relativeFilePath = this.buildRelativeFilePath(params.filename());
        //TODO 并发问题，从探测文件存在到创建出文件
        Path absoluteFilePath = FileUtils.generateUniquePath(
                Paths.get(this.getAbsoluteFilePath(relativeFilePath))
        );

        return Mono.fromCallable(() -> {
            Files.createDirectories(absoluteFilePath.getParent());
            Files.createFile(absoluteFilePath);
            return 0;
        })
                .doOnNext(ignored -> params.transferTo(absoluteFilePath))
                .flatMap(ignored -> Mono.fromCallable(() -> Files.size(absoluteFilePath)))
                .map(fileSize -> {
                    String relativePath = absoluteFilePath.toString().substring(basePath.length() + 1);
                    return new FileVO(params.filename(), relativePath, fileSize);
                });
    }

    @Override
    public Mono<Boolean> delete(String relativeFilePath) {
        log.info("删除相对路径文件[{}]", relativeFilePath);
        return Mono.fromCallable(() -> {
            Path path = Paths.get(basePath).resolve(relativeFilePath);
            log.info("删除绝对路径文件[{}]", path);
            return Files.isDirectory(path)
                    ? FileSystemUtils.deleteRecursively(path)
                    : Files.deleteIfExists(path);
        });
    }
}
