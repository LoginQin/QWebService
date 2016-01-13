#QWebService 说明

QWebService 是一个基于Spring框架快速发布WebService的工具库, 目标是让开发WebService接口变得简单.

你可以只声明一个Java类, 利用QWebService可以很快的将整个类的公用方法变成一个可以提供HTTP接口的WebService服务.

可以利用QWebService-Java客户端, 或者HessianRPC的客户端, 甚至直接用HTTP-GET请求构造请求路径来访问(就像发布的Controller)

##Maven
```xml
<dependency>
    <groupId>cn.duapi.qweb</groupId>
    <artifactId>qwebservice</artifactId>
    <version>1.1.1</version>
</dependency>

```

添加maven依赖库地址
```xml
<repositories>
       <repository>
           <id>qwei-maven-repo</id>
           <url>https://raw.githubusercontent.com/LoginQin/maven-repo/master</url>
       </repository>
</repositories>
```

国内使用oschina
```xml
	<repositories>
		<repository>
			<id>qinwei-maven-release-repository</id>
			<url>http://git.oschina.net/chinesetiger/maven-repo/raw/release/</url>
		</repository>
	</repositories>
```

## 服务端 applicationContent.xml 配置
QWebService支持注解发布, 和使用XML配置的方式发布.

###注解方式发布QWebService
如果你觉得自己配置XML实在太麻烦. QWebService提供了注解的方式.

#####在项目的`applicationContext.xml`引入注解支持的注册器
```html
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" 
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <!-- QWebService 注解注册支持 -->
    <import resource="qwebservice-annotation.xml"></import>

</beans>

```

####在代码中使用`@QWebService`注解来标注QWebService

下面的方法前提是**需要让Spring框架扫描到你的这个类**自动装配Bean, @QWebService本质也是一个@Component, 所以你可以在项目的任何地方采用
依赖注入的方式调用这个类.

####类模式
```java
@QWebService(url = "/rpc/mytest/")
public class MyTestAPI {

    public boolean test() {
        return false;
    }
    
    public MyModel get(int id) {
        return new MyModel();
    }
}
```

####接口模式
在注解中使用`api=`指定要发布的接口`url=`来表明映射的路径, `value`指定beanName(@Compoment)

```java
// value = 发布的URL映射地址
// api = 指定发布的接口方法为MyService
// value = 指定beanName 
@QWebService(url="/rpc/mytest/", api=MyService.class)
public class MyTestAPI implements MyService, otherInterface{

    // MyService 中定义了一个方法, 这个只会发布这个方法
    @Override
    public boolean test() {
        return false;
    }
    

    @Override
    public MyModel get(int id) {
        return new MyModel();
    }
}
```
###XML配置方式发布RPC

如果想了解注册过程可以采用XML配置, 整个配置类似Hessian发布RPC一样

```html
<!-- 将需要发布的接口用 WebServiceExporter 封装管理 -->
<bean name="resourceExporter" class="cn.duapi.qweb.WebServiceExporter">
    <property name="service">
        <ref bean="resourceWebService" />
    </property>
    <!-- 这个接口也可以不指定, 如果不指定接口, 将不采用接口模式, 而是类模式. -->
    <property name="serviceInterface">
        <value>com.mytest.api.ResourceWebService</value>
    </property>
</bean>

<!--将 resourceExporter 利用 WebServiceUrlHandlerMapping 跟路径URL映射 -->
<bean id="urlMapping" class="cn.duapi.qweb.WebServiceUrlHandlerMapping">
    <property name="mappings">
        <props>
            <!-- 这里可以配置多个 url map -->
            <prop key="/rpc/mytest/">resourceExporter</prop>
        </props>
    </property>
</bean>
```
##QWeb客户端RPC访问
```
<bean id="cizuWebServiceRemote" class="cn.duapi.qweb.client.QWebProxyFactoryBean">
	<property name="serviceUrl">
        <value>http://youdomain.com/rpc/mytest/</value>
	</property>
	<property name="serviceInterface">
		<value>com.mytest.api.ResourceWebService</value>
	</property>
</bean>
```



##Java-Hessian客户端访问
#####applicationContent.xml配置
```xml
<bean id="cizuWebServiceRemote" class="org.springframework.remoting.caucho.HessianProxyFactoryBean">
	<property name="serviceUrl">
        <!-- 需要指定[hessian-protol]来表明客户端采用Hessian协议访问 -->
        <value>http://youdomain.com/rpc/mytest/hessian-protol/</value>
	</property>
	<property name="serviceInterface">
		<value>com.mytest.api.ResourceWebService</value>
	</property>
</bean>
```

## <s>DuowanRPC -- 客户端访问 </s>
因为需要支持通用化, 已经废止支持duowanRPC方式
#####applicationContent.xml配置
```xml
<bean id="cizuWebServiceRemote" class="com.duowan.common.rpc.client.RPCProxyFactoryBean">
	<property name="serviceUrl">
        <!-- 需要指定[duowan-protol]来表明客户端采用duowan-rpc协议访问 -->
        <value>http://youdomain.com/rpc/mytest/duowan-protol/</value>
	</property>
	<property name="serviceInterface">
		<value>com.mytest.api.ResourceWebService</value>
	</property>
</bean>
```

##浏览器客户端访问
直接把接口当成是一个HTTP接口, 用 `[方法名].do` 的方式调用Java的公共方法, 并按照URL参数协议传递参数即可
比如发布的class有一个公共方法`list`, 直接利用`list.do` 调用
#####直接调用
> http://youdomain.com/rpc/mytest/list.do?pageId=1

#####跨域访问,支持JSONP, 只需要传递callback
> http://youdomain.com/rpc/mytest/list.do?pageId=1&callback=cb

#####跨域访问,支持返回JS, 只需要传递val
> http://youdomain.com/rpc/mytest/list.do?pageId=1&val=d


##自定义返回结果格式
如果你不想返回Leopard格式的`JsonView`, 那么你还可以自定义返回格式, QWebService提供了一些自定义的功能.

但这样做的后果是当前的`QWebService`只能基于Controller访问, 不能使用QWebService的Java客户端采用RPC方式连接上,比如Hessian等, 因为这破坏了统一的返回格式, 所以自定义的方式是不建议的.

如果你想返回一个其他的`ModelAndView`最好还是自己写对应的Controller, 因为快速发布可用的HTTP访问接口才是QWebService的初衷.

+ 直接在返回方法返回一个`ModelAndView`结果
+ 开放的类继承自`QWebViewHandler` 接口


###方法中直接返回ModelAndView对象, 覆盖默认的JsonView
```java
@QWebService(url="/rpc/mytest/")
public class MyTestAPI implements QWebViewHandler {

    public boolean test() {
        return false;
    }
    
    //在方法体中直接返回一个ModelAndView, 就像Controller一样, 会优先使用方法体的ModelAndView
    public ModelAndView get() {
        return new PagingJsonView(1, 10);
    }
}
```
###当前类实现`QWebViewHandler`接口
如果需要统一当前发布`QWebService`的所有公有方法体
```java
@QWebService(url="/rpc/mytest/")
public class MyTestAPI implements QWebViewHandler {

    public boolean test() {
        return false;
    }

    //方法体中的ModelAndView比QWebViewHandler优先级更高
    public ModelAndView get() {
        return new PagingJsonView(1, 10);
    }


    @Override
    public ModelAndView getResultView(String currMethodName, Object result) {
        // TODO 统一实现你处理方法, 这个公开的QWebService会统一使用这个渲染
        return null;
    }

    @Override
    public ModelAndView getExceptionView(String currMethodName, Throwable ex) {
        // TODO 统一实现如果抛出异常后的错误的处理方法
        return null;
    }
}
```

##关于
Author: Qin Wei ( ChineseTiger )

email: qinwei081@gmail.com   

email: qinwei081@foxmail.com


###License

[The Apache Software License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)


