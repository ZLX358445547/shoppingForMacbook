package com.neuedu.model;

public class EnglandPizzaFactory extends PizzaFactory {
    @Override
    public Pizza createPizza() {
        return  new EnglandPizza();
    }
}
