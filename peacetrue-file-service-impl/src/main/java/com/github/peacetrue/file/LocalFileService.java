package com.github.peacetrue.file;

import com.github.peacetrue.spring.util.BeanUtils;
import com.github.peacetrue.util.FileUtils;
import com.github.peacetrue.util.StreamUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.data.domain.*;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
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

    private final String absoluteBasePath;
    private final Path absoluteBasePathObject;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public LocalFileService(String absoluteBasePath) {
        this.absoluteBasePath = Objects.requireNonNull(absoluteBasePath, "absoluteBasePath must not be null");
        this.absoluteBasePathObject = Paths.get(absoluteBasePath);
        if (Files.notExists(this.absoluteBasePathObject)) {
            throw new IllegalArgumentException(String.format("absoluteBasePath[%s] not exists, create it first", absoluteBasePath));
        }
    }

    @Override
    public String getAbsolutePath(String path) {
        return FileUtils.concat(absoluteBasePath, path);
    }

    /**
     * replaceFileName("a/b/c.txt","d")="a/b/c-1.txt"
     */
    public static String indexFileName(String filePath, Object index) {
        return filePath.replaceFirst("([^/\\\\]+)(\\.[^.]+)$", String.format("$1-%s$2", index));
    }

    public static void main(String[] args) {
        System.out.println(indexFileName("a/b/c.txt", 1));
    }

    @Override
    public Mono<FileVO> add(FileAdd params) {
        log.info("新增单个文件[{}]", params);
        String relativePath = params.getRelativePath() == null
                ? FileUtils.buildDateRelativePath()
                : FileUtils.formatRelativePath(params.getRelativePath());
        String fileName = params.getFileName() == null
                ? params.getFilePart().filename()
                : params.getFileName();
        return Mono.fromCallable(() -> {
            //TODO 并发问题，从探测文件存在到创建出文件
            Path absoluteFilePath = absoluteBasePathObject.resolve(relativePath).resolve(fileName);
            if (!Boolean.TRUE.equals(params.getOverwrite()))
                absoluteFilePath = FileUtils.buildNewPath(absoluteFilePath);
            if (Files.notExists(absoluteFilePath)) {
                Files.createDirectories(absoluteFilePath.getParent());
                Files.createFile(absoluteFilePath);
            }
            return absoluteFilePath;
        })
                .doOnNext(absoluteFilePath -> params.getFilePart().transferTo(absoluteFilePath))
                .flatMap(absoluteFilePath -> map(absoluteFilePath, absoluteFilePath.toString().substring(absoluteBasePath.length() + 1)))
                .doOnNext(vo -> eventPublisher.publishEvent(new PayloadApplicationEvent<>(vo, params)));
    }

    @Override
    public Flux<FileVO> add(FilesAdd params) {
        log.info("新增多个文件[{}]", params);
        String relativePath = params.getRelativePath() == null
                ? FileUtils.buildDateRelativePath()
                : FileUtils.formatRelativePath(params.getRelativePath());
        return Mono.fromCallable(() -> Files.createDirectories(absoluteBasePathObject.resolve(relativePath)))
                .flatMapMany(absoluteFolderPath ->
                        Flux.fromArray(params.getFilePart())
                                .index((aLong, filePart) -> Tuples.of(aLong + params.getBaseIndex(), filePart))
                                .flatMap(tuple2 ->
                                        Mono.fromCallable(() -> {
                                            String fileName = params.getFileName() == null
                                                    ? tuple2.getT2().filename()
                                                    : indexFileName(params.getFileName(), tuple2.getT1());
                                            Path absoluteFilePath = absoluteFolderPath.resolve(fileName);
                                            if (!Boolean.TRUE.equals(params.getOverwrite()))
                                                absoluteFilePath = FileUtils.buildNewPath(absoluteFilePath);
                                            return Files.notExists(absoluteFilePath)
                                                    ? Files.createFile(absoluteFilePath)
                                                    : absoluteFilePath;
                                        }).zipWith(Mono.just(tuple2.getT2()))
                                )
                                .flatMap(tuple2 -> tuple2.getT2().transferTo(tuple2.getT1()).thenReturn(tuple2))
                                .flatMap(tuple2 -> map(tuple2.getT1(), tuple2.getT1().toString().substring(absoluteBasePath.length() + 1)))
                );
    }

    @Override
    public Mono<Page<FileVO>> query(@Nullable FileQuery params, @Nullable Pageable pageable, String... projection) {
        log.info("分页查询文件");
        FileQuery finalParams = params == null ? FileQuery.DEFAULT : params;
        Pageable finalPageable = pageable == null ? PageRequest.of(0, 10) : pageable;
        Sort sort = Sort.by(Sort.Direction.DESC, "folder")
                .and(finalPageable.getSortOr(Sort.by(Sort.Direction.ASC, "name")));
        boolean noPath = finalParams.getPath() == null || finalParams.getPath().length == 0;
        Path absolutePath = noPath ? absoluteBasePathObject : absoluteBasePathObject.resolve(finalParams.getPath()[0]);
        return Mono.fromCallable(() -> Files.list(absolutePath))
                .flatMapMany(Flux::fromStream)
                .flatMap(path -> map(path, path.toString().substring(absoluteBasePath.length() + 1)))
                .sort(sort(sort))
                .filter(fileVO -> filter(fileVO, finalParams))
                .skip(finalPageable.getOffset())
                .limitRequest(finalPageable.getPageSize())
                .reduce(new ArrayList<>(), StreamUtils.reduceToCollection())
                .zipWith(Mono.fromCallable(() -> Files.list(absolutePath).count()))
                .map(zip -> new PageImpl<>(zip.getT1(), finalPageable, zip.getT2()));
    }

    @SuppressWarnings("unchecked")
    private static Comparator<FileVO> sort(Sort sort) {
        return (t1, t2) -> {
            for (Sort.Order order : sort) {
                Comparable t1Value = (Comparable<?>) BeanUtils.getPropertyValue(t1, order.getProperty());
                Comparable t2Value = (Comparable<?>) BeanUtils.getPropertyValue(t2, order.getProperty());
                int value = order.isAscending() ? t1Value.compareTo(t2Value) : t2Value.compareTo(t1Value);
                if (value != 0) return value;
            }
            return 0;
        };
    }

    public static boolean filter(FileVO fileVO, FileQuery query) {
//        if (query.getId() != null && !Arrays.asList(query.getId()).contains(fileVO.getId())) {
//            return false;
//        }
        if (query.getFolder() != null && !query.getFolder().equals(fileVO.getFolder())) {
            return false;
        }
        if (query.getName() != null && !fileVO.getName().contains(query.getName())) {
            return false;
        }
        if (query.getSizes() != null && !query.getSizes().contains(fileVO.getSizes())) {
            return false;
        }
        return true;
    }

    public static Mono<FileVO> map(Path absolutePath, String relativePath) {
        return Mono.fromCallable(() -> Files.readAttributes(absolutePath, BasicFileAttributes.class))
                .map(attributes -> new FileVO(relativePath, absolutePath.getFileName().toString(),
                        attributes.isDirectory(), attributes.size()));
    }

    @Override
    public Flux<FileVO> query(FileQuery params, @Nullable Sort sort, String... projection) {
        log.info("分页查询文件");
        FileQuery finalParams = params == null ? FileQuery.DEFAULT : params;
        if (sort == null) sort = Sort.by(Sort.Direction.DESC, "folder");
        boolean noPath = finalParams.getPath() == null || finalParams.getPath().length == 0;
        Path absolutePath = noPath ? absoluteBasePathObject : absoluteBasePathObject.resolve(finalParams.getPath()[0]);
        return Mono.fromCallable(() -> Files.list(absolutePath))
                .flatMapMany(Flux::fromStream)
                .flatMap(path -> map(path, path.toString().substring(absoluteBasePath.length() + 1)))
                .sort(sort(sort))
                .filter(fileVO -> filter(fileVO, finalParams))
                .limitRequest(100)
                ;

    }

    @Override
    public Mono<FileVO> get(FileGet params, String... projection) {
        String relativePath = FileUtils.formatRelativePath(params.getId());
        Path absolutePath = absoluteBasePathObject.resolve(relativePath);
        return map(absolutePath, relativePath);
    }

    @Override
    public Mono<Integer> delete(FileDelete params) {
        log.info("删除文件[{}]", params);
        String relativePath = FileUtils.formatRelativePath(params.getId());
        if (StringUtils.isEmpty(relativePath)) {
            log.warn("不允许删除根目录");
            return Mono.just(0);
        }
        Path absoluteFilePath = absoluteBasePathObject.resolve(relativePath);
        return Mono.fromCallable(() -> Files.isDirectory(absoluteFilePath)
                ? FileSystemUtils.deleteRecursively(absoluteFilePath)
                : Files.deleteIfExists(absoluteFilePath))
                .doOnNext(result -> {
                    if (!result) return;
                    eventPublisher.publishEvent(
                            new PayloadApplicationEvent<>(map(absoluteFilePath, relativePath), params)
                    );
                })
                .map(result -> result ? 1 : 0);
    }
}
