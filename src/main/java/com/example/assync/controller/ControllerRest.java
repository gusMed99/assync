package com.example.assync.controller;


import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
public class ControllerRest {

    public static final int ESPERA = 50;

    @PostMapping(value = "/ids", produces = "application/json")
    public List<String> getListOfIds(@RequestBody final Integer listSize){
        System.out.println("--------------");
        List<String> emailsList = this.getListInputList(listSize);
        System.out.println("RC");
        Long ini = System.currentTimeMillis();
        List<String> assyncResult = this.emailsListToIdListAssync(emailsList);
        Long fim = System.currentTimeMillis();
        System.out.println("Assync: "+ (fim - ini));
        ini = System.currentTimeMillis();
        List<String> synResult = this.emailsListToIdListSync(emailsList);
        fim = System.currentTimeMillis();
        System.out.println("Sync: "+(fim- ini));
        return assyncResult;
    }

    private List<String> getListInputList(Integer listSize){
        List<String> resultList = new ArrayList<>();
        for(int i=0; i< listSize;i++){
            resultList.add("email "+i);
        }
        return resultList;
    }

    private List<String> emailsListToIdListSync(List<String> emailsList){
        System.out.println("---");
        System.out.println("Sync");
        return emailsList.stream().map(item -> this.emailToId(item, ESPERA)).collect(Collectors.toList());
    }

    @SneakyThrows
    private List<String> emailsListToIdListAssync(List<String> emailsList){
        System.out.println("----");
        System.out.println("Assync");
        return emailsList.stream()
                .map(item ->
                        CompletableFuture.supplyAsync(() ->
                                this.emailToId(item, ESPERA)))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(), completableFutureList ->
                                completableFutureList.stream()
                                        .map(CompletableFuture::join)
                ))
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private String emailToId(String email, int espera){
        System.out.println("Recebida chamada "+email);
        Thread.sleep(espera);
        System.out.println("Depois do sleep "+email);
        return UUID.nameUUIDFromBytes(email.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
