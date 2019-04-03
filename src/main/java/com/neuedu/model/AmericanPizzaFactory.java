package com.neuedu.model;

public class AmericanPizzaFactory  extends PizzaFactory{
    @Override
    public Pizza createPizza() {
        return new AmericanPizza();
    }
}
