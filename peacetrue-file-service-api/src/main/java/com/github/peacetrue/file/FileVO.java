package com.github.peacetrue.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author : xiayx
 * @since : 2020-11-23 10:44
 **/
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FileVO implements Serializable {

    private String filename;
    private String filePath;

}
