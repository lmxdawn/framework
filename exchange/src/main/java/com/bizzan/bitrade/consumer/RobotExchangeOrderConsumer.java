package com.bizzan.bitrade.consumer;

import com.alibaba.fastjson.JSON;
import com.bizzan.bitrade.Trader.CoinTrader;
import com.bizzan.bitrade.Trader.CoinTraderFactory;
import com.bizzan.bitrade.entity.ExchangeOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 机器人订单数据消费
 * 这个地方仅仅是给机器人使用的
 */
@Slf4j
@Component
public class RobotExchangeOrderConsumer {

    @Autowired
    private CoinTraderFactory traderFactory;
    
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @KafkaListener(topics = "robot-exchange-order",containerFactory = "kafkaListenerContainerFactory")
    public void onOrderSubmitted(List<ConsumerRecord<String,String>> records){
        for (int i = 0; i < records.size(); i++) {
            ConsumerRecord<String,String> record  = records.get(i);
            ExchangeOrder order = JSON.parseObject(record.value(), ExchangeOrder.class);
            if(order == null){
                return ;
            }
            log.info("接收订单>>topic={},value={},size={},orderId={}",record.topic(),record.value(),records.size(),order.getOrderId());
            CoinTrader trader = traderFactory.getTrader(order.getSymbol());
            //如果当前币种交易暂停会自动取消订单
            if (trader.isTradingHalt() || !trader.getReady()) {
                //撮合器未准备完成，撤回当前等待的订单
                kafkaTemplate.send("robot-exchange-order-cancel-success", JSON.toJSONString(order));
            } else {
                try {
                    long startTick = System.currentTimeMillis();
                    trader.trade(order);
                    log.info("complete trade,{}ms used!", System.currentTimeMillis() - startTick);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("====交易出错，退回订单===",e);
                    kafkaTemplate.send("robot-exchange-order-cancel-success", JSON.toJSONString(order));
                }
            }
        }
    }

    @KafkaListener(topics = "robot-exchange-order-cancel",containerFactory = "kafkaListenerContainerFactory")
    public void onOrderCancel(List<ConsumerRecord<String,String>> records){
        for (int i = 0; i < records.size(); i++) {
            ConsumerRecord<String,String> record  = records.get(i);
            ExchangeOrder order = JSON.parseObject(record.value(), ExchangeOrder.class);
            if(order == null){
                return ;
            }
            log.info("取消订单topic={},key={},size={},orderId={}",record.topic(),record.key(),records.size(),order.getOrderId());
            CoinTrader trader = traderFactory.getTrader(order.getSymbol());
            if(trader.getReady()) {
                try {
                    ExchangeOrder result = trader.cancelOrder(order);
                    if (result != null) {
                        kafkaTemplate.send("robot-exchange-order-cancel-success", JSON.toJSONString(result));
                    }
                }catch (Exception e){
                    log.info("====取消订单出错===",e);
                    e.printStackTrace();
                }
            }
        }
    }
}
