package com.opensource.dada.r2db;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@org.springframework.stereotype.Controller("/")
public class Controller {

    @GetMapping
    @ResponseBody
    public String hello() {
        return "hello";
    }

}
