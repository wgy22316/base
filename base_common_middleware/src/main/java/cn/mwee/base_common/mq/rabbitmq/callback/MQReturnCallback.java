package cn.mwee.base_common.mq.rabbitmq.callback;

import cn.mwee.base_common.utils.json.MwJsonUtil;
import cn.mwee.base_common.utils.log4j2.MwLogger;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;

/**
 * Created by liaomengge on 16/12/19.
 */
public class MQReturnCallback implements RabbitTemplate.ReturnCallback {

    private static final Logger logger = MwLogger.getInstance(MQReturnCallback.class);

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        //记录投递exchange成功,后续处理失败的逻辑
        Map<String, Object> returnMap = Maps.newHashMap();
        returnMap.put("message", message.getMessageProperties());
        returnMap.put("replyCode", replyCode);
        returnMap.put("replyText", replyText);
        returnMap.put("exchange", exchange);
        returnMap.put("routingKey", routingKey);

        logger.error("Return Callback Failed, Detail Message[{}]", MwJsonUtil.toJson4Log(returnMap));
    }
}
