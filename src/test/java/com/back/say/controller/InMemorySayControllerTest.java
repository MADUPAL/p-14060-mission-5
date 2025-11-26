package com.back.say.controller;

import com.back.say.repository.InMemorySayRepository;
import com.back.say.repository.SayRepository;

public class InMemorySayControllerTest extends AbstractSayControllerTest{

    @Override
    protected SayRepository createRepository() {
        return new InMemorySayRepository();
    }
}
