package ms;




/*
* 单例-懒汉模式_演进版本2
* */
public class Person {
    private static Person bigPerson;
    private Person(){}
    //将方法加锁，线程获取锁之后，其他线程无法访问，
    public synchronized static Person getInstance(){
        if (bigPerson == null){
            bigPerson = new Person();
        }
        return bigPerson;
    }



}
