package com.github.peacetrue.file;

import com.github.peacetrue.core.OperatorCapableImpl;
import com.github.peacetrue.core.Range;
import lombok.*;


/**
 * @author xiayx
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FileQuery extends OperatorCapableImpl<Long> {

    public static final FileQuery DEFAULT = new FileQuery();

    private static final long serialVersionUID = 0L;

    /** 主键 */
    private String[] id;
    /** 名称 */
    private String name;
    /** 大小 */
    private Range.Long sizes;
    /** 是否目录 */
    private Boolean folder;

    public FileQuery(String[] id) {
        this.id = id;
    }

    public String[] getPath() {
        return id;
    }
}
