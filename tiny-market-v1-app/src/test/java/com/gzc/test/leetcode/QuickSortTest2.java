package com.gzc.test.leetcode;

import org.junit.Test;

public class QuickSortTest2 {


    @Test
    public void test(){

        int[] arr = {-1,5,8,1,87};

        quickSort(arr, 0, arr.length-1);

        for(int e : arr){
            System.out.print(e + " ");
        }

    }


    public static void quickSort(int[] arr, int left, int right){

        if(left < right){

            int idx = partition(arr, left, right);
            quickSort(arr, left , idx - 1);
            quickSort(arr, idx + 1, right);
        }

    }


    public static int partition(int[] arr, int left, int right){
        int pivot = arr[right];

        int i = left, j = right;

        while(i < j){
            while (i < j && arr[i] <= pivot) i++;
            while (i < j && arr[j] >= pivot) j--;

            if (i < j){
                swap(arr, i , j);
            }
        }

        swap(arr, i, right);
        return i;
    }


    public static void swap(int[] arr , int i , int j){
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }
}
