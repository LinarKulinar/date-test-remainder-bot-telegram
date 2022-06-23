package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.camel.Exchange;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParserTable {
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * В методе захардкожено имя столбца и его индекс в таблице
     *
     * @return
     */
    private Map<String, Integer> initColumnNameMap() {
        // Todo: Move code to yml or json config file
        Map<String, Integer> columnNameMap = new HashMap<String, Integer>();
        columnNameMap.put("name", 1);
        columnNameMap.put("profession", 2);
        columnNameMap.put("theme", 3);
        columnNameMap.put("tolerance group", 4);
        columnNameMap.put("day expire", 9);
        columnNameMap.put("month expire", 10);
        columnNameMap.put("year expire", 11);
        return columnNameMap;
    }


    public Object findExpiringEmployees(Exchange exchange) {
        LocalDate currentDate = LocalDate.now(); // получаем текущую дату

        Map<String, Integer> columnNameMap = initColumnNameMap();

        List<Map<String, String>> upcomingEmployeeList = new ArrayList<>();

        String lastName = "";
        String lastProfession = "";
        List<List<String>> exchangeBody = (List<List<String>>) exchange.getIn().getBody();
//        log.info(exchangeBody.toString());

        for (int i = 0; i < exchangeBody.size(); i++) {
            List<String> line = exchangeBody.get(i);

            // Create a Map with employee fields based on information from columnNameMap
            Map<String, String> employeeMap = new HashMap<>();
            for (Map.Entry<String, Integer> entry : columnNameMap.entrySet()) {
                employeeMap.put(entry.getKey(), line.get(entry.getValue()));
            }

            // Remember last encountered field and recover if necessary
            if (!employeeMap.get("name").isEmpty()) {
                lastName = employeeMap.get("name");
            } else {
                employeeMap.put("name", lastName);
            }
            if (!employeeMap.get("profession").isEmpty()) {
                lastProfession = employeeMap.get("profession");
            } else {
                employeeMap.put("profession", lastProfession);
            }

            try {
                int dayExpireIntoLine = Integer.parseInt(employeeMap.get("day expire"));
                int monthExpireIntoLine = Integer.parseInt(employeeMap.get("month expire"));
                int yearExpireIntoLine = Integer.parseInt(employeeMap.get("year expire"));

                LocalDate dateExpireIntoLine = LocalDate.of(yearExpireIntoLine, monthExpireIntoLine, dayExpireIntoLine);
                log.info(String.format("Correct line: %s|%s|%s.%s.%s", lastName, lastProfession, dayExpireIntoLine, monthExpireIntoLine, yearExpireIntoLine));
                int rangeOfDays = 5;  // Сколько ближайших дней нас интересует
                if (dateExpireIntoLine.isAfter(currentDate.minusDays(1)) && dateExpireIntoLine.isBefore(currentDate.plusDays(rangeOfDays))) {
                    log.info("In line " +  i + ", we found an employee whose date is expiring. His name is \"" + employeeMap.get("name") +"\".");
                    upcomingEmployeeList.add(employeeMap);
                }
            } catch (NumberFormatException | DateTimeException e) {
                log.info(String.format("Invalid line: %s|%s|%s.%s.%s", lastName, lastProfession, line.get(9), line.get(10), line.get(11)));
            }
            //todo: написать сравнение с текущей датой (не забыть обработать ошибки выпарсивания дат)


            //todo: Если дата в файле и сегодняшняя дата совпадают, то положить в проперти флаг о том, что сегодня дата очередной проверки
            //todo: Если дата в файле наступит завтра, то в другое проперти положить флаг

//            System.out.println(formatter.format(currentDate));
            if (i > 30) {
                log.error("В исходных данных > 30 строк. После 20 строки данные будут отброшены");
                break;
            }
        }


//        log.info("Received message: {}", exchange.getIn().getBody());

//        return "Why did you say \"" + message.replace("\"", "-") + "\"?";
//        exchange.setProperty("upcomingEmployeeList", upcomingEmployeeList);
        return upcomingEmployeeList;
//        return "tife, ya lublu Lenochku. Тише.";
    }
}
