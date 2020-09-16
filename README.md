# dubbo-servicecomb 项目介绍

## 版本配套关系 

阅读下面的内容前，请切换对应代码分支，查看需要的版本内容。

| 分支 | 项目版本 | dubbo 基线版本 | 支持的dubbo版本建议|
| ---  | ------- | ------------ | ----------- |
| master | 1.0.0-SNAPSHOT | 2.6.9 | 2.6.x，建议使用前先升级到 2.6.9 以上的版本|

## dubbo 微服务概念 vs dubbo-servicecomb

* Dubbo微服务概念：
  * 应用(application）：可以独立部署和运行的单元。即通常所说的微服务。
  * 服务(service)：dubbo的服务发现机制是基于接口(java interface）的，dubbo的服务，对应于我们通常所说的接口。
  
* ServiceComb微服务概念：
  * 微服务(microservice）：可以独立部署和运行的单元。即通常所说的微服务。
  * 应用（application）: 服务于某种客户功能的一组微服务的统称。 比如客户OA系统，就是一个应用。应用由多个微服务组成，比如认证鉴权微服务、业务流微服务等。
  * 契约(schema）：一个接口（java interface）对应一个契约文件。契约类似于dubbo的服务，但是契约没有实例化的概念，而dubbo的服务是可以实例化的，类似于微服务实例。
  
## dubbo-servicecomb 功能介绍

基于 dubbo 提供的扩展机制，将 dubbo 的微服务概念和治理模型，转换为 ServiceComb的微服务概念和治理模型。 使用 cse dubbo 接入 CSE 以后，应用视图如下：

```
dubbo  <------->  servicecomb dubbo
应用                     服务
无                       应用
服务                     契约
服务实例                 服务实例中的一个端点(endpoint)
```

一个示例项目的注册信息：

```
Microserivce:
   application: Dubbo应用示例
   service：dubbo provider
   schemas: com.dubbo.Foo
                    com.dubbo.Bar
MicroserviceInstance:
   endpoints: dubbo://192.168.1.2/com.dubbo.Foo?methods=sayHello&pid=7504
                       dubbo://192.168.1.3/com.dubbo.Bar?methods=sayHello&pid=7507
```

## 优点和限制

* 优点

  * 统一了 servicecomb、spring cloud、dubbo 等微服务开发框架的模型。都采用服务进行统一的注册和发现。能够更好的和云原生开发相契合，比如将服务名映射为 istio 的主机名，能够更好的使用云原生运行环境的服务发现机制。
  * 缩小了注册中心的管理规模，能够支持更大型系统的注册模型。原生dubbo，假设有100个应用，每个应用有100个服务，每个应用部署100个实例，那么dubbo需要管理 100 * 100 * 100个实例和心跳信息，对注册中心的性能提出很高的要求，维持心跳也会对网络带宽造成极大的浪费。采用 dubbo servicecomb， 只需要管理 100 * 100 个实例信息和心跳，减少了100（服务个数）倍。通常来讲，应用个数相对是比较固定和比较少的，而服务个数和实例个数增长的速度远大于应用，所以实际场景这个优化的效果是非常好的。
  * 统一了治理模型。治理都通过配置的方式进行，可以支持和CSE目前治理框架的一样的长期发展和演进策略，为以后针对不同的开发框架抽象统一的治理模型奠定技术基础。
  
* 限制

  * 原生dubbo允许一个服务在不同的应用中提供实现。这个给服务拆分和合并带来了很大的方便，将一个微服务拆分两个微服务，consumer不用感知这个拆分。使用 dubbo servicecomb，服务拆分涉及接口变更，需要调用端配套修改（或者只能够通过应用网关屏蔽差异，前提条件是URL不发生变化）。
  * 原生dubbo可以通过注册中心控制台或者REST接口，调整注册参数，从而实现对微服务的治理。使用 dubbo servicecomb，无法满足这方面的使用要求。

可以看到，限制里面的内容， dubbo 在 2.7 以后的版本也进行了调整，架构设计上支持以微服务为粒度进行服务发现、配置和注册分离。相比于其他的优点，这里的
限制显得不那么重要。 

## 快速开始

1. POM 中引入依赖

        ```yaml
            <dependency>
              <groupId>com.huaweicloud</groupId>
              <artifactId>dubbo-servicecomb-service-center</artifactId>
            </dependency>
        ```

2. 采用 Spring 启动（目前只支持 Spring 启动方式）。
  Spring 扫描路径中，需要增加 `classpath*:spring/dubbo-servicecomb.xml`， 举例如下：

        ```java
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "classpath*:spring/dubbo-provider.xml", "classpath*:spring/dubbo-servicecomb.xml");
        ```

3. 使用 service center 作为注册发现。
   在 Spring 配置文件中增加如下配置。 如果配置文件已经配置了 zookeeper， 那么需要使用下面的配置项进行替换。 

        ```xml
        <dubbo:registry address="sc://127.0.0.1:30100"/>
        ```
4. 在配置文件 `dubbo.properties` 增加配置项。
  基本配置项包括微服务信息、微服务实例信息和服务中心地址信息。 提供了使用 SSL、 AK/SK 进行认证的配置。 
  
        ```properties
        # 版本。默认为 1.0.0.0
        dubbo.servicecomb.service.version=1.0.0
        # 环境。默认为空。可选值：development, testing, acceptance, production
        # dubbo.servicecomb.service.environment=production
        #### END
        
        #### 实例配置信息 ####
        # 实例初始状态。可选值： UP, DOWN, STARTING, OUTOFSERVICE
        # dubbo.servicecomb.instance.initialStatus=UP
        #### END
        
        #### 服务中心配置信息 ####
        dubbo.servicecomb.registry.address=http://127.0.0.1:30100
        # project。 默认为 default
        # dubbo.servicecomb.registry.project=
        #dubbo.servicecomb.ssl.enabled=true
        # ssl engine. 默认为 JDK， 可选 OPENSSL
        #dubbo.servicecomb.ssl.engine=
        # ssl protocols。 默认 TLSv1.2
        #dubbo.servicecomb.ssl.protocols=
        # ssl ciphers。默认 TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256
        #dubbo.servicecomb.ssl.ciphers=
        #dubbo.servicecomb.ssl.authPeer=false
        #dubbo.servicecomb.ssl.trustStore=
        #dubbo.servicecomb.ssl.trustStoreType=
        #dubbo.servicecomb.ssl.trustStoreValue=
        #dubbo.servicecomb.ssl.keyStore=
        #dubbo.servicecomb.ssl.keyStoreType=
        #dubbo.servicecomb.ssl.keyStoreValue=
        #dubbo.servicecomb.ssl.crl=
        #dubbo.servicecomb.ssl.sslCustomClass=
        
        # AK/SK 认证配置
        #dubbo.servicecomb.credentials.enabled=true
        #dubbo.servicecomb.credentials.accessKey=
        #dubbo.servicecomb.credentials.secretKey=
        #dubbo.servicecomb.credentials.cipher=
        #dubbo.servicecomb.credentials.project=cn-south-1
        ```
