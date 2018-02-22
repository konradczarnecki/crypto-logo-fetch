package com.konra.cryptologofetch;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class FetchService {

    private static String path = "/home/konrad/";

    RestTemplate rest;
    Map<String, String> logos;

    public FetchService() {
        rest = new RestTemplate();
        logos = new HashMap<>();
    }

    @PostConstruct
    public void init() throws IOException {

        CoinList list = rest.getForObject("https://api.coinmarketcap.com/v1/ticker/?limit=1000", CoinList.class);

        for(Coin c: list) {

            Document doc = Jsoup.connect("https://coinmarketcap.com/currencies/" + c.getId()).get();
            Elements logoEls = doc.select(".currency-logo-32x32");
            Element logo = logoEls.get(0);

            String src = logo.attr("src");
            String[] split = src.split("/");
            String filename = split[split.length-1];
            logos.put(c.getId(), filename);
        }

        saveToFile();
    }

    public void saveToFile() {

        Path jsFile = Paths.get(path, "map.js");

        try(BufferedWriter bw = Files.newBufferedWriter(jsFile, StandardOpenOption.CREATE_NEW)) {

            bw.write("export const logos = {");
            bw.newLine();
            int count = 0;

            for(Map.Entry<String, String> entry: logos.entrySet()) {

                String line = "  \"" + entry.getKey() + "\" : \"" + entry.getValue() + "\"";
                if(count != logos.entrySet().size() - 1) line += ",";

                bw.write(line);
                bw.newLine();
            }

            bw.write("}");

        }catch (Exception e) {

        }

        log.info("done");
    }
}
