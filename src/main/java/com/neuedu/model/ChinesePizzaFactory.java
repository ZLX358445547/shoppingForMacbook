package com.neuedu.model;

public class ChinesePizzaFactory extends PizzaFactory {
    @Override
    public Pizza createPizza() {
        return  new ChinesePizza();
    }
}
