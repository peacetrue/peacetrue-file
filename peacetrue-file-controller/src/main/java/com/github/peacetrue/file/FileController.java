package com.github.peacetrue.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ZeroCopyHttpOutputMessage;
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

    @Autowired
    private FileService fileService;

    @ResponseBody
    @PostMapping
    public Mono<FileVO> add(Mono<FileAdd> mono) {
        log.info("上传单个文件");
        return mono.flatMap(param -> fileService.add(param));
    }

    @ResponseBody
    @PostMapping(params = "type=multiple")
    public Flux<FileVO> addFiles(Mono<FilesAdd> mono) {
        log.info("上传多个文件");
        return mono.flatMapMany(filesAdd -> fileService.add(filesAdd));
    }

    @ResponseBody
    @GetMapping(params = "page")
    public Mono<Page<FileVO>> query(FileQuery params, Pageable pageable, String... projection) {
        log.info("分页查询文件信息[{}]", params);
        return fileService.query(params, pageable, projection);
    }

    @ResponseBody
    @GetMapping
    public Flux<FileVO> query(FileQuery params, Sort sort, String... projection) {
        log.info("全量查询文件信息(请求方法+参数变量)[{}]", params);
        return fileService.query(params, sort, projection);
    }

    @ResponseBody
    @GetMapping("/{*id}")
    public Mono<FileVO> getByUrlPathVariable(@PathVariable String id, String... projection) {
        log.info("获取文件信息(请求方法+路径变量)详情[{}]", id);
        return fileService.get(new FileGet(id), projection);
    }

    @ResponseBody
    @RequestMapping("/get")
    public Mono<FileVO> getByPath(FileGet params, String... projection) {
        log.info("获取文件信息(请求路径+参数变量)详情[{}]", params);
        return fileService.get(params, projection);
    }

    @ResponseBody
    @DeleteMapping("/{*id}")
    public Mono<Integer> deleteByUrlPathVariable(@PathVariable String id) {
        log.info("删除文件信息(请求方法+URL路径变量)[{}]", id);
        return fileService.delete(new FileDelete(id));
    }

    @ResponseBody
    @DeleteMapping(params = "id")
    public Mono<Integer> deleteByUrlParamVariable(FileDelete params) {
        log.info("删除文件信息(请求方法+URL参数变量)[{}]", params);
        return fileService.delete(params);
    }

    @ResponseBody
    @RequestMapping(path = "/delete")
    public Mono<Integer> deleteByPath(FileDelete params) {
        log.info("删除文件信息(请求路径+URL参数变量)[{}]", params);
        return fileService.delete(params);
    }

    @GetMapping(value = "/{*filePath}", params = "dispositionType")
    public Mono<Void> download(ServerHttpResponse response,
                               @PathVariable String filePath,
                               @RequestHeader(name = "X-Requested-With", required = false) String requestedWithHeader,
                               @RequestParam(name = "X-Requested-With", required = false) String requestedWithParam,
                               String dispositionType) {
        boolean isAjax = isAjax(requestedWithHeader) || isAjax(requestedWithParam);
        return downloadLocalFile(response, fileService.getAbsolutePath(filePath), isAjax, dispositionType);
    }

    private static boolean isAjax(String requestedWith) {
        return "XMLHttpRequest".equals(requestedWith);
    }

    public static final String DOWNLOAD_TYPE_PREVIEW = "preview";
    public static final String DISPOSITION_TYPE_INLINE = "inline",
            DISPOSITION_TYPE_ATTACHMENT = "attachment";

    public static Mono<Void> downloadLocalFile(ServerHttpResponse response, String absoluteFilePath,
                                               boolean isAjax, String dispositionType) {
        log.info("下载本地文件[{}]", absoluteFilePath);
        return writeLocalFile(response, isAjax ? null : dispositionType, absoluteFilePath);
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
