package com.example.lecture;

import com.oanda.v20.Context;
import com.oanda.v20.account.AccountSummary;
import com.oanda.v20.pricing.ClientPrice;
import com.oanda.v20.pricing.PricingGetRequest;
import com.oanda.v20.pricing.PricingGetResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;

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

    @GetMapping("/forex-actprice")
    public String showActualPricesForm(Model model) {
        model.addAttribute("messageActPrice", new MessageActPrice());
        // Add a list of common instruments for the dropdown
        List<String> instruments = Arrays.asList("EUR_USD", "USD_JPY", "GBP_USD", "USD_CHF", "AUD_USD", "NZD_USD");
        model.addAttribute("instruments", instruments);
        return "form_actual_prices";
    }

    @PostMapping("/forex-actprice")
    public String getActualPrice(@ModelAttribute MessageActPrice messageActPrice, Model model) {
        Context ctx = new Context(Config.URL, Config.TOKEN);
        String instrument = messageActPrice.getInstrument();
        String priceInfo = "Could not retrieve price.";

        try {
            List<String> instrumentsList = Arrays.asList(instrument);
            PricingGetRequest request = new PricingGetRequest(Config.ACCOUNTID, instrumentsList);
            PricingGetResponse resp = ctx.pricing.get(request);

            if (resp.getPrices() != null && !resp.getPrices().isEmpty()) {
                ClientPrice clientPrice = resp.getPrices().get(0);

                // Extract base and quote currencies
                String[] currencies = instrument.split("_");
                String baseCurrency = currencies.length > 0 ? currencies[0] : "N/A";
                String quoteCurrency = currencies.length > 1 ? currencies[1] : "N/A";

                // Get bid and ask prices and convert PriceValue to String
                String bidPrice = clientPrice.getBids().get(0).getPrice().toString();
                String askPrice = clientPrice.getAsks().get(0).getPrice().toString();

                // Rephrase for simpler understanding
                priceInfo = "For " + instrument + " (1 " + baseCurrency + " equals " + quoteCurrency + "):<br>" +
                            "You can **sell** 1 " + baseCurrency + " for " + bidPrice + " " + quoteCurrency + ".<br>" +
                            "You can **buy** 1 " + baseCurrency + " for " + askPrice + " " + quoteCurrency + ".<br>" +
                            "Last updated: " + clientPrice.getTime();
            } else {
                priceInfo = "No price data found for " + instrument;
            }
        } catch (Exception e) {
            e.printStackTrace();
            priceInfo = "Error fetching price for " + instrument + ": " + e.getMessage();
        }

        model.addAttribute("selectedInstrument", instrument);
        model.addAttribute("actualPrice", priceInfo);
        return "result_actual_prices";
    }
}
