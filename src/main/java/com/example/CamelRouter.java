package com.example;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.CsvDataFormat;
import java.util.List;

public class CamelRouter extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        getContext().setStreamCaching(true);
        CsvDataFormat csv = new CsvDataFormat();
        csv.setDelimiter(";");

        from("telegram:bots")
                .routeId("parse-message-from-telegram-users")
                .log("routeId:${routeId} body=${body}")
                .log("headers=${headers}")
                .setProperty("CamelMessageTimestamp", simple("${header.CamelMessageTimestamp}"))
                .setProperty("CamelTelegramChatId", simple("${header.CamelTelegramChatId}"))

                .choice().when(simple("${body} == '/test'"))
                    .to("direct:get-test-template-data")
                .endChoice()
                .otherwise()
                    .setBody(constant("Привет! Я - бот, который предназначен для создания напоминаний о подходящем сроке обучения по охране труда.\n" +
                            "Для проверки работоспособности на тестовых данных, напиши мне /test.\n" +
                            "Настройки напоминаний на реальных данных конфигурируются разработчиком"))
                .endChoice()
                .end()

//                .log("exchangeProperties11 = ${exchangeProperty}")
                .setHeader("CamelMessageTimestamp", simple("${exchangeProperty.CamelMessageTimestamp}"))
                .setHeader("CamelTelegramChatId", simple("${exchangeProperty.CamelTelegramChatId}"))

                .log("headers11=${headers}")
                .log("body11=${body}")
                .to("telegram:bots");


        from("direct:get-test-template-data")
                .routeId("get-test-template-data")
                .log("routeId:${routeId}")
//                .pollEnrich("file:src/main/resources/?fileName=График проверок знаний (редакция)utf-8.csv&noop=true&idempotent=false") // idempotent to allow re-read, no-op to keep the file untouched
                .pollEnrich("file:src/main/resources/?fileName={{test-csv-filename}}&noop=true&idempotent=false") // idempotent to allow re-read, no-op to keep the file untouched
//                .pollEnrich("file:src/main/resources/?fileName=test1.csv&noop=true&idempotent=false") // idempotent to allow re-read, no-op to keep the file untouched
//                .log("foo1 body=${body}")
                .unmarshal(csv)
                .bean(new ParserTable(), "findExpiringEmployees")
                .bean(new HumanReadableTextCreator(), "createHumanReadableText")

                //todo: идея: если не сегодня и не завтра дата очередной проверки, то послать текст о том, что приложение живое и прислать дату следующей очередной проверки

                .log("headers1=${headers}")
                .removeHeaders("CamelFile*")
                .log("headers2=${headers}")

                .log("body=${body}")
        ;


        //todo: идея: Слать в отдельный чатик для меня статусы о том, что приложение живое и количество напоминалок вообще и в этом месяце.

//        from("timer://foo1?fixedRate=true&period=200000")
//        from("timer://foo1?fixedRate=true&period=86400000") //todo: натравить таймер на определенное время  - ну к примеру, на 8 утра по Самаре
        from("timer://foo1?time={{first-time-execute-timer}}&fixedRate=true&period=86400000") //todo: натравить таймер на определенное время  - ну к примеру, на 8 утра по Самаре
                .to("direct:get-test-template-data")
                .setHeader("CamelTelegramChatId", simple("{{test.telegram.chatid}}")) //id Линара
                .to("telegram:bots")
        ;

//        from("file:src/main/resources/?fileName=График проверок знаний (редакция).csv&noop=true")
//                .unmarshal().csv()
//                .log(body())
//        ;

    }
}
