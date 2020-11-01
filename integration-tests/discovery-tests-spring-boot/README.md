运行测试的步骤：

1. 安装本地微服务引擎并启动
2. 运行 PriceApplication
3. 运行 OrderApplication
4. 配置中心下发配置项： 

        [全局配置] dubbo.servicecomb.test.configuration: peizhi
        [服务配置：price-provider] dubbo.servicecomb.test.configurationService: peizhi_service
        [全局配置] dubbo.servicecomb.governance: {"providerInfos":[{"serviceName":"price-provider","schemaInfos":[{"schemaId":"com.huaweicloud.it.price.PriceService","parameters":{"timeout":5000}}]}]}

5. 运行 PortalApplication 查看测试结果， 如果成功，输出 `running all test cases successfully`

