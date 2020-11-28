package com.github.peacetrue.util;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

/**
 * @author : xiayx
 * @since : 2020-11-28 04:45
 **/
public class DateTimeFormatterUtils2 extends DateTimeFormatterUtils {

    /** formatter: yyyy/MM/dd */
    public static DateTimeFormatter SEPARATOR_DATE_TIME = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(YEAR)
            .appendLiteral(File.separatorChar)
            .append(MONTH)
            .appendLiteral(File.separatorChar)
            .append(DATE)
            .toFormatter();

}
