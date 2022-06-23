package com.example;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HumanReadableTextCreator {
    private Logger log = LoggerFactory.getLogger(getClass());


    public Object createHumanReadableText(Exchange exchange) {

        List<Map<String, String>> upcomingEmployeeList = (List<Map<String, String>>) exchange.getIn().getBody();

        // Сортируем по дате истекания
        Collections.sort(upcomingEmployeeList, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                LocalDate o1DateExpire = null;
                LocalDate o2DateExpire = null;
                try {
                    o1DateExpire = LocalDate.of(Integer.parseInt(o1.get("year expire")), Integer.parseInt(o1.get("month expire")), Integer.parseInt(o1.get("day expire")));

                } catch (NumberFormatException | DateTimeException e) {
                    log.info("Произошла ошибка парсина даты у сотрудника " + o1.toString());
                }
                try {
                    o2DateExpire = LocalDate.of(Integer.parseInt(o2.get("year expire")), Integer.parseInt(o2.get("month expire")), Integer.parseInt(o2.get("day expire")));
                } catch (NumberFormatException | DateTimeException e) {
                    log.info("Произошла ошибка парсина даты у сотрудника " + o2.toString());
                }
                return o1DateExpire.compareTo(o2DateExpire);
            }
        });


        StringBuffer botAnswer = new StringBuffer("");
        for (int i = 0; i < upcomingEmployeeList.size(); i++) {
            Map<String, String> upcomingEmployee = upcomingEmployeeList.get(i);
            LocalDate dateExpire = null;
            try {
                dateExpire = LocalDate.of(Integer.parseInt(upcomingEmployee.get("year expire")), Integer.parseInt(upcomingEmployee.get("month expire")), Integer.parseInt(upcomingEmployee.get("day expire")));
            } catch (NumberFormatException | DateTimeException e) {
                log.info("Произошла ошибка парсина даты у сотрудника " + upcomingEmployee.toString());
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String dateExpireString = dateExpire.format(formatter);

            botAnswer.append(String.format(dateExpireString + " у \"%s\" на должности \"%s\" по теме \"%s - %s\" наступает срок очередной проверки знаний\n\n", upcomingEmployee.get("name"), upcomingEmployee.get("profession"), upcomingEmployee.get("theme"), upcomingEmployee.get("tolerance group")));

        }
        if (!botAnswer.toString().isEmpty())
            return botAnswer;
        else {
            return "В ближайшее время нет очередных проверок знаний";
        }
    }
}
