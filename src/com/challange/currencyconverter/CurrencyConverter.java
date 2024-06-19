package com.challange.currencyconverter;

import com.challange.currencyconverter.exceptions.CurrencyConversionException;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class CurrencyConverter {

    private static final String API_KEY = "b6f16ffc8908c42079c399b7";
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/";

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_RED = "\u001B[31m";

    private static final List<String> conversionHistory = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String fromCurrency = "BRL";
        String toCurrency;
        double amount;

        while (true) {
            printMenu();
            System.out.print("Selecione uma opção: ");
            int choice = scanner.nextInt();

            if (choice == 9) break;
            if (choice < 1 || choice > 8) {
                System.out.println(ANSI_RED + "Opção inválida, por favor selecione uma opção entre 1 e 8" + ANSI_RESET);
                continue;
            }

            switch (choice) {
                case 1: toCurrency = "USD"; break;
                case 2: toCurrency = "EUR"; break;
                case 3: toCurrency = "GBP"; break;
                case 4: toCurrency = "JPY"; break;
                case 5: toCurrency = "AUD"; break;
                case 6: toCurrency = "CAD"; break;
                case 7: toCurrency = "MXN"; break;
                case 8: printConversionHistory(); continue;
                default: continue;
            }

            System.out.print("Insira a quantidade em " + fromCurrency + ": ");
            amount = scanner.nextDouble();

            try {
                double rate = getExchangeRate(fromCurrency, toCurrency);
                double convertedAmount = amount * rate;
                String conversionResult = String.format("%.2f %s = %.2f %s", amount, fromCurrency, convertedAmount, toCurrency);
                System.out.println(ANSI_GREEN + conversionResult + ANSI_RESET);
                logConversion(amount, fromCurrency, convertedAmount, toCurrency);
                conversionHistory.add(conversionResult + "  " + getCurrentTime());
            } catch (CurrencyConversionException e) {
                System.out.println(ANSI_RED + "Erro ao obter a taxa de câmbio: " + e.getMessage() + ANSI_RESET);
            }
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println(ANSI_YELLOW + "======================================");
        System.out.println("    Conversor de Moedas (BRL)    ");
        System.out.println("======================================" + ANSI_RESET);
        System.out.println("1. BRL => USD");
        System.out.println("2. BRL => EUR");
        System.out.println("3. BRL => GBP");
        System.out.println("4. BRL => JPY");
        System.out.println("5. BRL => AUD");
        System.out.println("6. BRL => CAD");
        System.out.println("7. BRL => MXN");
        System.out.println("8. Ver histórico de conversões");
        System.out.println("9. Sair");
        System.out.println("======================================" + ANSI_RESET);
    }

    private static void printConversionHistory() {
        System.out.println(ANSI_BLUE + "Histórico de Conversões:" + ANSI_RESET);
        if (conversionHistory.isEmpty()) {
            System.out.println(ANSI_RED + "Não há conversões registradas" + ANSI_RESET);
        } else {
            for (String record : conversionHistory) {
                System.out.println(record);
            }
        }
    }

    private static double getExchangeRate(String fromCurrency, String toCurrency) throws CurrencyConversionException {
        try {
            String requestUrl = API_URL + fromCurrency;
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            Gson gson = new Gson();
            ExchangeRateResponse exchangeRateResponse = gson.fromJson(response.toString(), ExchangeRateResponse.class);
            if (!exchangeRateResponse.conversionRates.containsKey(toCurrency)) {
                throw new CurrencyConversionException("A taxa de conversão para " + toCurrency + " não está disponível.");
            }
            return exchangeRateResponse.conversionRates.get(toCurrency);
        } catch (Exception e) {
            throw new CurrencyConversionException(ANSI_RED + "Erro ao obter a taxa de câmbio" + ANSI_RESET);
        }
    }

    private static void logConversion(double amount, String fromCurrency, double convertedAmount, String toCurrency) {
        String logEntry = String.format(" %.2f %s = %.2f %s  %s",
                amount, fromCurrency, convertedAmount, toCurrency, getCurrentTime());
        System.out.println(ANSI_PURPLE + logEntry + ANSI_RESET);
    }

    private static String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    private static class ExchangeRateResponse {
        @SerializedName("conversion_rates")
        private Map<String, Double> conversionRates;
    }
}
