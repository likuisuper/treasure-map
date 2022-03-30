package agent.jvmtool;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @Author likui
 * @Date 2021-11-06 15:34
 **/
public interface ToolService  extends Remote {

    //  查找类
    String findClassName(String name) throws RemoteException;

    String findMethod(String name) throws RemoteException;

    //  反编译类
    String jadClass(String className) throws RemoteException;
}
