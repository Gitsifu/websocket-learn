package com.sifu.websocketlearn.controller;

import com.sifu.websocketlearn.entity.ServerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sifu
 * @version 1.0
 * @date 2018/5/12
 */
@RestController
@RequestMapping("/send")
public class SendMessageController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;



    /**
     * 客户端只要订阅了/topic/subscribeTest主题，调用这个方法即可
     */
    @RequestMapping("/test")
    public void templateTest() {
        messagingTemplate.convertAndSend("/topic/subscribeTest", new ServerMessage("服务器主动推的数据"));
    }
}
