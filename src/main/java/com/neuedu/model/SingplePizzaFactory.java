package com.neuedu.model;

public class SingplePizzaFactory {



    public Pizza createPizza(int type ){
        if (type==0){
            //AmericanPizza
            return new AmericanPizza();
        }else if(type ==1){
            return new ChinesePizza();
        }
        return null;
    }

    public static void main(String[] args) {
            SingplePizzaFactory pizzaFactory = new SingplePizzaFactory();
            Pizza pizza = pizzaFactory.createPizza(1);
        System.out.println(pizza);
    }
}
