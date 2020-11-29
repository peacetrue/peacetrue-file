package com.github.peacetrue.util;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * //TODO merge to common {@link FileUtils}
 *
 * @author : xiayx
 * @since : 2020-11-29 15:55
 **/
public abstract class FileUtils {

    protected FileUtils() {
    }

    public static Path generateUniquePath(Path path) {
        if (Files.notExists(path)) return path;

        String fileName = path.getFileName().toString();
        //TODO handle no extension context
        int index = fileName.lastIndexOf('.');
        String name = fileName.substring(0, index),
                extension = fileName.substring(index + 1);
        int i = 0;
        do {
            //eg abc.zip
            String uniqueFileName = name + "(" + ++i + ")." + extension;
            path = path.resolveSibling(uniqueFileName);
        } while (Files.exists(path));
        return path;
    }

}
