package com.calo.cmpp.service;

import com.calo.cmpp.enums.MobileType;
import com.calo.cmpp.enums.OperateType;
import org.apache.commons.lang3.StringUtils;

import java.util.Random;

public class RandomPhoneNumber {

    private static final Random random = new Random();

    //中国移动
    private static final String[] CHINA_MOBILE = {
            "134", "135", "136", "137", "138", "139", "150", "151", "152", "157", "158", "159",
            "182", "183", "184", "187", "188", "178", "147", "172", "198"
    };
    //中国联通
    private static final String[] CHINA_UNICOM = {
            "130", "131", "132", "145", "155", "156", "166", "171", "175", "176", "185", "186", "166"
    };
    //中国电信
    private static final String[] CHINA_TELECOME = {
            "133", "149", "153", "173", "177", "180", "181", "189", "199"
    };

    private static String createMobile(int mobileType) {
        StringBuilder sb = new StringBuilder();
        String mobile01;//手机号前三位
        int temp;
        mobile01 = switch (mobileType) {
            case 1 -> CHINA_MOBILE[random.nextInt(CHINA_MOBILE.length)];
            case 2 -> CHINA_UNICOM[random.nextInt(CHINA_UNICOM.length)];
            case 3 -> CHINA_TELECOME[random.nextInt(CHINA_TELECOME.length)];
            default -> "op标志位有误！";
        };
        if (mobile01.length() > 3) {
            return mobile01;
        }
        sb.append(mobile01);
        //生成手机号后8位
        for (int i = 0; i < 8; i++) {
            temp = random.nextInt(10);
            sb.append(temp);
        }
        return sb.toString();
    }

    public static String generateMobile(MobileType operator) {
        int op;
        if (MobileType.RAND.equals(operator)) {
            op = random.nextInt(1, 4);
        } else {
            op = operator.getValue();
        }
        return createMobile(op);
    }


}
