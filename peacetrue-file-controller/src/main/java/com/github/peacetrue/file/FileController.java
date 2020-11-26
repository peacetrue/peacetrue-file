package com.github.peacetrue.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ZeroCopyHttpOutputMessage;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

/**
 * @author : xiayx
 * @since : 2020-11-23 10:20
 **/
@Slf4j
@Controller
@RequestMapping("/files")
public class FileController {

    private FileService fileService;
    private Function<FilePart, Mono<? extends FileVO>> filePartHandler;

    @Autowired
    public void setFileService(FileService fileService) {
        this.fileService = fileService;
        this.filePartHandler = buildFilePartHandler(fileService);
    }

    public static Function<FilePart, Mono<? extends FileVO>> buildFilePartHandler(FileService fileService) {
        return filePart -> {
            String relativeFilePath = fileService.buildRelativeFilePath(filePart.filename());
            Path absoluteFilePath = Paths.get(fileService.getAbsoluteFilePath(relativeFilePath));
            Mono<Integer> transitionMono = Files.notExists(absoluteFilePath) ? Mono.fromCallable(() -> {
                Files.createDirectories(absoluteFilePath.getParent());
                Files.createFile(absoluteFilePath);
                return 0;
            }) : Mono.just(0);
            return transitionMono
                    .flatMap(ignored -> Mono.fromCallable(() -> Files.size(absoluteFilePath)))
                    .flatMap(fileSize -> filePart.transferTo(absoluteFilePath)
                            .thenReturn(new FileVO(filePart.filename(), relativeFilePath, fileSize))
                    );
        };
    }

    @ResponseBody
    @PostMapping(params = "fileCount=1")
    public Mono<FileVO> upload(@RequestPart("file") Mono<FilePart> files) {
        log.info("上传单个文件");
        return files.flatMap(filePartHandler);
    }

    @ResponseBody
    @PostMapping
    public Flux<FileVO> upload(@RequestPart("files") Flux<FilePart> files) {
        log.info("上传多个文件");
        return files.flatMap(filePartHandler);
    }

    @GetMapping("/{*filePath}")
    public Mono<Void> download(ServerHttpResponse response, @PathVariable String filePath) {
        return downloadLocalFile(response, fileService.getAbsoluteFilePath(filePath));
    }

    @GetMapping(value = "/{*filePath}", params = "type=preview")
    public Mono<Void> preview(ServerHttpResponse response, @PathVariable String filePath) {
        return previewLocalFile(response, fileService.getAbsoluteFilePath(filePath));
    }

    public static Mono<Void> downloadLocalFile(ServerHttpResponse response, String absoluteFilePath) {
        log.info("下载本地文件[{}]", absoluteFilePath);
        return writeLocalFile(response, "attachment", absoluteFilePath);
    }

    public static Mono<Void> previewLocalFile(ServerHttpResponse response, String absoluteFilePath) {
        log.info("预览本地文件[{}]", absoluteFilePath);
        return writeLocalFile(response, "inline", absoluteFilePath);
    }

    public static Mono<Void> writeLocalFile(ServerHttpResponse response, String dispositionType, String absoluteFilePath) {
        log.debug("输出本地文件[{}]", absoluteFilePath);
        File file = new File(absoluteFilePath);
        if (file.exists()) {
            ZeroCopyHttpOutputMessage zeroCopyResponse = (ZeroCopyHttpOutputMessage) response;
            String filename = Paths.get(absoluteFilePath).getFileName().toString();
            response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, dispositionType + "; filename=" + filename);
            response.getHeaders().setContentType(MediaType.parseMediaType(URLConnection.guessContentTypeFromName(filename)));
            return zeroCopyResponse.writeWith(file, 0, file.length());
        } else {
            log.warn("本地文件[{}]不存在", absoluteFilePath);
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return response.setComplete();
        }
    }

}
