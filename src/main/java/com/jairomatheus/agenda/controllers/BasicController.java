package com.jairomatheus.agenda.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class BasicController {

    @GetMapping("/")
    public String getRootPage() {
        return """
            <style>
                body{
                    background-color: red;
                    color: white;
                }
            </style>

            <body>
                <div>TESTANDO ESSA BAGAÇÃ<div>
            </body>
        """;
            
    }
}
