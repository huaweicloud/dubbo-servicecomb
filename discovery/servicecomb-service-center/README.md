# dubbo 注册流程

Provider:

1. ServiceCenterRegistry: doRegister
dubbo://192.168.0.204:8081/com.huaweicloud.it.price.PriceService?anyhost=true&application=price-provider&deprecated=false&dubbo=2.0.2&dynamic=true&generic=false&interface=com.huaweicloud.it.price.PriceService&metadata-type=remote&methods=sayHello,sayHelloAsync&pid=8172&release=2.7.7&side=provider&timestamp=1598794693130

2. ServiceCenterRegistry: doSubscribe
provider://192.168.0.204:8081/com.huaweicloud.it.price.PriceService?anyhost=true&application=price-provider&bind.ip=192.168.0.204&bind.port=8081&category=configurators&check=false&deprecated=false&dubbo=2.0.2&dynamic=true&generic=false&interface=com.huaweicloud.it.price.PriceService&metadata-type=remote&methods=sayHello,sayHelloAsync&pid=7504&qos.port=22222&release=2.7.7&side=provider&timestamp=1598795055146

Consumer:

1. ServiceCenterRegistry: doRegister
consumer://192.168.0.204/com.huaweicloud.it.price.PriceService?application=order-consumer&category=consumers&check=false&dubbo=2.0.2&init=false&interface=com.huaweicloud.it.price.PriceService&methods=sayHello,sayHelloAsync&pid=5452&qos.port=33333&release=2.7.7&side=consumer&sticky=false&timestamp=1598795748338


2. ServiceCenterRegistry: doSubscribe
consumer://192.168.0.204/com.huaweicloud.it.price.PriceService?application=order-consumer&category=providers,configurators,routers&check=false&dubbo=2.0.2&init=false&interface=com.huaweicloud.it.price.PriceService&methods=sayHello,sayHelloAsync&pid=5452&qos.port=33333&release=2.7.7&side=consumer&sticky=false&timestamp=1598795748338


3. ServiceCenterRegistry: doSubscribe: NotifyListener
dubbo://192.168.0.204:8081/com.huaweicloud.it.price.PriceService?anyhost=true&application=price-provider&category=providers&deprecated=false&dubbo=2.0.2&dynamic=true&generic=false&interface=com.huaweicloud.it.price.PriceService&metadata-type=remote&methods=sayHello,sayHelloAsync&path=com.huaweicloud.it.price.PriceService&pid=7800&protocol=dubbo&release=2.7.7&side=provider&timestamp=1598797007624


dubbo://192.168.0.204:8081/com.huaweicloud.it.price.PriceService?anyhost=true&application=price-provider&deprecated=false&dubbo=2.0.2&dynamic=true&generic=false&interface=com.huaweicloud.it.price.PriceService&metadata-type=remote&methods=sayHello,sayHelloAsync&pid=8172&release=2.7.7&side=provider&timestamp=1598794693130
dubbo://192.168.0.204:8081/com.huaweicloud.it.price.PriceService?anyhost=true&application=price-provider&deprecated=false&dubbo=2.0.2&dynamic=true&generic=false&interface=com.huaweicloud.it.price.PriceService&metadata-type=remote&methods=sayHello,sayHelloAsync&pid=7800&release=2.7.7&side=provider&timestamp=1598797007624

category=providers&
&path=com.huaweicloud.it.price.PriceService
&protocol=dubbo