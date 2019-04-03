package summary;

import ms.Person;

/*
* 编码实现线程安全的单例模式
* */
public class Singleton {
    private static volatile Singleton instance;

    private Singleton(){};
//第一种方法：
/*    public synchronized static Singleton getInstance(){
        if(instance == null){
            instance = new Singleton();
        }
        return instance;
    }*/


/*
* 第二种方法：
* 实现代码块的同步锁
* */
    public static  Singleton getInstance() {

        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return  instance;
    }
/*
* 写主函数测试
* */
    public static void main(String[] args) {
          Singleton singleton1  =  Singleton.getInstance();

          Singleton singleton2  = Singleton.getInstance();


        if(singleton1.equals(singleton2))
        {
            System.out.println("singletonOne 和 singletonTwo 代表的是同一个实例");
        }else
        {
            System.out.println("singletonOne 和 singletonTwo 代表的是不同实例");
        }

    }
}
