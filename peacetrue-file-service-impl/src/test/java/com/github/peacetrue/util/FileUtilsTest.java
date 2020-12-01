package com.github.peacetrue.util;

import com.github.peacetrue.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author : xiayx
 * @since : 2020-11-29 15:56
 **/
class FileUtilsTest {

    @Test
    void generateUniquePath() throws IOException {
        String sourceAbsolutePath = TestUtils.getSourceAbsolutePath(FileUtilsTest.class);
        Path path = FileFileUtils.generateUniquePath(Paths.get(sourceAbsolutePath + ".java"));
        Assertions.assertEquals(sourceAbsolutePath + "(1).java", path.toString());
        Files.createFile(path);

        Path path2 = FileFileUtils.generateUniquePath(Paths.get(sourceAbsolutePath + ".java"));
        Assertions.assertEquals(sourceAbsolutePath + "(2).java", path2.toString());
        Files.deleteIfExists(path);
    }

}
