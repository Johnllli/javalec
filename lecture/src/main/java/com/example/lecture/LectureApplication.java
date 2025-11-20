package com.example.lecture;

import com.oanda.v20.Context;
import com.oanda.v20.account.AccountSummary;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@SpringBootApplication
@Controller
public class LectureApplication {

    public static void main(String[] args) {
        SpringApplication.run(LectureApplication.class, args);
    }

    @GetMapping("/forex-account")
    @ResponseBody
    public AccountSummary getForexAccountInfo() {
        Context ctx = new Context(Config.URL, Config.TOKEN);
        try {
            AccountSummary summary = ctx.account.summary(Config.ACCOUNTID).getAccount();
            return summary;
        } catch (Exception e) {
            e.printStackTrace();
            // You might want to return a more user-friendly error message or a specific error object
            return null;
        }
    }
}
