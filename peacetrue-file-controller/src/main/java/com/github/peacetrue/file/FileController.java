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

import javax.annotation.Nullable;
import java.io.File;
import java.net.URLConnection;
import java.nio.file.Paths;

/**
 * @author : xiayx
 * @since : 2020-11-23 10:20
 **/
@Slf4j
@Controller
@RequestMapping("/files")
public class FileController {

    private FileService fileService;

    @Autowired
    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }

    @ResponseBody
    @PostMapping(params = "fileCount=1")
    public Mono<FileVO> upload(@RequestPart("file") Mono<FilePart> files) {
        log.info("上传单个文件");
        return files.flatMap(filePart -> fileService.upload(filePart));
    }

    @ResponseBody
    @PostMapping(params = "fileCount!=1")
    public Flux<FileVO> upload(@RequestPart("files") Flux<FilePart> files) {
        log.info("上传多个文件");
        return files.flatMap(filePart -> fileService.upload(filePart));
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
    public static final String DISPOSITION_TYPE_INLINE = "inline",
            DISPOSITION_TYPE_ATTACHMENT = "attachment";

    public static Mono<Void> downloadLocalFile(ServerHttpResponse response, String absoluteFilePath,
                                               boolean isAjax, String type) {
        log.info("下载本地文件[{}]", absoluteFilePath);
        String dispositionType = isAjax ? null : (
                DOWNLOAD_TYPE_PREVIEW.equals(type)
                        ? DISPOSITION_TYPE_INLINE
                        : DISPOSITION_TYPE_ATTACHMENT);
        return writeLocalFile(response, dispositionType, absoluteFilePath);
    }

    public static Mono<Void> writeLocalFile(ServerHttpResponse response, @Nullable String dispositionType, String absoluteFilePath) {
        return writeLocalFile(response, dispositionType, absoluteFilePath, Paths.get(absoluteFilePath).getFileName().toString());
    }

    public static Mono<Void> writeLocalFile(ServerHttpResponse response, @Nullable String dispositionType, String absoluteFilePath, String fileName) {
        log.debug("输出本地文件[{}]", absoluteFilePath);
        File file = new File(absoluteFilePath);
        if (file.exists()) {
            ZeroCopyHttpOutputMessage zeroCopyResponse = (ZeroCopyHttpOutputMessage) response;
            if (dispositionType != null) {
                response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, dispositionType + "; filename=" + fileName);
            }
            String mediaType = URLConnection.guessContentTypeFromName(fileName);
            if (mediaType != null) response.getHeaders().setContentType(MediaType.parseMediaType(mediaType));
            return zeroCopyResponse.writeWith(file, 0, file.length());
        } else {
            log.warn("本地文件[{}]不存在", absoluteFilePath);
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return response.setComplete();
        }
    }

}
