package agent.jvmtool;

import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * @Author likui
 * @Date 2021-11-06 15:34
 **/
public class Agent {
  public static Instrumentation instrumentation;
    public static void agentmain(String args, Instrumentation instrumentation) {
                // 启动远程服务
        Agent.instrumentation=instrumentation;
        try {
            startRmiService(Integer.parseInt(args));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startRmiService(int port) throws RemoteException, AlreadyBoundException, MalformedURLException {
        ToolServiceImpl userService =new ToolServiceImpl();
        LocateRegistry.createRegistry(port);
        Naming.bind("rmi://localhost:"+port+"/ToolService", userService);
        System.out.println("rmi 已启动："+port);
    }

}
