package com.example.lecture;

import com.oanda.v20.account.AccountID;

public class Config {
    private Config() {}

    public static final String URL = "https://api-fxpractice.oanda.com";
    public static final String TOKEN = "3c510fdd4cf14ddcd1030fee48b54e37-9ad0657320eaaa9b505eb42ab340c6c5"; // Replace with your Oanda API Token
    public static final AccountID ACCOUNTID = new AccountID("101-004-37740278-001"); // Replace with your Oanda Account ID
}
