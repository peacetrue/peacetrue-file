package com.github.peacetrue.file;

import com.github.peacetrue.core.OperatorCapableImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Size;

/**
 * @author : xiayx
 * @since : 2020-12-15 20:10
 **/
@Getter
@Setter
@ToString(callSuper = true)
public abstract class AbstractFileAdd extends OperatorCapableImpl<Long> {

    private static final long serialVersionUID = 0L;

    /** 相对目录 */
    @Size(max = 255)
    private String relativePath;
    /** 文件名 */
    @Size(max = 32)
    private String fileName;
    /** 覆盖已存在的文件，默认：不覆盖 */
    private Boolean overwrite;

    public void setFileName(String fileName) {
        this.fileName = fileName;
        if (overwrite == null) setOverwrite(true);
    }
}
