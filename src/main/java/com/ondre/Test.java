package com.ondre;

import java.util.Arrays;

public class Test {
    public static int removeDuplicates(int[] nums) {
        int k=1, i=0;
        int[] tmpNums = new int[nums.length];
        tmpNums[0]=nums[0];
        while(i<nums.length-1){
            if(nums[i]!=nums[i+1]){
                tmpNums[k]=nums[i+1];
                k++;
                System.out.println(Arrays.toString(tmpNums));
            }
            i++;
        }
        nums=tmpNums;
        System.out.println(Arrays.toString(nums));

        return k;
    }
    public static void main(String[] args) {
        int[] arr = {1,1,2};
        System.out.println(removeDuplicates(arr));
    }
}
