package summary;

public class BinarySearch {
    public static int binaryserch (int srcArray[],int start,int end,int key){
        int middle = (start+end)/2 + start;
        if (srcArray[middle]==key){
            return  middle;
        }
        if (start >=end){
            return -1;
        }else if (key > srcArray[middle]){
            return binaryserch(srcArray,middle+1,end,key);
        }else if (key < srcArray[middle]){
            return binaryserch(srcArray,start,end-1,key);
        }
        return -1;
    }

}
