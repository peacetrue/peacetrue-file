package com.github.peacetrue.file;

import lombok.*;
import org.springframework.http.codec.multipart.FilePart;

import javax.validation.constraints.NotNull;


/**
 * @author xiayx
 */
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class FileAdd extends AbstractFileAdd {

    private static final long serialVersionUID = 0L;

    @NotNull
    private FilePart filePart;


}
