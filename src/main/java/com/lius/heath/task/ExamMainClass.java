package com.lius.heath.task;

import org.apache.commons.lang.StringUtils;

public class ExamMainClass {

    public static void main(String[] args) {
        String str = "192.168.1.1,126.3.3.3";
        int i = StringUtils.countMatches(str, ".");
        System.out.println();

    }
}
