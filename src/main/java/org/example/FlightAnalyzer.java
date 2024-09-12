package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


class Ticket {
    String origin;
    String destination;
    String departure_date;
    String departure_time;
    String arrival_date;
    String arrival_time;
    String carrier;
    int price;
}

class TicketCollection {
    List<Ticket> tickets;
}

public class FlightAnalyzer {

    public static void main(String[] args) {
        try {
            // Считываю файл tickets.json
            FileReader reader = new FileReader("src/main/resources/tickets.json");
            Gson gson = new Gson();
            Type ticketCollectionType = new TypeToken<TicketCollection>() {}.getType();
            TicketCollection ticketCollection = gson.fromJson(reader, ticketCollectionType);
            reader.close();

            // Фильтрацую билеты, оставляея те, что между Владивостоком и Тель-Авивом
            List<Ticket> vvoToTlvTickets = new ArrayList<>();
            for (Ticket t : ticketCollection.tickets) {
                if (t.origin.equals("VVO") && t.destination.equals("TLV")) {
                    vvoToTlvTickets.add(t);
                }
            }

            // Вычисляю минимальное время полета для каждого авиаперевозчика
            Map<String, Integer> minFlightTimes = new HashMap<>();
            for (Ticket ticket : vvoToTlvTickets) {
                int flightDuration = getFlightDuration(ticket.departure_date, ticket.departure_time, ticket.arrival_date, ticket.arrival_time);
                if (!minFlightTimes.containsKey(ticket.carrier) || flightDuration < minFlightTimes.get(ticket.carrier)) {
                    minFlightTimes.put(ticket.carrier, flightDuration);
                }
            }

            System.out.println("Минимальное время полета между Владивостоком и Тель-Авивом для каждого авиаперевозчика:");
            for (Map.Entry<String, Integer> entry : minFlightTimes.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue() + " минут");
            }

            // Вычисляю среднюю цену
            int totalPrice = 0;
            for (Ticket ticket : vvoToTlvTickets) {
                totalPrice += ticket.price;
            }
            double avgPrice = totalPrice / (double) vvoToTlvTickets.size();

            // Вычисляю медиану
            List<Integer> sortedPrices = new ArrayList<>();
            for (Ticket ticket : vvoToTlvTickets) {
                sortedPrices.add(ticket.price);
            }
            Collections.sort(sortedPrices);

            double medianPrice;
            int count = sortedPrices.size();
            if (count % 2 == 0) {
                medianPrice = (sortedPrices.get(count / 2 - 1) + sortedPrices.get(count / 2)) / 2.0;
            } else {
                medianPrice = sortedPrices.get(count / 2);
            }

            double priceDifference = avgPrice - medianPrice;

            System.out.println("\nРазница между средней ценой и медианой: " + String.format("%.2f", priceDifference) + " рублей");

        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
        }
    }

    // Метод для расчета времени полета
    public static int getFlightDuration(String departureDate, String departureTime, String arrivalDate, String arrivalTime) {
        SimpleDateFormat dateTimeFormat1 = new SimpleDateFormat("dd.MM.yy H:mm");
        SimpleDateFormat dateTimeFormat2 = new SimpleDateFormat("dd.MM.yy HH:mm");
        Date departure = null, arrival = null;

        try {
            departure = dateTimeFormat1.parse(departureDate + " " + departureTime);
        } catch (ParseException e) {
            try {
                departure = dateTimeFormat2.parse(departureDate + " " + departureTime);
            } catch (ParseException ex) {
                System.out.println("Ошибка даты отправления: " + ex.getMessage());
            }
        }

        try {
            arrival = dateTimeFormat1.parse(arrivalDate + " " + arrivalTime);
        } catch (ParseException e) {
            try {
                arrival = dateTimeFormat2.parse(arrivalDate + " " + arrivalTime);
            } catch (ParseException ex) {
                System.out.println("Ошибка даты прибытия: " + ex.getMessage());
            }
        }

        if (departure != null && arrival != null) {
            long duration = arrival.getTime() - departure.getTime();
            return (int) (duration / (1000 * 60));
        } else {
            return 0;
        }
    }
}
