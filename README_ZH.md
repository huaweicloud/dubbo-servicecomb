# Dubbo-Serivcecomb | [English](README.md)

Dubbo-Serivcecomb 为 [Apache Dubbo](https://github.com/apache/dubbo) 开发的微服务应用，
提供注册中心、配置中心、服务治理支持，方便 dubbo 应用使用
[华为云微服务引擎](https://www.huaweicloud.com/product/cse.html) 。 

dubbo 的微服务概念一直在变化， 2.7.x 之前的版本和之后的版本存在差异。在 2.7.x 之前， dubbo 主要提供 `基于接口的服务发现`，
2.7.x 以后，为了和主流社区的概念统一 （比如 Spring  Cloud, ServiceComb, Istio 等）， dubbo 提供了`基于服务的服务发现` 机制。 

dubbo-servicecomb 为 dubbo 版本（包括 2.7.x 之前的版本）， 提供 `基于服务的服务发现`。 这样不同的微服务开发框架都可以平滑的接入
华为云微服务引擎，在微服务引擎里面，可以采用统一的微服务模型进行管理和治理。 

## 版本配套关系 

阅读下面的内容前，请切换对应代码分支，查看需要的版本内容。

| 分支 | 最新版本 | dubbo 基线版本 | 支持的dubbo版本建议|
| ---  | ------- | ------------ | ----------- |
| 2.6.x | 1.1.3 | 2.6.9 | 2.6.x，建议使用前先升级到 2.6.9 以上的版本|
| 2.6.x | 1.1.5-SNAPSHOT | 2.6.9 | 2.6.x，建议使用前先升级到 2.6.9 及以上的版本|
| master | 1.3.5 | 2.7.8 | 2.7.x，建议使用前先升级到 2.7.8 及以上的版本|
| master | 1.3.6-SNAPSHOT | 2.7.8 | 2.7.x，建议使用前先升级到 2.7.8 及以上的版本|

## dubbo 微服务概念 vs dubbo-servicecomb

* Dubbo微服务概念：
  * 应用(application）：可以独立部署和运行的单元。即通常所说的微服务。
  * 服务(service)：dubbo的服务发现机制是基于接口(java interface）的，dubbo的服务，对应于我们通常所说的接口。

* ServiceComb微服务概念：
  * 微服务(microservice）：可以独立部署和运行的单元。即通常所说的微服务。
  * 应用（application）: 服务于某种客户功能的一组微服务的统称。 比如客户OA系统，就是一个应用。应用由多个微服务组成，比如认证鉴权微服务、业务流微服务等。
  * 契约/接口(schema）：一个接口（java interface）对应一个契约文件。契约类似于dubbo的服务，但是契约没有实例化的概念，而dubbo的服务是可以实例化的，类似于微服务实例。

这些概念容易导致冲突，在下面的文档中，除了特殊说明，都会采用ServiceComb微服务概念。

## dubbo 服务治理概念 vs dubbo-servicecomb

和服务发现一样， dubbo 的服务治理是基于接口的。 dubbo 服务在启动的时候， 会将与接口有关的治理项注册到注册中心。通过注册中心控制台，
可以修改治理项，或者增加新的治理项。 注册中心将治理项的变更，推送给相关的服务订阅者(consumer)，从而实现服务治理。

ServiceComb的治理过程和dubbo不同。服务注册的时候，只会将微服务信息、微服务实例信息、契约/接口信息注册到注册中心。微服务实例信息的变更，
注册中心会推送给服务订阅者(consumer）。 单纯通过注册中心，无法实现服务治理。 ServiceComb提供了独立的配置中心，可以在配置中心修改、
增加配置项，配置项的变更会推送给作用域内的微服务。配置项作用域包括全局生效、微服务生效等。 服务订阅者(consumer)通过订阅配置变更实现
服务治理。 

## dubbo-servicecomb 功能介绍

基于 dubbo 提供的扩展机制，将 dubbo 的微服务概念和治理模型，转换为 ServiceComb的微服务概念和治理模型。 使用 cse dubbo 接入 CSE 以后，应用视图如下：

```
dubbo  <------->  servicecomb dubbo
应用                     服务
无                       应用
服务                     契约/接口
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

为了实现 dubbo 的原生治理， dubbo-servicecomb 增加了一个配置项:

```yaml
dubbo.servicecomb.governance: {"providerInfos":[{"serviceName":"price-provider","schemaInfos":[{"schemaId":"com.huaweicloud.it.price.PriceService","parameters":{"timeout":5000}}]}]}
```

服务订阅者监听这个配置项的变更，模拟 dubbo 原生监听注册信息的变更。 当配置项变更后，会调用 dubbo 的 NotifyListener , 将变更的属性，和
注册属性进行合并（配置变更覆盖注册属性），从而实现对于 dubbo 的服务治理。 上面的配置项演示了设置请求超时时间的功能。

## 优点和限制

* 优点

  * 统一了 servicecomb、spring cloud、dubbo 等微服务开发框架的模型。都采用服务进行统一的注册和发现。能够更好的和云原生开发相契合，比如将服务名映射为 istio 的主机名，能够更好的使用云原生运行环境的服务发现机制。
  * 缩小了注册中心的管理规模，能够支持更大型系统的注册模型。原生dubbo，假设有100个应用，每个应用有100个服务，每个应用部署100个实例，那么dubbo需要管理 100 * 100 * 100个实例和心跳信息，对注册中心的性能提出很高的要求，维持心跳也会对网络带宽造成极大的浪费。采用 dubbo servicecomb， 只需要管理 100 * 100 个实例信息和心跳，减少了100（服务个数）倍。通常来讲，应用个数相对是比较固定和比较少的，而服务个数和实例个数增长的速度远大于应用，所以实际场景这个优化的效果是非常好的。
  * 统一了微服务引擎对于不同微服务框架，比如 Spring Cloud、ServiceComb、Dubbo的治理模型。治理都通过配置的方式进行，让 Dubbo 跟随微服务引擎按照一样的策略长期发展和演进，为以后针对不同的开发框架抽象统一的治理模型奠定技术基础。
  
* 限制

  * 原生dubbo允许一个服务在不同的应用中提供实现。这个给服务拆分和合并带来了很大的方便，将一个微服务拆分两个微服务，consumer不用感知这个拆分。使用 dubbo servicecomb，服务拆分涉及接口变更，需要调用端配套修改（或者只能够通过应用网关屏蔽差异，前提条件是URL不发生变化）。
  * 原生dubbo可以通过注册中心控制台或者REST接口，调整注册参数，从而实现对微服务的治理。使用 dubbo servicecomb，无法满足这方面的使用要求。短期内需要通过提供的配置项实现治理，无法使用 dubbo-admin 控制台。

## 快速开始

[dubbo-servicecomb-sample](https://github.com/huaweicse/dubbo-servicecomb-samples) 提供了使用 dubbo-servicecomb 接入的例子。 可以下载
示例进行本地体验， 本章节简单介绍一个 dubbo 应用， 快速使用 dubbo-servicecomb 接入微服务引擎的过程。 

* 下载[本地微服务引擎](https://support.huaweicloud.com/productdesc-servicestage/cse_productdesc_0012.html), 解压后运行
    `start.bat` 启动。 工具会安装注册中心、配置中心，以及控制台。 安装完毕后， 通过 `http://localhost:30106/` 可以访问控制台。

* POM 中引入依赖

        ```
        <dependency>
          <groupId>com.huaweicloud.dubbo-servicecomb</groupId>
          <artifactId>dubbo-servicecomb-solution-spring-boot</artifactId>
        </dependency>
        ```
        
  上面两个部件，实现 dubbo 应用的注册和动态配置、服务治理配置项检测等功能。 
 
 * 采用 Spring Boot 启动
  Spring 扫描路径中，需要增加 `classpath*:spring/dubbo-servicecomb.xml`， 举例如下：
  
        ```
        @SpringBootApplication
        @ImportResource({"classpath*:spring/dubbo-provider.xml", "classpath*:spring/dubbo-servicecomb.xml"})
        public class PriceApplication {
          public static void main(String[] args) throws Exception {
            try {
              SpringApplication.run(PriceApplication.class);
            } catch (Throwable e) {
              e.printStackTrace();
            }
          }
        }
        ``` 

* 使用 service center 作为注册发现。
   在 Spring 配置文件中增加如下配置。 如果配置文件已经配置了 zookeeper， 那么需要使用下面的配置项进行替换。 

        ```xml
        <dubbo:registry address="sc://127.0.0.1:30100"/>
        ```
    
    这里配置的地址信息是不重要的。 服务中心和配置中心的实际地址在 `dubbo.properties` 里面指定。 
    
* 在配置文件 `dubbo.properties` 或者 `application.yml` 增加配置项。
  基本配置项包括微服务信息、微服务实例信息和服务中心、注册中心地址信息。  
  
        ```yaml        
        PAAS_CSE_SC_ENDPOINT: http://127.0.0.1:30100
        PAAS_CSE_CC_ENDPOINT: http://127.0.0.1:30113
        
        #### 服务配置信息 ####
        dubbo:
          servicecomb:
            service:
              application: basic-application # 所属应用。
              name: price-provider   # 服务名称。
              version: 1.0.0   # 版本。默认为 1.0.0.0
              # environmen: production # 环境。默认为空。可选值：development, testing, acceptance, production
              # project: # project。 默认为 default
              # instance:
              # initialStatus: UP # 实例初始状态。可选值： UP, DOWN, STARTING, OUTOFSERVICE
        
            registry:
              address: ${PAAS_CSE_SC_ENDPOINT}
            config:
              address: ${PAAS_CSE_CC_ENDPOINT}
        ```

## 接入微服务引擎

上面介绍了使用本地微服务引擎开发的过程。华为云微服务引擎接入需要使用 HTTPS，并且需要提供认证信息。 在配置文件增加如下配置即可：

```properties
#### 服务中心配置信息 ####
dubbo.servicecomb.registry.address=https://cse.cn-north-1.myhuaweicloud.com

#### 配置中心配置信息 ####
dubbo.servicecomb.config.address=https://cse.cn-north-1.myhuaweicloud.com

#### SSL 配置信息  ####
dubbo.servicecomb.ssl.enabled=true

#### AK/SK 认证配置  ####
dubbo.servicecomb.credentials.enabled=true
dubbo.servicecomb.credentials.accessKey= Your access key
dubbo.servicecomb.credentials.secretKey= Your secret key
dubbo.servicecomb.credentials.project=cn-north-1
```

## 所有配置项参考

```properties
#### 服务配置信息 ####
# 所属应用。
dubbo.servicecomb.service.application=discovery
# 服务名称。
dubbo.servicecomb.service.name=price-provider
# 版本。默认为 1.0.0.0
dubbo.servicecomb.service.version=1.0.0
# 环境。默认为空。可选值：development, testing, acceptance, production
# dubbo.servicecomb.service.environment=production
# project。 默认为 default
# dubbo.servicecomb.service.project=
#### END

#### 实例配置信息 ####
# 实例初始状态。可选值： UP, DOWN, STARTING, OUTOFSERVICE
# dubbo.servicecomb.instance.initialStatus=UP
#### END

#### 服务中心配置信息 ####
dubbo.servicecomb.registry.address=http://127.0.0.1:30100

#### 配置中心配置信息 ####
dubbo.servicecomb.config.address=http://127.0.0.1:30113

#### SSL 配置信息  ####
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

#### AK/SK 认证配置  ####
#dubbo.servicecomb.credentials.enabled=true
#dubbo.servicecomb.credentials.accessKey=
#dubbo.servicecomb.credentials.secretKey=
#dubbo.servicecomb.credentials.cipher=
#dubbo.servicecomb.credentials.project=cn-south-1
```


