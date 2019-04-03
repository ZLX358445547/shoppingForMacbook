package com.neuedu.model;

public class TestFactory {

    public static void main(String[] args) {
        //生产中国口味的pizza
        //创建一个pizza工厂
        PizzaFactory pizzaFactory = new ChinesePizzaFactory();
        Pizza pizza = pizzaFactory.createPizza();
        System.out.println(pizza);
    }


    /*
    *添加一种英国口味pizza
    * 新建EnglandPizza类，继承Pizza类
    * 在新建EnglandPizzaFactory工厂，继承PizzaFactory类
    * 在测试类直接调用即可，不许要改动其他代码
     */

}
