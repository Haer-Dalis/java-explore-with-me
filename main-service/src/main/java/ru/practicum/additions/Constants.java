package ru.practicum.additions;

import java.time.format.DateTimeFormatter;

public class Constants {
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final String DEFAULT_FROM = "0";

    public static final String DEFAULT_SIZE = "10";

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

}
