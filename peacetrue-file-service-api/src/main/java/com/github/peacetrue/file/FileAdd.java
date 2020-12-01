package com.github.peacetrue.file;

import com.github.peacetrue.core.OperatorCapableImpl;
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
public class FileAdd extends OperatorCapableImpl<Long> {

    private static final long serialVersionUID = 0L;

    @Size(max = 255)
    private String relativePath;
    @NotNull
    private FilePart filePart;

    public FileAdd(FilePart filePart) {
        this.filePart = filePart;
    }
}