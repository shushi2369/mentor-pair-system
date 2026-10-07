package com.mentorpair.util;

import com.mentorpair.common.BusinessException;

public final class TextUtil {

    private TextUtil() {
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static String blankToNull(String s) {
        return isBlank(s) ? null : s.trim();
    }

    /** 字段长度校验：值非空且超长时抛出面向用户的业务异常 */
    public static void ensureLen(String label, String value, int max) {
        if (value != null && value.trim().length() > max) {
            throw new BusinessException(label + "不能超过 " + max + " 字");
        }
    }
}
