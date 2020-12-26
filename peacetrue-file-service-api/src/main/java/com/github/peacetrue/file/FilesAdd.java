package com.github.peacetrue.file;

import lombok.*;
import org.springframework.http.codec.multipart.FilePart;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


/**
 * @author xiayx
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FilesAdd extends AbstractFileAdd {

    /** 基础索引名 */
    @Size(max = 32)
    private int baseIndex = 0;
    @NotNull
    private FilePart[] filePart;

}
