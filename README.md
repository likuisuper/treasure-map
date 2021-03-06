# treasure-map

## 1、javaagent插桩机制

对应代码为项目中的`javaagent`模块

## 2、ClassLoader问题

这部分中，我们简单的写了个查询数据库的方法，然后对该方法进行插桩，插桩的入口是`BaseExecutor`，方法是第二个query方法，也就是参数最多的那个方法。接下来分析其中遇到的问题：

#### BaseExecutor找不到

在第一个版本中，代码见`agent1`项目的`MybatisAgent1`类，然后在`boot-example`这个spring-boot项目中启动，此时会报`BaseExecutor`这个类找不到，原因是这样的：

~~~Java
ClassPool classPool=new ClassPool();
//加入系统路径
classPool.appendSystemPath();
~~~

新建了一个类池，并且给这个类池一个系统的路径来加载类，但是这个系统路径它加载的是rt，ext这些路径下的类，而我们导入的mybatis相关的jar包是通过appClassloader来加载的，我们知道，jvm中的类加载机制，父类是访问不了子类的，也就是访问不了`BaseExecutor`，解决的办法就是将加载它的类加载器也放入到类池路径下：

~~~Java
classPool.appendClassPath(new LoaderClassPath(loader));
~~~

这个loader就是transform方法的第一个参数loader。

#### BoundSql找不到

我们先来看下代码：

~~~Java
        public static SqlInfo begin(Object[] args){
            SqlInfo sqlInfo=new SqlInfo();
            sqlInfo.setBeginTime(System.currentTimeMillis());
            //获取执行的SQL,BoundSql是query的第6个参数（从0开始）
            BoundSql boundSql=(BoundSql)args[5];
            sqlInfo.setSql(boundSql.getSql());
            //获取SQL参数，query的第2个参数
            Object para=args[1];
            sqlInfo.setParameter(para);
            return sqlInfo;
        }
~~~

可以看到，我们通过插桩方法`query`的参数获取到了BoundSql，然后调用了它的getSql方法。

然后启动spring-boot应用，我们通过两种方式来启动：

##### 直接在idea里面启动

这种方式是不会报错的，因为这种方式的类加载结构如下：

![](https://s1.ax1x.com/2022/04/12/Lelj0g.png)

可以看到，在idea中是直接以`ApplicationClassLoader`来load的，此时agent.jar包和spring-boot应用是在同一层，所以这种方式是不会报错的。

##### 通过-jar的方式启动

我们在spring-boot应用的pom文件中加入以下依赖：

~~~xml
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>2.6.5</version>
<!--                解决jar包启动提示没有主清单属性的问题-->
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
~~~

这种方式打包和普通方式打包的不同可以看这篇文章：https://blog.csdn.net/yu102655/article/details/112490962

我们看打包后的jar包结构：

~~~
boot-example
├── BOOT-INF
│   ├── classes（我们写的程序）
│   │   ├── application.properties
│   │   └── com
│   │       └── cxylk
│   │           ├── BootExampleApplication.class
│   │           ├── bean
│   │           │   └── User.class
│   │           ├── controller
│   │           │   └── MyController.class
│   │           ├── dao
│   │           │   └── UserMapper.class
│   │           └── service
│   │               ├── UserService.class
│   │               └── UserServiceImpl.class
│   └── lib（当前程序依赖的第三方jar）
│       ├── HikariCP-3.4.5.jar
│       ├── jackson-annotations-2.13.2.jar
│       ├── jackson-core-2.13.2.jar
│       ├── jackson-databind-2.13.2.jar
│       ├── jackson-datatype-jdk8-2.13.2.jar
│       ├── jackson-datatype-jsr310-2.13.2.jar
│       ├── jackson-module-parameter-names-2.13.2.jar
│       ├── jakarta.annotation-api-1.3.5.jar
│       ├── jul-to-slf4j-1.7.36.jar
│       ├── log4j-api-2.17.2.jar
│       ├── log4j-to-slf4j-2.17.2.jar
│       ├── logback-classic-1.2.11.jar
│       ├── logback-core-1.2.11.jar
│       ├── mybatis-3.5.6.jar
│       ├── mybatis-spring-2.0.6.jar
│       ├── mybatis-spring-boot-autoconfigure-2.1.4.jar
│       ├── mybatis-spring-boot-starter-2.1.4.jar
│       ├── mysql-connector-java-8.0.15.jar
│       ├── protobuf-java-3.6.1.jar
│       ├── slf4j-api-1.7.30.jar
│       ├── snakeyaml-1.29.jar
│       ├── spring-aop-5.3.17.jar
│       ├── spring-beans-5.3.17.jar
│       ├── spring-boot-2.6.5.jar
│       ├── spring-boot-autoconfigure-2.6.5.jar
│       ├── spring-boot-jarmode-layertools-2.6.5.jar
│       ├── spring-context-5.3.17.jar
│       ├── spring-core-5.3.17.jar
│       ├── spring-expression-5.3.17.jar
│       ├── spring-jcl-5.3.17.jar
│       ├── spring-jdbc-5.3.1.jar
│       ├── spring-tx-5.3.1.jar
│       ├── spring-web-5.3.17.jar
│       ├── spring-webmvc-5.3.17.jar
│       ├── tomcat-embed-core-9.0.60.jar
│       ├── tomcat-embed-el-9.0.60.jar
│       └── tomcat-embed-websocket-9.0.60.jar
├── META-INF（应用相关的元信息）
│   ├── MANIFEST.MF
│   └── maven
│       └── com.cxylk
│           └── boot-example
│               ├── pom.properties
│               └── pom.xml
└── org（jar加载和启动相关的类）
    └── springframework
        └── boot
            └── loader
                ├── ClassPathIndexFile.class
                ├── ExecutableArchiveLauncher.class
                ├── JarLauncher.class
                ├── LaunchedURLClassLoader$DefinePackageCallType.class
                ├── LaunchedURLClassLoader$UseFastConnectionExceptionsEnumeration.class
                ├── LaunchedURLClassLoader.class
                ├── Launcher.class
                ├── MainMethodRunner.class
                ├── PropertiesLauncher$1.class
                ├── PropertiesLauncher$ArchiveEntryFilter.class
                ├── PropertiesLauncher$ClassPathArchives.class
                ├── PropertiesLauncher$PrefixMatchingArchiveFilter.class
                ├── PropertiesLauncher.class
                ├── WarLauncher.class
                ├── archive
                │   ├── Archive$Entry.class
                │   ├── Archive$EntryFilter.class
                │   ├── Archive.class
                │   ├── ExplodedArchive$AbstractIterator.class
                │   ├── ExplodedArchive$ArchiveIterator.class
                │   ├── ExplodedArchive$EntryIterator.class
                │   ├── ExplodedArchive$FileEntry.class
                │   ├── ExplodedArchive$SimpleJarFileArchive.class
                │   ├── ExplodedArchive.class
                │   ├── JarFileArchive$AbstractIterator.class
                │   ├── JarFileArchive$EntryIterator.class
                │   ├── JarFileArchive$JarFileEntry.class
                │   ├── JarFileArchive$NestedArchiveIterator.class
                │   └── JarFileArchive.class
                ├── data
                │   ├── RandomAccessData.class
                │   ├── RandomAccessDataFile$1.class
                │   ├── RandomAccessDataFile$DataInputStream.class
                │   ├── RandomAccessDataFile$FileAccess.class
                │   └── RandomAccessDataFile.class
                ├── jar
                │   ├── AbstractJarFile$JarFileType.class
                │   ├── AbstractJarFile.class
                │   ├── AsciiBytes.class
                │   ├── Bytes.class
                │   ├── CentralDirectoryEndRecord$1.class
                │   ├── CentralDirectoryEndRecord$Zip64End.class
                │   ├── CentralDirectoryEndRecord$Zip64Locator.class
                │   ├── CentralDirectoryEndRecord.class
                │   ├── CentralDirectoryFileHeader.class
                │   ├── CentralDirectoryParser.class
                │   ├── CentralDirectoryVisitor.class
                │   ├── FileHeader.class
                │   ├── Handler.class
                │   ├── JarEntry.class
                │   ├── JarEntryCertification.class
                │   ├── JarEntryFilter.class
                │   ├── JarFile$1.class
                │   ├── JarFile$JarEntryEnumeration.class
                │   ├── JarFile.class
                │   ├── JarFileEntries$1.class
                │   ├── JarFileEntries$EntryIterator.class
                │   ├── JarFileEntries$Offsets.class
                │   ├── JarFileEntries$Zip64Offsets.class
                │   ├── JarFileEntries$ZipOffsets.class
                │   ├── JarFileEntries.class
                │   ├── JarFileWrapper.class
                │   ├── JarURLConnection$1.class
                │   ├── JarURLConnection$JarEntryName.class
                │   ├── JarURLConnection.class
                │   ├── StringSequence.class
                │   └── ZipInflaterInputStream.class
                ├── jarmode
                │   ├── JarMode.class
                │   ├── JarModeLauncher.class
                │   └── TestJarMode.class
                └── util
                    └── SystemPropertyUtils.class
~~~

spring boot使用了这种**FatJar**技术将所有依赖放在一个最终的jar包文件BOOT-INF/lib中，把当前项目的class全部放在BOOT-INF/classes目录中。并且在lib中可以看到`tomcat-embed-core-9.0.60.jar`这个Tomcat的启动包，spring boot内置的Tomcat就是这样加载到项目中的。

我们再来看下MANIFEST文件中的内容：

~~~
Manifest-Version: 1.0
Spring-Boot-Classpath-Index: BOOT-INF/classpath.idx
Archiver-Version: Plexus Archiver
Built-By: likui
Spring-Boot-Layers-Index: BOOT-INF/layers.idx
Start-Class: com.cxylk.BootExampleApplication
Spring-Boot-Classes: BOOT-INF/classes/
Spring-Boot-Lib: BOOT-INF/lib/
Spring-Boot-Version: 2.6.5
Created-By: Apache Maven 3.8.4
Build-Jdk: 1.8.0_311
Main-Class: org.springframework.boot.loader.JarLauncher
~~~

这里面有两个重要参数：

1、Start-Class:

它是应用自己的main函数，这个参数对应的类才是真正的业务main函数入口

2、Main-Class:

它是org.springframework.boot.loader.JarLauncher，它是这个jar包启动的main函数，**它负责创建LaunchedURLClassLoader来加载/lib下面所有的jar包**。我们可以看下相关的核心代码，就在上面解压后的loader目录下：

~~~Java
    public static void main(String[] args) throws Exception {
        (new JarLauncher()).launch(args);
    }
~~~

最终会调用到基类`Launcher`中的`launch`方法：

~~~Java
    protected void launch(String[] args) throws Exception {
        if (!this.isExploded()) {
            JarFile.registerUrlProtocolHandler();
        }

        ClassLoader classLoader = this.createClassLoader(this.getClassPathArchivesIterator());
        String jarMode = System.getProperty("jarmode");
        String launchClass = jarMode != null && !jarMode.isEmpty() ? "org.springframework.boot.loader.jarmode.JarModeLauncher" : this.getMainClass();
        this.launch(args, launchClass, classLoader);
    }
~~~

该方法分为三步：

（1）注册URL协议并清除应用缓存

（2）创建类加载器

~~~Java
    protected ClassLoader createClassLoader(URL[] urls) throws Exception {
        return new LaunchedURLClassLoader(isExploded(), getArchive(), urls, getClass().getClassLoader());
    }
~~~

可以发现这里创建了一个新的classloader，也就是**LaunchedURLClassLoader**，然后将classload加入thread的context中：

~~~Java
protected void launch(String[] args, String launchClass, ClassLoader classLoader) throws Exception {
        Thread.currentThread().setContextClassLoader(classLoader);
        createMainMethodRunner(launchClass, args, classLoader).run();
    }
~~~

LaunchedURLClassLoader继承了URLClassloader，并且实现了loadClass方法，这其实又回到了双亲委派机制，最后让Application Classloader来load。这里的第二个参数也就是要加载的类通过下面这个方法得到：

~~~Java
    protected String getMainClass() throws Exception {
        Manifest manifest = this.archive.getManifest();
        String mainClass = null;
        if (manifest != null) {
            mainClass = manifest.getMainAttributes().getValue("Start-Class");
        }

        if (mainClass == null) {
            throw new IllegalStateException("No 'Start-Class' manifest entry specified in " + this);
        } else {
            return mainClass;
        }
    }
~~~

可以看到，它就是定义的start-class，也就是我们实际在代码中启动的类

（3）执行main方法

在MainMethodRunner中，通过LauncherdURLClassLoader load并且通过反射调用了start-class参数对应的类中的main方法：

~~~Java
    public void run() throws Exception {
        Class<?> mainClass = Class.forName(this.mainClassName, false, Thread.currentThread().getContextClassLoader());
        Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
        mainMethod.setAccessible(true);
        mainMethod.invoke((Object)null, this.args);
    }
~~~

所以，通过FAT JAR的方式启动jar包，这时的类加载就是下面所示：

![](https://s1.ax1x.com/2022/04/12/Lmx6QP.png)

##### 原因

那么现在，我们就知道为什么通过jar的方式启动会提示找不到BoundSql了：

BoundSql是我们导入mybatis依赖才会有的，而mybatis依赖包在我们将应用打成jar包后，它位于BOOT-INF/lib下面（从上面的jar包结构中可以找到），由LaunchedURLClassLoader加载，而我们的agent jar包是由它的父类AppClassLoader来加载的，现在，agent jar包中的begin方法要去访问它的子类加载的mybatis相关类，当然会提示找不到，因为子类可以访问父类，但是父类是访问不了子类的！

#### 解决办法

两种解决办法：

1、将位于AppClassLoader这层的agent.jar包放入到下面的LaunchedURLClassLoader中：

~~~Java
    public void appendToLoader(ClassLoader loader) throws NoSuchMethodException, MalformedURLException, InvocationTargetException, IllegalAccessException {
        URLClassLoader urlClassLoader= (URLClassLoader) loader;
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        String path = MybatisAgent2.class.getResource("").getPath();//file:/Users/likui/Workspace/github/treasure-map/agent1/target/agent1-0.0.1-SNAPSHOT.jar!/com/cxylk/agent1/mybatis/
        path=path.substring(0,path.indexOf("!/"));
        //调用addURL方法将MybatisAgent2的类路径加入到URLClassloader中
        method.invoke(urlClassLoader,new URL(path));
    }
~~~

但是这种方式在Tomcat中可行，使用-jar的方式还是会报错

2、**通过适配器反射**

这种方式是最合适也是最推荐的，这样就不用担心到底是通过-jar的方式启动还是Tomcat方式启动，原理就是在父类加载器访问子类加载器的时候不是直接访问子类加载器，而是给父类添加一个适配器，然后通过反射的方式来访问子类加载器相关的类：

~~~Java
    public static class BoundSqlAdapter{
            private Object target;

            private Class aClass;

            private Method getSql;
						
            public BoundSqlAdapter(Object target){
                this.target=target;
                if(this.aClass==null){
                    init(target.getClass());
                }
            }

            private synchronized void init(Class clazz){
                try {
                    aClass=clazz;
                    getSql=clazz.getDeclaredMethod("getSql");
                    getSql.setAccessible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public String getSql(){
                try {
                    return (String) getSql.invoke(target);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
    }
~~~

将原来直接访问BoundSql的地方替换为BoundSqlAdapter，然后通过构造函数传入一个Object，比如要访问的是BoundSql，**那么这里就是一个以Object表示的BoundSql**，不能直接写BoundSql，不然还是会提示找不到。

## 3、HTTP协议监控

### 3.1、服务响应监控

#### 设计目标

http采集的目标：

1、保证稳定性：系统运行时，一直都可以采集数据

2、保证通用性：尽可能支持更多的http服务实现

3、实现简单：减少开发维护成本

#### 插桩采集点

http服务的实现整体是由Servlet容器提供的，如Tomcat，Jetty，Netty。

![](https://s1.ax1x.com/2022/04/24/L4IAsO.png)

Http服务实现分为两部分，一种是支持Servlet规范的，另一种是不支持Servlet规范的第三方实现，绝大部分业务都是基于第一种实现的，所以不支持Servlet规范的基本先不考虑。在Servlet规范这边，又划分成MVC框架层，Servlet层，容器层，除了中间层Servlet层其它两层的实现都是多样的，所以**为了保证通用性在Servlet层插桩是最佳选择**。比如使用MVC框架，DispatchServlet也是实现HttpServlet的，最终还是调用的HttpServlet。

#### 采集数据

1、URL路径

2、客户端IP

3、请求完整用时

4、请求参数

5、异常信息

#### ClassLoader问题

在begin方法中，需要获取service方法的第一个参数`HttpServletRequest`得到URL等信息，这样在使用jar包启动时任然会报错，和上面分析的一样。

我们先来看Tomcat的类加载器结构：

![](https://z3.ax1x.com/2021/08/31/hapsBj.png)

* Common通用类加载器加载Tomcat使用以及应用通用的一些类，位于CATALINA_HOME/lib下，**比如servlet-api.jar**。

* Catalina ClassLoader ⽤于加载服务器内部可⻅类，这些类应⽤程序不能访问
* SharedClassLoader ⽤于加载应⽤程序共享类，这些类服务器不会依赖
* WebappClassLoader，每个应⽤程序都会有⼀个独⼀⽆⼆的Webapp ClassLoader，他⽤来加载本应⽤程序 /WEB-INF/classes 和 /WEB-INF/lib 下的类

前面分析过，agent.jar是AppClassLoader加载的，而servlet jar包是Tomcat的Common类加载器加载的，它是AppClassLoader的子类，这样在启动web应用时，父类中的agent要访问子类下的servlet中相关的类，肯定是访问不到的（启动的是web应用，访问的类都是以web应用中的类为准）。

解决办法，仍然是通过适配器来解决，具体看代码agent1模块下的httpserver包下的HttpCollect类。

### 3.2、服务调用监控

与前面所说的Http响应进入到我们服务相反，现在要实现的是通过http调用别人的服务

#### Http API调用方式

##### 基于URL直接调用

##### 基于工具调用

##### 基于第三方平台提供的SDK来调用

![](https://s1.ax1x.com/2022/04/24/L4qxcn.png)

可以发现，Http调用实现并没有集中的点，只能针对这些框架一个个实现采集插桩，并且封装层在实际使用中并不多，主要还是实现层，所以根据通用程度先实现URL采集监控。

#### URL Http调用分析

基于URL http的调用，一般都是下面这段代码来实现：

~~~java
//构造一个URL
URL url = new URL("https://www.baidu.com");
//创建网络连接
URLConnection conn = url.openConnection();
//打开连接
conn.connect();
//读取流
InputStream input = conn.getInputStream();
byte[] bytes = IOUtils.readFully(input, -1, false);
System.out.println(new String(bytes));
~~~

这种方式很难一眼就能找到监控点，这时就必须深入URL的内部实现：

~~~java
    public URLConnection openConnection() throws java.io.IOException {
        return handler.openConnection(this);
    }
~~~

在建立网络连接中，是通过调用handler的openConnection方法完成的，那么这个handler是什么呢？

~~~java
transient URLStreamHandler handler;
~~~

它是一个协议处理器，来处理不同协议的URL，比如http，https，ftp等等，它是一个抽象类，不同的协议有不同的实现，而我们在第一步构造URL时传进来的url中会有具体的协议，然后在构造函数中就会得到对应协议的URLStreamHandler，这部分代码省略。所以我们来看https的实现，需要注意的是**这些处理器都叫Handler，只不过协议的不同，它的包前缀不同**，比如https的就是`sun.net.www.protocol.https`下的Handler，http的就是`sun.net.www.protocol.http`下的Handler。

~~~java
public class Handler extends sun.net.www.protocol.http.Handler {
    protected String proxy;
    protected int proxyPort;

    protected int getDefaultPort() {
        return 443;
    }

    public Handler() {
        this.proxy = null;
        this.proxyPort = -1;
    }

    public Handler(String var1, int var2) {
        this.proxy = var1;
        this.proxyPort = var2;
    }

    protected URLConnection openConnection(URL var1) throws IOException {
        return this.openConnection(var1, (Proxy)null);
    }

    protected URLConnection openConnection(URL var1, Proxy var2) throws IOException {
        return new HttpsURLConnectionImpl(var1, var2, this);
    }
}
~~~

https对应的Handler继承了http对应的Handler，可以看到，openConnection这个方法中返回的是HttpsURLConnectionImpl，它是HttpsURLConnection中的内部类，这里有点绕，我们直接看http协议对应的Handler：

~~~Java
package sun.net.www.protocol.http;

public class Handler extends URLStreamHandler {
    protected String proxy;
    protected int proxyPort;

    protected int getDefaultPort() {
        return 80;
    }

    public Handler() {
        this.proxy = null;
        this.proxyPort = -1;
    }

    public Handler(String var1, int var2) {
        this.proxy = var1;
        this.proxyPort = var2;
    }

    protected URLConnection openConnection(URL var1) throws IOException {
        return this.openConnection(var1, (Proxy)null);
    }

    protected URLConnection openConnection(URL var1, Proxy var2) throws IOException {
        return new HttpURLConnection(var1, var2, this);
    }
}
~~~

这个HttpURLConnection是`sun.net.www.protocol.http`包下的，但是它继承了`java.net`包下的HttpURLConnection，父类HttpURLConnection就是url.openConnection返回的Connection，但是建立连接connect这个方法还是在`sun.net.www.protocol.http`这个包下的HttpURLConnection来实现的。

我们来看下这个流程：

![](https://s1.ax1x.com/2022/04/24/L5SnRU.png)

所以我们只要在源头去控制处理器的生成，就可以从头到尾代理整个URL的处理过程，并获取性能数据。

![](https://s1.ax1x.com/2022/04/24/L5lJud.png)

构建处理器有下面几种方式，优先级由高到低

##### 基于自定义的URLStreamHandlerFactory

我们看下URL的构造函数就知道了：

~~~java
    public URL(String protocol, String host, int port, String file,
               URLStreamHandler handler) throws MalformedURLException {
      ...
        if (handler == null &&
            (handler = getURLStreamHandler(protocol)) == null) {
            throw new MalformedURLException("unknown protocol: " + protocol);
        }
        this.handler = handler;
    }
~~~

getURLStreamHandler方法会根据不同的协议得到对应的URLStreamHandler：

~~~Java
    static URLStreamHandler getURLStreamHandler(String protocol) {

        URLStreamHandler handler = handlers.get(protocol);
        if (handler == null) {

            boolean checkedWithFactory = false;

            // Use the factory (if any)
            if (factory != null) {
                handler = factory.createURLStreamHandler(protocol);
                checkedWithFactory = true;
            }
          	//后面的代码还会将
            ...
~~~

可以看到，如果factory不为空的话，那么直接根据factory的createURLStreamHandler创建处理器，**所以我们就可以对这个factory代理，然后重写createURLStreamHandler方法，根据不同的协议实现不同的Handler**。factory在URL中也提供了设置的入口：

~~~java
	    public static void setURLStreamHandlerFactory(URLStreamHandlerFactory fac) {
        synchronized (streamHandlerLock) {
            if (factory != null) {
                throw new Error("factory already defined");
            }
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkSetFactory();
            }
            handlers.clear();
            factory = fac;
        }
    }
~~~

我们只要调用这个方法，参数传入代理后的URLStreamHandlerFactory即可。

而url.openConnection方法返回的也是我们通过代理HttpUrlConnection得到的对象，然后在执行connect，disconnect时就可以插桩了，比如连接建立时间等，具体看代码agent1模块下httpinvoker包下的HttpProxy1和ProxyHttpUrlConnection。

**但是需要注意**：在上面这个方法中可以发现，如果factory!=null，那么会报错的，而事实也证明如此，不管是通过idea直接运行web项目还是通过jar的方式，因为factory我们在Tomcat启动前已经自己实现了一个代理类进行初始化，**但是Tomcat它也会创建这个factory，所以导致factory被初始化了两次**：

~~~Java
    private TomcatURLStreamHandlerFactory(boolean register) {
        this.registered = register;
        if (register) {
            URL.setURLStreamHandlerFactory(this);
        }

    }
~~~

也就是说这种方式如果servlet容器是Tomcat的话是行不通的。

#####  基于⾃定义包前缀获取:java.protocol.handler.pkgs

我们再回过头来看getURLStreamHandler这个方法：

~~~Java
static URLStreamHandler getURLStreamHandler(String protocol) {

        URLStreamHandler handler = handlers.get(protocol);
        if (handler == null) {

            boolean checkedWithFactory = false;

            // Use the factory (if any)
            if (factory != null) {
                handler = factory.createURLStreamHandler(protocol);
                checkedWithFactory = true;
            }

            // Try java protocol handler
            if (handler == null) {
                String packagePrefixList = null;

              	//根据java.protocol.handler.pkgs得到包前缀，默认是空
                packagePrefixList
                    = java.security.AccessController.doPrivileged(
                    new sun.security.action.GetPropertyAction(
                        protocolPathProp,""));
                if (packagePrefixList != "") {
                    packagePrefixList += "|";
                }

                // REMIND: decide whether to allow the "null" class prefix
                // or not.
                packagePrefixList += "sun.net.www.protocol";

                StringTokenizer packagePrefixIter =
                    new StringTokenizer(packagePrefixList, "|");

                while (handler == null &&
                       packagePrefixIter.hasMoreTokens()) {

                    String packagePrefix =
                      packagePrefixIter.nextToken().trim();
                    try {
                      	//只需在packagePrefixList中也加入一个我们自定义的packagePrefix即可，其余不变
                        String clsName = packagePrefix + "." + protocol +
                          ".Handler";
                        Class<?> cls = null;
                        try {
                            cls = Class.forName(clsName);
                        } catch (ClassNotFoundException e) {
                            ClassLoader cl = ClassLoader.getSystemClassLoader();
                            if (cl != null) {
                                cls = cl.loadClass(clsName);
                            }
                        }
                        if (cls != null) {
                            handler  =
                              (URLStreamHandler)cls.newInstance();
                        }
                    } catch (Exception e) {
                        // any number of exceptions can get thrown here
                    }
                }
            }
          ...
~~~

当factory为空后，首先通过这个方法GetPropertyAction，其中参数protocolPathProp = "java.protocol.handler.pkgs";：

~~~Java
    private String theProp;
    private String defaultVal;

    public GetPropertyAction(String var1) {
        this.theProp = var1;
    }

		//构造方法赋值
    public GetPropertyAction(String var1, String var2) {
        this.theProp = var1;
        this.defaultVal = var2;
    }

	  //放入系统属性中
		//Key就是java.protocol.handler.pkgs，value默认是空
    public String run() {
        String var1 = System.getProperty(this.theProp);
        return var1 == null ? this.defaultVal : var1;
    }
~~~

然后后面在包前缀列表packagePrefixList中又加入了"sun.net.www.protocol"这个包，多个包是通过|来分隔的，然后根据包前缀+协议+Handler得到className，再通过反射生成URLStreamHandler，所以我们也可以模仿这种方式：**自定义包前缀，然后根据自定义包前缀+协议+Handler放入系统属性中，key还是java.protocol.handler.pkgs不变**。

比如我们在com.cxylk.agent1.httpinvoker.https（代理的是哪个协议，这个包名就是对应协议的名称）这个包下定义Handler类

~~~Java
public class Handler extends sun.net.www.protocol.https.Handler {
        @Override
        protected URLConnection openConnection(URL url, Proxy proxy) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) super.openConnection(url, proxy);
            return new ProxyHttpUrlConnection(connection, url);
        }

        @Override
        protected URLConnection openConnection(URL url) throws IOException {
            return openConnection(url, null);
        }
    }
~~~

然后将Handler所在的包加入系统属性中：

```
public class HttpProxy2 {
    private static final String PROTOCOL_HANDLER = "java.protocol.handler.pkgs";
    private static final String HANDLERS_PACKAGE="com.cxylk.agent1.httpinvoker";

    public static void registerProtocol(){
        String handlers = System.getProperty(PROTOCOL_HANDLER, "");
        //handlers不为空的话，还要加上handlers，使用|拼接
        System.setProperty(PROTOCOL_HANDLER,(handlers==null||handlers.isEmpty())?HANDLERS_PACKAGE:handlers+"|"+HANDLERS_PACKAGE);

    }
}
```

最后在premain方法中调用registerProtocol方法即可。
