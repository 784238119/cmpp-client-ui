package com.calo.cmpp.constant;

public class CmppConstant {

    public final static String MAC_PATH = System.getProperty("user.home") + "/Library/Containers/com.calo.cmpp/Data";

    public final static String WIN_PATH = System.getProperty("user.home") + "/AppData/Local/com.calo.cmpp/Data";

    public final static String ACCOUNT_DB_NAME = "account.db";

    public static String getAccountDbPath() {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith("win")) {
            return WIN_PATH + "/" + ACCOUNT_DB_NAME;
        } else {
            return MAC_PATH + "/" + ACCOUNT_DB_NAME;
        }
    }
}
