package com.cxylk.coverage;

import java.util.Objects;

/**
 * @author likui
 * @date 2022/9/2 下午4:05
 **/
public class Hello {
    public static final String a="xxx";

    //<init>
    public Hello() {
    }

    public void hi(String name,int d) {
        //var  4
        int i=1;//4
        int b=2;
        System.out.println(i);//4+1
        System.out.println(b);//5+1
        System.out.println(name);//
    }

    public void hello(String name) {
        //var  4
        int i=1;//4
        System.out.println(i);//4+1
        System.out.println(name);//
    }
}
