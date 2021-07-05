# dubbo 注册流程

Provider:

1. ServiceCenterRegistry: doRegister
dubbo://192.168.0.204:8081/PriceService?anyhost=true&application=price-provider&deprecated=false&dubbo=2.0.2&dynamic=true&generic=false&interface=PriceService&metadata-type=remote&methods=sayHello,sayHelloAsync&pid=8172&release=2.7.7&side=provider&timestamp=1598794693130

2. ServiceCenterRegistry: doSubscribe
provider://192.168.0.204:8081/PriceService?anyhost=true&application=price-provider&bind.ip=192.168.0.204&bind.port=8081&category=configurators&check=false&deprecated=false&dubbo=2.0.2&dynamic=true&generic=false&interface=PriceService&metadata-type=remote&methods=sayHello,sayHelloAsync&pid=7504&qos.port=22222&release=2.7.7&side=provider&timestamp=1598795055146

注册完毕发送 : ServiceBeanExportedEvent 事件

Consumer:

1. ServiceCenterRegistry: doRegister
consumer://192.168.0.204/PriceService?application=order-consumer&category=consumers&check=false&dubbo=2.0.2&init=false&interface=PriceService&methods=sayHello,sayHelloAsync&pid=5452&qos.port=33333&release=2.7.7&side=consumer&sticky=false&timestamp=1598795748338


2. ServiceCenterRegistry: doSubscribe
consumer://192.168.0.204/PriceService?application=order-consumer&category=providers,configurators,routers&check=false&dubbo=2.0.2&init=false&interface=PriceService&methods=sayHello,sayHelloAsync&pid=5452&qos.port=33333&release=2.7.7&side=consumer&sticky=false&timestamp=1598795748338


3. ServiceCenterRegistry: doSubscribe: NotifyListener
dubbo://192.168.0.204:8081/PriceService?anyhost=true&application=price-provider&category=providers&deprecated=false&dubbo=2.0.2&dynamic=true&generic=false&interface=PriceService&metadata-type=remote&methods=sayHello,sayHelloAsync&path=PriceService&pid=7800&protocol=dubbo&release=2.7.7&side=provider&timestamp=1598797007624

4. ServiceCenterRegistry: lookup
consumer://192.168.0.204/PriceService?application=order-consumer&category=providers,configurators,routers&check=false&dubbo=2.0.2&interface=PriceService&methods=sayHello,sayHelloAsync&pid=10100&qos.port=33333&side=consumer&timestamp=1599116467792


dubbo://192.168.0.204:8081/PriceService?anyhost=true&application=price-provider&deprecated=false&dubbo=2.0.2&dynamic=true&generic=false&interface=PriceService&metadata-type=remote&methods=sayHello,sayHelloAsync&pid=8172&release=2.7.7&side=provider&timestamp=1598794693130
dubbo://192.168.0.204:8081/PriceService?anyhost=true&application=price-provider&deprecated=false&dubbo=2.0.2&dynamic=true&generic=false&interface=PriceService&metadata-type=remote&methods=sayHello,sayHelloAsync&pid=7800&release=2.7.7&side=provider&timestamp=1598797007624

category=providers&
&path=PriceService
&protocol=dubbo