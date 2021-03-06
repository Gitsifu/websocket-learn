package com.sifu.websocketlearn.controller;

import com.sifu.websocketlearn.entity.ClientMessage;
import com.sifu.websocketlearn.entity.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

/**
 * websocket控制层
 *
 * @author sifu
 * @version 1.0
 * @date 2018/5/12
 */
@Controller
public class WebSocketController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 接收消息
     *
     * @param message
     * @return
     */
    @MessageMapping("/sendTest")
    @SendTo("/topic/subscribeTest") //若没有此注解，且该方法有返回值，则原路返回，即返回的目的地地址为/topic/sendTest，经过消息代理，客户端需要订阅了这个主题才能收到返回消息
    public ServerMessage sendDemo(ClientMessage message) {
        logger.info("接收到了信息" + message.getName());
        return new ServerMessage("你发送的消息为:" + message.getName());
    }

    /**
     * 消息订阅
     *
     * @return
     */
    @SubscribeMapping("/subscribeTest")
    public ServerMessage sub() {
        logger.info("XXX用户订阅了我。。。");
        return new ServerMessage("感谢你订阅了我。。。");
    }
}
