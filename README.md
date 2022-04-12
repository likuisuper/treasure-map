# treasure-map

## 1、javaagent插桩机制

对应代码为项目中的`javaagent`模块

## 2、服务监控与ClassLoader问题

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
