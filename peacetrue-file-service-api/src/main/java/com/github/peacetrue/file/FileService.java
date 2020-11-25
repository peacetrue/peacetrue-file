package com.github.peacetrue.file;

import java.io.IOException;

/**
 * @author : xiayx
 * @since : 2020-11-23 10:19
 **/
public interface FileService {

    /**
     * 根据文件名构建相对文件路径
     *
     * @param filename 文件名，例如：a.txt
     * @return 相对文件路径，例如：2020/11/20/a.txt
     */
    String buildRelativeFilePath(String filename);

    /**
     * 根据上传文件相对路径获取文件绝对路径
     *
     * @param relativeFilePath 相对文件路径，{@link #buildRelativeFilePath(String)} 返回的路径
     * @return 绝对文件路径，例如：/Users/zhangsan/2020/11/20/a.txt
     */
    String getAbsoluteFilePath(String relativeFilePath);


    /** 上传文件，此接口未确定 */
    FileVO upload(FileUploadDTO params) throws IOException;

}
