package edu.upenn.cis.cis455.commonUtil;

import edu.upenn.cis.cis455.crawler.handlers.RegisterFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class CommonUtil {

    Logger logger = LogManager.getLogger(RegisterFilter.class);

    public static String encrypt(String rawPassword) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.reset();
        md.update(rawPassword.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        return String.format("%064x", new BigInteger(1, digest));
    }

    public static String getMD5(String document) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.reset();
        md.update(document.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        String hashtext = (new BigInteger(1,digest)).toString(16);
        StringBuilder sb = new StringBuilder(hashtext);
        return sb.toString();
    }

    public static String getCurrentTime() {
        return getTimeFormatter().format(ZonedDateTime.now(ZoneId.of("GMT")));
    }

    public static Date getDateFromString(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString, getTimeFormatter());
            return java.sql.Date.valueOf(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static DateTimeFormatter getTimeFormatter() {
        return DateTimeFormatter.RFC_1123_DATE_TIME;
    }

}
