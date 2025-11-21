package com.example.lecture;

import com.oanda.v20.Context;
import com.oanda.v20.account.AccountSummary;
import com.oanda.v20.pricing.ClientPrice;
import com.oanda.v20.pricing.PricingGetRequest;
import com.oanda.v20.pricing.PricingGetResponse;
import com.oanda.v20.instrument.Candlestick;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import com.oanda.v20.instrument.InstrumentCandlesResponse;
import com.oanda.v20.primitives.InstrumentName;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.order.MarketOrderRequest;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderCreateResponse;
import com.oanda.v20.primitives.DecimalNumber;
import com.oanda.v20.trade.Trade;
import com.oanda.v20.trade.TradeCloseRequest; // Import TradeCloseRequest
import com.oanda.v20.trade.TradeSpecifier; // Import TradeSpecifier

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@Controller
public class LectureApplication {

    public static void main(String[] args) {
        SpringApplication.run(LectureApplication.class, args);
    }

    // Controller for the home page
    @GetMapping("/")
    public String home() {
        return "home"; // Renders src/main/resources/templates/home.html
    }

    @GetMapping("/forex-account")
    public String getForexAccountInfo(Model model) {
        Context ctx = new Context(Config.URL, Config.TOKEN);
        try {
            AccountSummary summary = ctx.account.summary(Config.ACCOUNTID).getAccount();
            model.addAttribute("accountSummary", summary);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error fetching account data: " + e.getMessage());
        }
        return "account_info"; // Renders a new account_info.html template
    }

    @GetMapping("/forex-actprice")
    public String showActualPricesForm(Model model) {
        model.addAttribute("messageActPrice", new MessageActPrice());
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
                String[] currencies = instrument.split("_");
                String baseCurrency = currencies.length > 0 ? currencies[0] : "N/A";
                String quoteCurrency = currencies.length > 1 ? currencies[1] : "N/A";
                String bidPrice = clientPrice.getBids().get(0).getPrice().toString();
                String askPrice = clientPrice.getAsks().get(0).getPrice().toString();
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

    @GetMapping("/forex-histprice")
    public String showHistoricalPricesForm(Model model) {
        model.addAttribute("messageHistPrice", new MessageHistPrice());
        List<String> instruments = Arrays.asList("EUR_USD", "USD_JPY", "GBP_USD", "USD_CHF", "AUD_USD", "NZD_USD");
        List<String> granularities = Arrays.stream(CandlestickGranularity.values())
                                            .map(Enum::name)
                                            .collect(Collectors.toList());
        model.addAttribute("instruments", instruments);
        return "form_hist_prices";
    }

    @PostMapping("/forex-histprice")
    public String getHistoricalPrices(@ModelAttribute MessageHistPrice messageHistPrice, Model model) {
        Context ctx = new Context(Config.URL, Config.TOKEN);
        String instrument = messageHistPrice.getInstrument();
        String granularityStr = messageHistPrice.getGranularity();
        String historicalPriceInfo = "Could not retrieve historical prices.";

        try {
            CandlestickGranularity granularity = CandlestickGranularity.valueOf(granularityStr);
            InstrumentCandlesRequest request = new InstrumentCandlesRequest(new InstrumentName(instrument));
            request.setGranularity(granularity);
            request.setCount(10L);

            InstrumentCandlesResponse resp = ctx.instrument.candles(request);

            if (resp.getCandles() != null && !resp.getCandles().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Last 10 Historical Prices for ").append(instrument).append(" (").append(granularityStr).append("):<br>");
                sb.append("<ul>");
                for (Candlestick candle : resp.getCandles()) {
                    sb.append("<li>")
                      .append("Time: ").append(candle.getTime());
                    if (candle.getMid() != null) {
                        sb.append(", Close (Mid): ").append(candle.getMid().getC().toString());
                    } else {
                        sb.append(", (Price data not available for this candle)");
                    }
                    sb.append("</li>");
                }
                sb.append("</ul>");
                historicalPriceInfo = sb.toString();
            } else {
                historicalPriceInfo = "No historical price data found for " + instrument + " with granularity " + granularityStr;
            }
        } catch (Exception e) {
            e.printStackTrace();
            historicalPriceInfo = "Error fetching historical prices for " + instrument + ": " + e.getMessage();
        }

        model.addAttribute("selectedInstrument", instrument);
        model.addAttribute("selectedGranularity", granularityStr);
        model.addAttribute("historicalPriceInfo", historicalPriceInfo);
        return "result_hist_prices";
    }

    @GetMapping("/forex-open")
    public String showOpenPositionForm(Model model) {
        model.addAttribute("messageOpenPosition", new MessageOpenPosition());
        List<String> instruments = Arrays.asList("EUR_USD", "USD_JPY", "GBP_USD", "USD_CHF", "AUD_USD", "NZD_USD");
        model.addAttribute("instruments", instruments);
        return "form_open_position";
    }

    @PostMapping("/forex-open")
    public String openPosition(@ModelAttribute MessageOpenPosition messageOpenPosition, Model model) {
        Context ctx = new Context(Config.URL, Config.TOKEN);
        String instrument = messageOpenPosition.getInstrument();
        int units = messageOpenPosition.getUnits();
        String resultMessage;

        try {
            MarketOrderRequest marketOrderRequest = new MarketOrderRequest();
            marketOrderRequest.setInstrument(new InstrumentName(instrument));
            marketOrderRequest.setUnits(new DecimalNumber(units));

            OrderCreateRequest orderCreateRequest = new OrderCreateRequest(Config.ACCOUNTID);
            orderCreateRequest.setOrder(marketOrderRequest);

            OrderCreateResponse response = ctx.order.create(orderCreateRequest);
            
            resultMessage = "Successfully opened position for " + units + " units of " + instrument + ".<br>" +
                            "Transaction ID: " + response.getOrderFillTransaction().getId();

        } catch (Exception e) {
            e.printStackTrace();
            resultMessage = "Error opening position for " + instrument + ": " + e.getMessage();
        }

        model.addAttribute("resultMessage", resultMessage);
        return "result_open_position";
    }

    @GetMapping("/forex-pos")
    public String getOpenPositions(Model model) {
        Context ctx = new Context(Config.URL, Config.TOKEN);
        try {
            List<Trade> openTrades = ctx.trade.listOpen(Config.ACCOUNTID).getTrades();
            model.addAttribute("openTrades", openTrades);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error fetching open positions: " + e.getMessage());
        }
        return "open_positions";
    }

    @GetMapping("/forex-close")
    public String showClosePositionForm(Model model) {
        model.addAttribute("messageClosePosition", new MessageClosePosition());
        return "form_close_position";
    }

    @PostMapping("/forex-close")
    public String closePosition(@ModelAttribute MessageClosePosition messageClosePosition, Model model) {
        Context ctx = new Context(Config.URL, Config.TOKEN);
        String tradeId = messageClosePosition.getTradeId();
        String resultMessage;

        try {
            ctx.trade.close(new TradeCloseRequest(Config.ACCOUNTID, new TradeSpecifier(tradeId)));
            resultMessage = "Successfully closed position with Trade ID: " + tradeId;
        } catch (Exception e) {
            e.printStackTrace();
            resultMessage = "Error closing position with Trade ID " + tradeId + ": " + e.getMessage();
        }

        model.addAttribute("resultMessage", resultMessage);
        return "result_close_position";
    }
}
