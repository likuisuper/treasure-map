package agent;

import java.io.IOException;

/**
 * @Classname com.cxylk.agent.MyApp
 * @Description TODO
 * @Author likui
 * @Date 2021-11-06 15:34
 **/
public class MyApp {
    public static void main(String[] args) throws IOException {
        System.out.println("main");
        new HelloWorld().hello();
//        System.in.read();
    }
}
