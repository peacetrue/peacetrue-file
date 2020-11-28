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

    @GetMapping(value = "/{*filePath}")
    public Mono<Void> download(ServerHttpResponse response,
                               @PathVariable String filePath,
                               @RequestHeader(name = "X-Requested-With", required = false) String requestedWithHeader,
                               @RequestParam(name = "X-Requested-With", required = false) String requestedWithParam,
                               String type) {
        boolean isAjax = isAjax(requestedWithHeader) || isAjax(requestedWithParam);
        return downloadLocalFile(response, fileService.getAbsoluteFilePath(filePath), isAjax, type);
    }

    private static boolean isAjax(String requestedWith) {
        return "XMLHttpRequest".equals(requestedWith);
    }

    public static final String DOWNLOAD_TYPE_PREVIEW = "preview";
    public static final String DISPOSITION_TYPE_INLINE = "inline";
    public static final String DISPOSITION_TYPE_ATTACHMENT = "attachment";

    public static Mono<Void> downloadLocalFile(ServerHttpResponse response, String absoluteFilePath,
                                               boolean isAjax, String type) {
        log.info("下载本地文件[{}]", absoluteFilePath);
        String dispositionType = isAjax ? null : (
                DOWNLOAD_TYPE_PREVIEW.equals(type)
                        ? DISPOSITION_TYPE_INLINE
                        : DISPOSITION_TYPE_ATTACHMENT);
        return writeLocalFile(response, dispositionType, absoluteFilePath);
    }

    public static Mono<Void> writeLocalFile(ServerHttpResponse response, String dispositionType, String absoluteFilePath) {
        log.debug("输出本地文件[{}]", absoluteFilePath);
        File file = new File(absoluteFilePath);
        if (file.exists()) {
            ZeroCopyHttpOutputMessage zeroCopyResponse = (ZeroCopyHttpOutputMessage) response;
            String filename = Paths.get(absoluteFilePath).getFileName().toString();
            if (dispositionType != null) {
                response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, dispositionType + "; filename=" + filename);
            }
            String mediaType = URLConnection.guessContentTypeFromName(filename);
            if (mediaType != null) response.getHeaders().setContentType(MediaType.parseMediaType(mediaType));
            return zeroCopyResponse.writeWith(file, 0, file.length());
        } else {
            log.warn("本地文件[{}]不存在", absoluteFilePath);
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return response.setComplete();
        }
    }

}
