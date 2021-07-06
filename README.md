# Dubbo-Serivcecomb | [中文](README_ZH.md)

Dubbo-Serivcecomb provide extensions for [Apache Dubbo](https://github.com/apache/dubbo) to
using [Cloud Service Engine](https://www.huaweicloud.com/product/cse.html), so that
Dubbo users can use service center, config center and governance features. 

Dubbo's versions before 2.7.x `interface based discovery` is used. After 2.7.x, users 
can also use `service based discovery`, which is widely used in `Spring Cloud`, `ServiceComb` and
`Istio`.

Dubbo-ServiceComb provides `service based discovery` for all Dubbo's versions includes
versions before 2.7.x. So that different versions of Dubbo applications can be managed
by Cloud Service Engine.

## Supported versions 

Before reading the next contents, check the supported versions in different branch.

| Branch | Latest Version | Dubbo Base Version | Supported Dubbo Suggestions |
| ---  | ------- | ------------ | ----------- |
| 2.6.x | 1.1.3 | 2.6.9 | 2.6.x，Suggested upgrade to 2.6.9 or later |
| 2.6.x | 1.1.5-SNAPSHOT | 2.6.9 | 2.6.x，Suggested upgrade to 2.6.9 or later |
| master | 1.3.6 | 2.7.8 | 2.7.x，Suggested upgrade to 2.7.8 or later |
| master | 1.3.7-SNAPSHOT | 2.7.8 | 2.7.x，Suggested upgrade to 2.7.8 or later |

## Dubbo Microservice Concepts vs Dubbo-ServiceComb Concepts

* Dubbo Microservice Concepts
  * application: microservice. 
  * service：java interface。

* Dubbo-ServiceComb Concepts
  * microservice: microservice.
  * application: A group of microservices fulfils user applications.
  * schema: java interface. 

In the following chapters we use Dubbo-ServiceComb Concepts.

## Dubbo Governance vs Dubbo-ServiceComb Governance

When Dubbo application starts up, it registers all governance properties to register center.
Users change governance properties from `Dubbo Admin`, and Dubbo Admin will push changes
to application. The internal governance feature will read the change and follow the instructions.

Dubbo-ServiceComb registers governance properties to config center. Users change configurations
throw config center, and config center will push all change to the application.


## Benefits and Drawbacks 

* Benefits

  * Add `Service Based Discovery`, so that different version can be manged together. And 
    can inter-operate with other microservice framework.
  * Much less microservice instances. Each microservice has only one instance, while Dubbo 
    instances is compared to interfaces.
  * Same governance procedure with Spring Cloud, ServiceComb. So that we can deploy
    different services together and managed with the same rule.
  
* Drawbacks

  * Dubbo allows one interface has many implementations in different microservices, this is
    very useful for microservice split and combination. 
  * Dubbo uses `Dubbo Admin` to change governance properties, while Dubbo ServiceComb cannot.

## Quick Start

This quick start can be found in [dubbo-servicecomb-sample](https://github.com/huaweicse/dubbo-servicecomb-samples).

* Download [Local CSE](https://support.huaweicloud.com/en-us/productdesc-servicestage/cse_productdesc_0012.html), run
    `start.bat`. After start up, using `http://localhost:30106/` to open the console.

* Config POM in project

        ```
        <dependency>
          <groupId>com.huaweicloud.dubbo-servicecomb</groupId>
          <artifactId>dubbo-servicecomb-solution-spring-boot</artifactId>
        </dependency>
        ```
  
 * Start Up using Spring Boot
 
  In Spring Boot Component scan, add `classpath*:spring/dubbo-servicecomb.xml`.
  
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

* using service center 

   In Spring configuration files, change zookeeper to service center. 

        ```xml
        <dubbo:registry address="sc://127.0.0.1:30100"/>
        ```
    
    The address is ignored and configured in `dubbo.properties`. 
    
* In `dubbo.properties` or `applicion.yaml` configurations

  The configurations include microservice name, version and address of service
  center, config center.   
  
        ```yaml        
        PAAS_CSE_SC_ENDPOINT: http://127.0.0.1:30100
        PAAS_CSE_CC_ENDPOINT: http://127.0.0.1:30113
        
        dubbo:
          servicecomb:
            service:
              application: basic-application
              name: price-provider
              version: 1.0.0
              # environmen: production # options: development, testing, acceptance, production
              # project: 
              # instance:
              # initialStatus: UP # options: UP, DOWN, STARTING, OUTOFSERVICE
        
            registry:
              address: ${PAAS_CSE_SC_ENDPOINT}
            config:
              address: ${PAAS_CSE_CC_ENDPOINT}
        ```

* add governance configurations

```yaml
dubbo:
  servicecomb:
    governance: {"providerInfos":[{"serviceName":"price-provider","schemaInfos":[{"schemaId":"com.huawei.it.price.PriceService","parameters":{"timeout":5000}}]}]}
```

## Using Cloud Service Engine

Change address to Cloud Service Engine address and configure HTTPS, AK/SK. 

```properties
#### service center address ####
dubbo.servicecomb.registry.address=https://cse.cn-north-1.myhuaweicloud.com

#### config center address ####
dubbo.servicecomb.config.address=https://cse.cn-north-1.myhuaweicloud.com

#### enabled SSL  ####
dubbo.servicecomb.ssl.enabled=true

#### Configure AK/SK  ####
dubbo.servicecomb.credentials.enabled=true
dubbo.servicecomb.credentials.accessKey= Your access key
dubbo.servicecomb.credentials.secretKey= Your secret key
dubbo.servicecomb.credentials.project=cn-north-1
```

## Configurations Reference

```properties
#### Microservices ####
# Application
dubbo.servicecomb.service.application=discovery
# Microservice Name
dubbo.servicecomb.service.name=price-provider
# Version, default is 1.0.0.0
dubbo.servicecomb.service.version=1.0.0
# Environment, default is Empty.  Options: development, testing, acceptance, production
# dubbo.servicecomb.service.environment=production
# Project 
# dubbo.servicecomb.service.project=
#### END

#### Microservice Instance ####
# Initial status. Options: UP, DOWN, STARTING, OUTOFSERVICE
# dubbo.servicecomb.instance.initialStatus=UP
#### END

#### service center address ####
dubbo.servicecomb.registry.address=http://127.0.0.1:30100

#### config center address ####
dubbo.servicecomb.config.address=http://127.0.0.1:30113

#### SSL options  ####
#dubbo.servicecomb.ssl.enabled=true
# ssl engine. Options JDK or OPENSSL
#dubbo.servicecomb.ssl.engine=
# ssl protocols, default to TLSv1.2
#dubbo.servicecomb.ssl.protocols=
# ssl ciphers, default to TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256
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

#### AK/SK Options  ####
#dubbo.servicecomb.credentials.enabled=true
#dubbo.servicecomb.credentials.accessKey=
#dubbo.servicecomb.credentials.secretKey=
#dubbo.servicecomb.credentials.cipher=
#dubbo.servicecomb.credentials.project=cn-south-1
```


