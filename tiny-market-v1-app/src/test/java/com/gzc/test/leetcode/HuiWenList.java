package com.gzc.test.leetcode;

import org.junit.Test;

import java.util.Stack;

public class HuiWenList {


    @Test
    public void test(){

        String s = "123231";
        boolean huiWen = isHuiWen(s);
        System.out.println(huiWen);

    }

    public static boolean isHuiWen(String str){

        int len = str.length();
        Stack<Character> stack = new Stack<>();
        int i = 0;
        for (; i < len / 2 + 1; i++){
            stack.push(str.charAt(i));
        }
        if (i % 2 != 0){
            stack.pop();
        }
        for(; i < len; i++){
            Character cur = stack.pop();
            if (cur != str.charAt(i)){
                return false;
            }
        }

        return true;
    }
}
