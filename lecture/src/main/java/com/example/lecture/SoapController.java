package com.example.lecture;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import soapclient.MNBArfolyamServiceSoap;
import soapclient.MNBArfolyamServiceSoapGetExchangeRatesStringFaultFaultMessage;
import soapclient.MNBArfolyamServiceSoapImpl;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SoapController {

    @GetMapping("/soap")
    public String soap1(Model model) {
        model.addAttribute("param", new MessagePrice());
        return "form";
    }

    @PostMapping("/soap")
    public String soap2(@ModelAttribute MessagePrice messagePrice, Model model) throws
            MNBArfolyamServiceSoapGetExchangeRatesStringFaultFaultMessage {
        MNBArfolyamServiceSoapImpl impl = new MNBArfolyamServiceSoapImpl();
        MNBArfolyamServiceSoap service = impl.getCustomBindingMNBArfolyamServiceSoap();
        String xml = service.getExchangeRates(messagePrice.getStartDate(), messagePrice.getEndDate(), messagePrice.getCurrency());

        var parsed = parseMnbExchangeRates(xml, messagePrice.getCurrency());

        var dates = new ArrayList<String>();
        var values = new ArrayList<String>();
        for (var e : parsed) {
            dates.add(e.date);
            values.add(e.value);
        }

        model.addAttribute("currency", messagePrice.getCurrency());
        model.addAttribute("rates", parsed);
        model.addAttribute("dates", dates);
        model.addAttribute("values", values);

        return "result";
    }

    public static class RateEntry {
        public String date;
        public String value;
        public RateEntry(String d, String v){ date=d; value=v; }
    }

    private List<RateEntry> parseMnbExchangeRates(String xml, String currency) {
        List<RateEntry> list = new ArrayList<>();
        if (xml == null || xml.isBlank()) {
            return list;
        }

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(xml)));

            NodeList dayNodes = doc.getElementsByTagName("Day");
            for (int i = 0; i < dayNodes.getLength(); i++) {
                Element day = (Element) dayNodes.item(i);
                String date = day.getAttribute("date");

                NodeList rates = day.getElementsByTagName("Rate");
                for (int j = 0; j < rates.getLength(); j++) {
                    Element rate = (Element) rates.item(j);
                    if (currency.equalsIgnoreCase(rate.getAttribute("curr"))) {
                        String value = rate.getTextContent().trim();
                        String valueForChart = value.replace(",", "."); // convert to float
                        list.add(new RateEntry(date, valueForChart));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
