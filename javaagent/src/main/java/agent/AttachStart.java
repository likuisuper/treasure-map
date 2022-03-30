package agent;

import com.sun.tools.attach.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @Classname AttachStart
 * @Description 使用agentmain启动javaagent时配置的启动器
 * @Author likui
 * @Date 2021-11-06 15:50
 **/
public class AttachStart {
    public static void main(String[] args) throws IOException, AgentLoadException, AgentInitializationException, AttachNotSupportedException {
        //获取jvm进程列表
        List<VirtualMachineDescriptor> virtualMachineDescriptors = VirtualMachine.list();
        for (int i = 0; i < virtualMachineDescriptors.size(); i++) {
            System.out.println(String.format("[%s] %s",i,virtualMachineDescriptors.get(i).displayName()));
        }
        System.out.println("请输入指定要attach的进程");
        //选择JVM进程
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String readLine = bufferedReader.readLine();
        int i = Integer.parseInt(readLine);
        //附着agent
        VirtualMachine virtualMachine = VirtualMachine.attach(virtualMachineDescriptors.get(i));
        //传入agentmain方法所在类的jar包
        virtualMachine.loadAgent("F:\\github\\treasure-map\\javaagent\\target\\javaagent-1.0-SNAPSHOT.jar","hello");
        virtualMachine.detach();
        System.out.println("加载成功");
    }
}
