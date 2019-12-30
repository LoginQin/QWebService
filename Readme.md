# QWebService - Quickly Web Service 

QWebService 是一个基于Spring框架快速发布QWeb Service的RPC远程访问的工具库, 目标是让开发开放WEB服务接口变得简单.

## 启用方法
### pom.xml
```xml
<dependency>
    <groupId>cn.duapi.qweb</groupId>
    <artifactId>qwebservice</artifactId>
    <version>2.0-SNAPSHOT</version>
</dependency>
```


### Spring boot @EnableQWebService

在SpringBoot项目中, 可以直接用`@EnableQWebService`开启`QWebService`注解支持

或者, 可以在项目的`applicationContext.xml`引入注解支持的注册器
```html
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" 
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <!-- QWebService 注解注册支持 -->
    <import resource="qwebservice-annotation.xml"></import>

</beans>
```

### 在代码中使用`@QWebService`注解来标注QWebService

下面的方法前提是**需要让Spring框架扫描到你的这个类**自动装配Bean, `@QWebService`本质也是一个`@Component`, 所以你可以在项目的任何地方采用
依赖注入的方式调用这个类.

### 发布服务模式一: 注解模式 
`@QWebService`参数说明:
- `url` 
    指定要发布RPC-URL路径
- `api` 指定要发布的接口`url=`来表明映射的路径
- `value`指定beanName(@Compoment)
- `doc` 是否打开简单文档
- `accessToken` 是否启动token校验, 客户端请求需要带上accessToken

2.0版本砍掉了类发布支持, 接口模式更规范, 而且对于RPC场景, 都是需要先定义接口

```java
// value = 发布的URL映射地址
// api = 指定发布的接口方法为MyService
// value = 指定beanName 
@QWebService(url = "/rpc/mytest/", api = MyService.class)
public class MyTestAPI implements MyService, OtherInterface{

    // MyService 中定义了一个方法, 这个只会发布这个方法
    @Override // from MyService
    public boolean test() {
        return false;
    }
    

    @Override // from OtherInterface
    public MyModel get(int id) {
        return new MyModel();
    }
}
```

### 发布服务模式二: XML配置方式发布RPC

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

## 客户端访问模式
### 1. QWeb客户端RPC访问

- 模式1: 直接创建代理RPC对象
```java
ResourceWebService proxy = QWebProxyFactoryBean.getProxy("http://localhost:8080/rpc/mytest", ResourceWebService.class,
               "accessKey");
// 像本地调用一样使用
proxy.test("hello");
```

- 模式2: XML配置生成Bean
```xml
<bean id="cizuWebServiceRemote" class="cn.duapi.qweb.client.QWebProxyFactoryBean">
	<property name="serviceUrl">
        <value>http://youdomain.com/rpc/mytest/</value>
	</property>
	<property name="serviceInterface">
		<value>com.mytest.api.ResourceWebService</value>
	</property>
</bean>
```
- 模式3: Spring Boot创建唯一Bean
```java
@Bean
public TestWebService testWebService() {
   return QWebProxyFactoryBean.getProxy("http://localhost:8080/rpc/test", TestWebService.class,
     "accesskey");
}
```
### 2. 浏览器客户端访问
直接把接口当成是一个HTTP接口, 用 `[方法名].do` 的方式调用接口,  并按照URL参数协议传递参数即可
比如`list.do`
##### 直接调用
> http://youdomain.com/rpc/mytest/list.do?pageId=1

##### 跨域访问,支持JSONP, 只需要传递callback
> http://youdomain.com/rpc/mytest/list.do?pageId=1&callback=cb

##### 跨域访问,支持返回JS, 只需要传递val
> http://youdomain.com/rpc/mytest/list.do?pageId=1&val=d


### 自定义返回结果格式

如果你不想返回默认的`JsonView`, 那么你还可以自定义返回格式, QWebService提供了一些自定义的功能.

但这样做的后果是当前的`QWebService`只能基于Controller访问, 不能使用QWebService的Java客户端采用RPC方式连接上,比如Hessian等, 因为这破坏了统一的返回格式, 所以自定义的方式是不建议的.

如果你想返回一个其他的`ModelAndView`最好还是自己写对应的Controller, 因为快速发布可用的HTTP访问接口才是QWebService的初衷.

+ 直接在返回方法返回一个`ModelAndView`结果
+ 实现总的`QWebViewHandler` 接口


### 方法中直接返回ModelAndView对象, 覆盖默认的JsonView
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
### 当前类实现`QWebViewHandler`接口
如果需要统一当前发布`QWebService`的所有公有方法体
```java
@QWebService(url = "/rpc/mytest/")
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

## 简单文档接口

`QWebService` 在1.2版本以后可以为每个开放的接口生成一个简单`markpage`文档(自主), 为开放的接口生成一个HTML文档描述

启用方式: 在注解`@QWebService`的`doc`接口添加一些描述使得内容非空, 默认会启用文档功能.

如果不想启用, 不配置`doc`就行 

启用文档后, 浏览器直接访问 `url`接口时会看见一个接口文档, 调用方法和参数类型一览无遗. 

生成的`markpage`文档省去你自己写接口文档的麻烦(尤其是接口多,参数多的时候), 你可以另存为HTML文件, 然后为了隐蔽而关闭`doc`, 稍作修改, 就可以发给调用方了


## 关于

Author: Qin Wei
email: imqinwei@qq.com


### License

[The Apache Software License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)


