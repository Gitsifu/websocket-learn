# WebSocket学习

原文链接： [https://blog.csdn.net/qq_28988969/article/details/78113463](https://blog.csdn.net/qq_28988969/article/details/78113463)

## SpringBoot中建立WebSocket连接(STOMP)

STOMP协议介绍
STOMP，Streaming Text Orientated Message Protocol，是流文本定向消息协议，是一种为MOM(Message Oriented Middleware，面向消息的中间件)设计的简单文本协议。

它提供了一个可互操作的连接格式，允许STOMP客户端与任意STOMP消息代理(Broker)进行交互，类似于OpenWire(一种二进制协议)。

由于其设计简单，很容易开发客户端，因此在多种语言和多种平台上得到广泛应用。其中最流行的STOMP消息代理是Apache ActiveMQ。

STOMP协议工作于TCP协议之上，使用了下列命令：

- SEND 发送
-  SUBSCRIBE 订阅
-  UNSUBSCRIBE 退订
-  BEGIN 开始
-  COMMIT 提交
-  ABORT 取消
-  ACK 确认
-  DISCONNECT 断开

发送消息：

```
SEND 
destination:/app/sendTest 
content-type:application/json 
content-length:44 

{"userId":"rwerfef45434refgrege"}
```

订阅消息：
```
SUBSCRIBE 
id:sub-1 
destination:/app/subscribeTest
```

服务器进行广播：
```
MESSAGE 
message-id:nxahklf6-1 
subscription:sub-1 
destination:/topic/subscribeTest 
{"message":"it is test"}
```

更多详细的STOMP API，请[点击这里](https://blog.csdn.net/jqsad/article/details/77745379)

springboot使用STOMP消息步骤：

- 添加pom文件依赖
- java方式配置websocket stomp
- 消息实体类
- 书写控制层
- 书写客户端

### 1.添加pom文件依赖
```
<!-- springboot websocket -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

### 2.java方式配置websocket stomp
```
package com.ahut.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

/**
 * 
 * @ClassName: WebSocketStompConfig
 * @Description: springboot websocket stomp配置
 * @author cheng
 * @date 2017年9月27日 下午3:45:36
 */

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketStompConfig extends AbstractWebSocketMessageBrokerConfigurer {

    /**
     * 注册stomp的端点
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 允许使用socketJs方式访问，访问点为webSocketServer，允许跨域
        // 在网页上我们就可以通过这个链接
        // http://localhost:8080/webSocketServer
        // 来和服务器的WebSocket连接
        registry.addEndpoint("/webSocketServer").setAllowedOrigins("*").withSockJS();
    }

    /**
     * 配置信息代理
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 订阅Broker名称
        registry.enableSimpleBroker("/queue", "/topic");
        // 全局使用的消息前缀（客户端订阅路径上会体现出来）
        registry.setApplicationDestinationPrefixes("/app");
        // 点对点使用的订阅前缀（客户端订阅路径上会体现出来），不设置的话，默认也是/user/
        // registry.setUserDestinationPrefix("/user/");
    }

}
```

代码详解：

```
registry.addEndpoint("/webSocketServer").setAllowedOrigins("*").withSockJS();
```
设置端点，客户端通过http://localhost:8080/webSocketServer来和服务器进行websocket连接


```
registry.enableSimpleBroker("/queue", "/topic");
```
用户订阅主题的前缀 

- /topic 代表发布广播，即群发 

- /queue 代表点对点，即发指定用户

```
registry.setApplicationDestinationPrefixes("/app");
```

设置客户端请求前缀 

例如客户端发送消息的目的地为/app/sendTest，则对应控制层@MessageMapping(“/sendTest”) 

客户端订阅主题的目的地为/app/subscribeTest，则对应控制层@SubscribeMapping(“/subscribeTest”)

### 3.消息实体类

客户端发往服务器端实体类(自定义)

```
package com.ahut.entity;

/**
 * 
 * @ClassName: ClientMessage
 * @Description: 客户端发送消息实体
 * @author cheng
 * @date 2017年9月27日 下午4:24:11
 */
public class ClientMessage {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
```

服务器端发往客户端实体类(自定义)

```
package com.ahut.entity;

/**
 * 
 * @ClassName: ServerMessage
 * @Description: 服务端发送消息实体
 * @author cheng
 * @date 2017年9月27日 下午4:25:26
 */
public class ServerMessage {
    private String responseMessage;

    public ServerMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }
}
```

### 4.书写控制层

```
package com.ahut.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import com.ahut.entity.ClientMessage;
import com.ahut.entity.ServerMessage;

/**
 * 
 * @ClassName: WebSocketAction
 * @Description: websocket控制层
 * @author cheng
 * @date 2017年9月27日 下午4:20:58
 */
@Controller
public class WebSocketAction {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @MessageMapping("/sendTest")
    @SendTo("/topic/subscribeTest")
    public ServerMessage sendDemo(ClientMessage message) {
        logger.info("接收到了信息" + message.getName());
        return new ServerMessage("你发送的消息为:" + message.getName());
    }

    @SubscribeMapping("/subscribeTest")
    public ServerMessage sub() {
        logger.info("XXX用户订阅了我。。。");
        return new ServerMessage("感谢你订阅了我。。。");
    }

}
```

代码详解：

@MessageMapping(“/sendTest”) 

接收客户端发送的消息，当客户端发送消息的目的地为/app/sendTest时，交给该注解所在的方法处理消息，其中/app是在

```
registry.setApplicationDestinationPrefixes("/app");
```

一步配置的客户端请求前缀 
若没有添加@SendTo注解且该方法有返回值，则返回的目的地地址为/topic/sendTest，经过消息代理，客户端需要订阅了这个主题才能收到返回消息

@SubscribeMapping(“/subscribeTest”) 

接收客户端发送的订阅，当客户端订阅的目的地为/app/subscribeTest时，交给该注解所在的方法处理订阅，其中/app为客户端请求前缀 
若没有添加@SendTo注解且该方法有返回值，则返回的目的地地址为/app/sendTest，不经过消息代理，客户端需要订阅了这个主题才能收到返回消息

@SendTo(“/topic/subscribeTest”) 

修改返回消息的目的地地址为/topic/subscribeTest，经过消息代理，客户端需要订阅了这个主题才能收到返回消息

### 5.书写客户端
```
<!DOCTYPE html>
<html>

<head>
    <title>stomp</title>
</head>

<body>
    Welcome<br/><input id="text" type="text" />
    <button onclick="send()">发送消息</button>
    <button onclick="subscribe2()">订阅消息/topic/sendTest</button>
    <button onclick="subscribe1()">订阅消息/topic/subscribeTest</button>
    <hr/>
    <button onclick="closeWebSocket()">关闭WebSocket连接</button>
    <hr/>
    <div id="message"></div>
</body>

<script src="http://cdn.bootcss.com/stomp.js/2.3.3/stomp.min.js"></script>
<script src="https://cdn.bootcss.com/sockjs-client/1.1.4/sockjs.min.js"></script>
<script type="text/javascript">
    // 建立连接对象（还未发起连接）
    var socket = new SockJS("http://localhost:8080/webSocketServer");

    // 获取 STOMP 子协议的客户端对象
    var stompClient = Stomp.over(socket);

    // 向服务器发起websocket连接并发送CONNECT帧
    stompClient.connect(
        {},
        function connectCallback(frame) {
            // 连接成功时（服务器响应 CONNECTED 帧）的回调方法
            setMessageInnerHTML("连接成功");
            stompClient.subscribe('/app/subscribeTest', function (response) {
                setMessageInnerHTML("已成功订阅/app/subscribeTest");
                var returnData = JSON.parse(response.body);
                setMessageInnerHTML("/app/subscribeTest 你接收到的消息为:" + returnData.responseMessage);
            });
        },
        function errorCallBack(error) {
            // 连接失败时（服务器响应 ERROR 帧）的回调方法
            setMessageInnerHTML("连接失败");
        }
    );

    //发送消息
    function send() {
        var message = document.getElementById('text').value;
        var messageJson = JSON.stringify({ "name": message });
        stompClient.send("/app/sendTest", {}, messageJson);
        setMessageInnerHTML("/app/sendTest 你发送的消息:" + message);
    }

    //订阅消息
    function subscribe1() {
        stompClient.subscribe('/topic/subscribeTest', function (response) {
            setMessageInnerHTML("已成功订阅/topic/subscribeTest");
            var returnData = JSON.parse(response.body);
            setMessageInnerHTML("/topic/subscribeTest 你接收到的消息为:" + returnData.responseMessage);
        });
    }

    //订阅消息
    function subscribe2() {
        stompClient.subscribe('/topic/sendTest', function (response) {
            setMessageInnerHTML("已成功订阅/topic/sendTest");
            var returnData = JSON.parse(response.body);
            setMessageInnerHTML("/topic/sendTest 你接收到的消息为:" + returnData.responseMessage);
        });
    }

    //将消息显示在网页上
    function setMessageInnerHTML(innerHTML) {
        document.getElementById('message').innerHTML += innerHTML + '<br/>';
    }

</script>

</html>
```

注意：以下两个js文件一定要记得引入

```
<script src="http://cdn.bootcss.com/stomp.js/2.3.3/stomp.min.js"></script>
<script src="https://cdn.bootcss.com/sockjs-client/1.1.4/sockjs.min.js"></script>
```
### 客户端发送和接收消息图解
![](https://github.com/Gitsifu/websocket-learn/src/resources/static/img.png)

服务器主动推数据

任意类中都可以
```
public class 任意类{
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    //客户端只要订阅了/topic/subscribeTest主题，调用这个方法即可
    public void templateTest() {
        messagingTemplate.convertAndSend("/topic/subscribeTest", new ServerMessage("服务器主动推的数据"));
    }
}
```
