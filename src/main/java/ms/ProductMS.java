package ms;
/*
* 线程同步问题
* 例如：秒杀商品问题
* 解决库存超卖；秒杀问题中的高并发问题
* */
public class ProductMS {
/*
* 线程不同步比线程同步效率高！
* 线程什么时候应该同步，什么时候应该不同步？
* 多线程访问同一个对象，需要线程同步
* */

    private  int stock =10000;//原始库存

    private  int stockSkill=0;//9900+stockSkill=10000


    /*
    * 秒杀商品的方法
    * 10000个线程
    */
    /*
    * 第一种方式：加synchronized 实现线程同步
    * */
    public synchronized void skill(){

        if (this.stockSkill<100){
            try {
                //线程睡眠
                Thread.sleep(10);
                this.stock--;
                this.stockSkill++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        //第二种:代码块中加入线程同步锁
        //this 表示要锁的对象，这里指的是productMS
        /*synchronized (this){
            if (this.stockSkill<100){
                try {
                    //线程睡眠
                    Thread.sleep(10);
                    this.stock--;
                    this.stockSkill++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }*/

    }




    /*
    * 用户，相当于一个线程
    * 启动线程的方法：1，实现runnable接口   2，继承thread类
    * */
    static class Customer implements Runnable{

        //引用productms
        ProductMS productMS;

        public Customer(ProductMS productMS){
            this.productMS = productMS;
        }

        @Override
        public void run() {
            //调用秒杀的方法
            productMS.skill();


        }
    }

    //主函数

    public static void main(String[] args) {

         //要将商品资源放在for循环外面，才能实现多个用户访问一个资源
         ProductMS productMS = new ProductMS();
        //假设有10000个用户去抢100个商品
        for ( int i=0;i<10000;i++){
            //创建一个线程
            Thread thread = new Thread(new Customer(productMS));
            //调用start方法并不是线程立刻启动，而是一个就绪状态，告诉cpu可以进行调用
            thread.start();
        }
        //主线程睡眠，主线程是main方法的线程
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //加入同步之前剩余库存9861秒杀的商品数139
        //System.out.println("加入同步之前"+"剩余库存"+productMS.stock+"秒杀的商品数"+productMS.stockSkill);
        //加入同步之后剩余库存9900秒杀的商品数100
        System.out.println("加入同步之后"+"剩余库存"+productMS.stock+"秒杀的商品数"+productMS.stockSkill);
    }

}
