package summary;

import java.util.Arrays;

/*
* 编码实现冒泡排序问题
* */
public class BubbleSort {
    public static void main(String[] args) {
            int[] array = {6,2,18,45,23,1};
            System.out.println("排序之前的数组：");
            System.out.println(Arrays.toString(array)+"");

        //===============================
        for (int i = 0;i<array.length-1;i++){
            for (int j = 0;j<array.length-i-1;j++){
                if (array[j]>array[j+1]){
                    int temp = array[j];
                    array[j] = array[j+1];
                    array[j+1] = temp;
                }
            }
        }
        //================================
        System.out.println("排序之后的数组为：");
        System.out.println(Arrays.toString(array));



    }

}
