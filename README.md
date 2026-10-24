#### 1.注册中心Nacos
下载地址：`[https://github.com/alibaba/nacos/releases/download/2.3.2/nacos-server-2.3.2.zip`](https://github.com/alibaba/nacos/releases/download/2.3.2/nacos-server-2.3.2.zip)

解压后进入bin目录，cmd执行：`startup.cmd -m standalone`

访问地址：`[http://127.0.0.1:8848/nacos`](http://127.0.0.1:8848/nacos)

默认账号密码：`nacos / nacos`

Pom依赖：
```
<!-- Nacos 注册中心客户端依赖 -->  
<dependency>  
    <groupId>com.alibaba.cloud</groupId>  
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>  
</dependency>
```
#### 2.熔断降级组件Sentinel
下载地址：`[https://github.com/alibaba/Sentinel/releases]`(https://github.com/alibaba/Sentinel/releases)

执行命令：`java -jar -Dserver.port=8858 sentinel-dashboard-1.8.10.jar`

访问地址：`[http://localhost:8858]`(http://localhost:8858)

默认账号密码：`sentinel` / `sentinel`

Pom依赖：
```
<!-- Sentinel 客户端依赖 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
<!-- Sentinel Nacos 数据源适配依赖（用于持久化规则） -->
<dependency>
   <groupId>com.alibaba.csp</groupId>
   <artifactId>sentinel-datasource-nacos</artifactId>
</dependency>
```
yml配置完成后，重启服务：
1. Sentinel 采用的是“懒加载”机制，它不会在启动时强行连接控制台，而是需要你**至少访问一次受保护的接口**，它才会向控制台注册自己。
2. 访问一次接口后，再次刷新 Sentinel 控制台（`http://localhost:8080`），在左侧菜单点击 **簇点链路**，就能看到 `/consumer/hello/{name}` 这个资源已经在列表中出现。
3. 在 Sentinel 控制台的 **簇点链路** 页面，找到 `/consumer/hello/{name}` 这一行。点击右侧的 **\+ 流控**（流控规则）按钮。
4. 弹出窗口中填写规则：**阈值类型**：QPS（单机 QPS 阈值），**单机阈值**：填 `2` （代表该接口每秒最多只能通过 2 个请求，超过直接限流），点击新增。
5. 验证：
   1. **正常情况**：在浏览器里以正常速度（每秒 1-2 次）刷新 `http://localhost:8082/consumer/hello/SentinelTest`，页面正常返回。
   2. **触发限流**：如果你快速连续刷新（比如用手指按住 F5 狂刷，或者一秒内点好几下），页面会抛出 Sentinel 默认的限流提示（通常是 `Blocked by Sentinel (flow limiting)`）。
#### 3.负载均衡Pom依赖
```
<!-- Spring Cloud LoadBalancer 依赖 -->  
<dependency>  
    <groupId>org.springframework.cloud</groupId>  
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>  
</dependency>
```
#### 4.Gateway网关
Pom依赖：
```
<!-- Spring Cloud Gateway 依赖（基于 WebFlux） -->  
<dependency>  
    <groupId>org.springframework.cloud</groupId>  
    <artifactId>spring-cloud-starter-gateway</artifactId>  
</dependency>
<!-- 3. 【关键补充】网关使用 lb:// 动态路由必须依赖此组件进行负载均衡解析 -->  
<dependency>  
    <groupId>org.springframework.cloud</groupId>  
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>  
</dependency>
```
#### 5.Spring Security + JWT 统一认证鉴权
1. **统一拦截（Gateway Filter）**：所有外部请求到达网关（8080）后，网关作为第一道关卡进行拦截。
2. **JWT 校验**：网关利用 Spring Security 相关的 JWT 工具类，验证请求头（Header）中携带的 `Authorization` Token 是否合法、是否过期。
3. **放行或拒绝**：
   1. 如果是登录、注册等公共接口，**直接放行**。
   2. 如果是受保护的业务接口（如 `/consumer/**`），校验通过则**放行并透传用户信息**，校验失败则直接返回 `401 Unauthorized` 拦截在门外，不让恶意流量污染下游的业务微服务。

Pom依赖：
```
<!-- Spring Security 响应式 Web 依赖（适用于网关 WebFlux） -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security-reactive</artifactId>
</dependency>

<!-- JJWT 依赖用于生成和解析 Token -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```
1. 创建 SecurityConfig 关闭这个默认登录拦截，完全使用写的 JWT 自定义拦截器
2. 创建 JwtUtils 生成和解析token
3. 创建 AuthorizeFilter 设置JWT拦截规则
4. 创建 AuthController 登录接口获取token
#### 6.Spring Cloud Alibaba 微服务全链路分布式追踪与监控Zipkin + Micrometer Tracing + OpenFeign
Consumer依赖核心pom：
```
<!-- Spring Boot 监控与管理模块（提供 management.\* 配置解析能力） \-->  
<dependency>  
    <groupId>org.springframework.boot</groupId>  
    <artifactId>spring-boot-starter-actuator</artifactId>  
</dependency>  
<!-- 2. Micrometer 追踪 Brave 桥接器 \-->  
<dependency>  
    <groupId>io.micrometer</groupId>  
    <artifactId>micrometer-tracing-bridge-brave</artifactId>  
</dependency>  
<!-- 确保引入了 Micrometer 对 Feign 的原生追踪适配支持 \-->  
<dependency>  
    <groupId>io.github.openfeign</groupId>  
    <artifactId>feign-micrometer</artifactId>  
</dependency>  
  
<!-- 3. Zipkin 客户端上报组件 \-->  
<dependency>  
    <groupId>io.zipkin.reporter2</groupId>  
    <artifactId>zipkin-reporter-brave</artifactId>  
</dependency>
```
Provider依赖核心Pom：
```
<!-- 1. 统一使用 Brave 桥接器 \-->  
<!-- Spring Boot 监控与管理模块（提供 management.\* 配置解析能力） \-->  
<dependency>  
    <groupId>org.springframework.boot</groupId>  
    <artifactId>spring-boot-starter-actuator</artifactId>  
</dependency>  
<!-- 2. Micrometer 追踪 Brave 桥接器 \-->  
<dependency>  
    <groupId>io.micrometer</groupId>  
    <artifactId>micrometer-tracing-bridge-brave</artifactId>  
</dependency>  
  
<!-- 3. Zipkin 客户端上报组件 \-->  
<dependency>  
    <groupId>io.zipkin.reporter2</groupId>  
    <artifactId>zipkin-reporter-brave</artifactId>  
</dependency>
```
Zikpin下载链接：`[https://repo1.maven.org/maven2/io/zipkin/zipkin-server/]`(https://repo1.maven.org/maven2/io/zipkin/zipkin-server/)

启动命令：`java -jar zipkin.jar`

docker启动：`docker run -d -p 9411:9411 openzipkin/zipkin`

访问地址：`[http://127.0.0.1:9411/zipkin/]`(http://127.0.0.1:9411/zipkin/)

