#QWebService 说明
QWebService 是一个基于Spring框架快速发布QWeb Service的远程访问的工具库, 目标是让开发WEB服务接口变得简单.

一次发布, 多种方式调用: 

+ 支持普通HTTP方式调用
+ 支持JSONP方式callback/js调用
+ 支持Hessian客户端方式调用
+ 支持DuowanRPC方式调用
+ 支持QWebClient方式调用(基于普通HTTP, 做了包装类)
+ 你还可以扩展更多....

你可以只声明一个简单的Java类, 利用QWebService可以很快的将整个类的public方法(这里拓展了Java的public方法调用范围, 让public范围更广, 广到什么程度? 广到可以用HTTP去调用这个方法)变成一个可以提供HTTP接口的Web服务.

代码开源, 你还可以拓展到别的客户端调用实现, 这里只是实现了几个实例

> 严格来说`WebService`最初指的是基于`SOAP`和`WSDL`等较为复杂的服务协议模型, 历史的产物. `QWeb`不是指的这个复杂东西, 而是类`RESTful`的方式 

> 严格来说`RESTful`有严格的HEAD,GET,DELETE,POST请求和URL规范, 但是`QWeb`也不是完全按照规范来, 而是更为宽泛的HTTP请求应用.

> QWeb这里的Q表示Quickly(快速), 简单, 这里引用最初的两个概念是希望读者不要混淆, 从学术的严谨来说, 我也不想误人子弟, 这里不再探讨标准. 

> 就像OSI标准的七层模型规范和技术人员自己普遍适用的TCP/IP四层模型一样的道理, 往下不再做特殊说明.

##QWebService可以解决什么痛点
统一解决外部调用的问题.

+ 你用Hessian/DuowanRPC发布的RPC服务, 只能依托于对应的客户端访问, 无法在浏览器端调用, 此时前端同学需要调用你的服务, 你不得不再开发一个接口
+ 你用别的方式发布的Controller HTTP-WEB接口, 浏览器调用没问题, 客户端调用还需要模拟完整的HTTP调用逻辑, 特别Java端本身可以提供SDK, 你可能需要自己再写包装类
+ 你写的HTTP接口,可以让客户端调用..但是浏览器端调用却不支持跨越...好吧, 再改改代码..
+ 你用DuowanRPC发布的接口,公司外部或者没有DuowanRPC客户端的同事压根没法用.
+ 你突然想用别的语言调用你开放的接口..由于最初开放的是Hessian协议等...你不得不努力为你的语言找一个Hessian模块..

等等等等...一切的问题都是因为你的接口不够开放, 不够自由, 对客户端不友好.既然你都做成WEB服务让外部调用, 何不再公开透明一点? 

`QWeb`解决了我重复开发的问题, 不用每次为`再提供一个接口`的事情重复开发,
也不用每次为了提供一个通用接口就写一个Http包装类的麻烦, 每当我想用RPC实现的时候, 首先想到用`QWeb`,因为`QWeb`是对客户端友好的. 

我知道今后可能我需要用到我的脚本或别的语言去调用我开放的方法.用Java客户端的时候会选择QWeb客户端, 用别的语言或者浏览器, 我会直接按照SpringMVC声明的方式传递参数来调用HTTP接口

对客户端调用友好, 其意义在于, 我只需要发布一次接口(Write Once), (Do More)客户端可以用自己喜欢的方式调用我的接口.

这样一来, 开发者在设计接口的时候会尽量考虑通用化和向后兼容. 

其次, 安全性和可访问性不应该用调用协议来保证(调用协议可模拟), 而应该是用业务来保证.

最后, 性能, 跟你平常发布一个简单的HTTP接口性能一样(本质上他就是一个HTTP接口). 不同的是Hessian的方式可能更省流量, 因为其序列化方式.

当然如果你的项目上了`Dubbo`这样级别的服务, 那么我们的讨论的系统范围就不是一个级别的了. 这里解决普通RPC存在的场景.

##Maven
```xml
<dependency>
    <groupId>cn.duapi.qweb</groupId>
    <artifactId>qwebservice</artifactId>
    <version>1.2</version>
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


