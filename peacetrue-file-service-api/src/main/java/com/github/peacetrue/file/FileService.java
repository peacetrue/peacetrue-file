package com.github.peacetrue.file;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;

/**
 * @author : xiayx
 * @since : 2020-11-23 10:19
 **/
public interface FileService {

    String getAbsolutePath(String path);

    /** 新增 */
    Mono<FileVO> add(FileAdd params);

    /** 新增 */
    Flux<FileVO> add(FilesAdd params);

    /** 分页查询 */
    Mono<Page<FileVO>> query(@Nullable FileQuery params, @Nullable Pageable pageable, String... projection);

    /** 全量查询 */
    Flux<FileVO> query(@Nullable FileQuery params, @Nullable Sort sort, String... projection);

    /** 全量查询 */
    default Flux<FileVO> query(FileQuery params, String... projection) {
        return this.query(params, (Sort) null, projection);
    }

    /** 获取 */
    Mono<FileVO> get(FileGet params, String... projection);

    /** 删除 */
    Mono<Integer> delete(FileDelete params);


}
