package com.bizzan.bitrade.service;

import com.bizzan.bitrade.entity.ContractTrade;
import com.bizzan.bitrade.entity.KLine;
import com.bizzan.bitrade.service.Base.BaseService;
import org.java_websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ContractMarketService  extends BaseService {
    @Autowired
    private MongoTemplate mongoTemplate;

    private Logger logger = LoggerFactory.getLogger(ContractMarketService.class);

    public List<KLine> findAllKLine(String symbol, String peroid){
        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC,"time"));
        Query query = new Query().with(sort).limit(1000);

        return mongoTemplate.find(query,KLine.class,"contract_kline_"+symbol+"_"+peroid);
    }

    public List<KLine> findAllKLine(String symbol,long fromTime,long toTime,String period){
        Criteria criteria = Criteria.where("time").gte(fromTime).andOperator(Criteria.where("time").lte(toTime));
        Sort sort = new Sort(new Sort.Order(Sort.Direction.ASC,"time"));
        Query query = new Query(criteria).with(sort);
        List<KLine> kLines = mongoTemplate.find(query,KLine.class,"contract_kline_"+symbol.toUpperCase()+"_"+ period);
        return kLines;
    }

    public void saveKLine(String symbol, KLine kLine){
        String collectionName = "contract_kline_" + symbol + "_" + kLine.getPeriod();
        long timeStamp = findMaxTimestamp(symbol, kLine.getPeriod());
        if(kLine.getTime() == timeStamp){
            Query query = new Query(Criteria.where("time").is(kLine.getTime()));
            Update update = new Update();
            update.set("openPrice", kLine.getOpenPrice());
            update.set("highestPrice", kLine.getHighestPrice());
            update.set("lowestPrice", kLine.getLowestPrice());
            update.set("closePrice", kLine.getClosePrice());
            update.set("count", kLine.getCount());
            update.set("volume", kLine.getVolume());
            update.set("turnover", kLine.getTurnover());
            mongoTemplate.upsert(query, update, collectionName);
            return;
        }
        logger.info("保存K线(" + symbol + "): " + kLine.getPeriod() + "/" + kLine.getTime());
        mongoTemplate.insert(kLine,collectionName);
    }

    /**
     * 获取K最近一条K线的时间
     * @param symbol
     * @param period
     * @return
     */
    public long findMaxTimestamp(String symbol, String period) {
        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC,"time"));
        Query query = new Query().with(sort).limit(1);

        List<KLine> result = mongoTemplate.find(query,KLine.class,"contract_kline_"+symbol+"_"+period);

        if (result != null && result.size() > 0) {
            return result.get(0).getTime();
        } else {
            return 0;
        }
    }
}
