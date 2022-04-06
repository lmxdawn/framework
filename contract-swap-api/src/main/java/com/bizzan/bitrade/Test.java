package com.bizzan.bitrade;

import com.alibaba.fastjson.JSONObject;
import com.bizzan.bitrade.util.OkHttpUtil;
import com.bizzan.bitrade.vo.HuobiKLineVo;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;

public class Test {

    public static void main(String[] args) {
        String tem = "btcusdt";
        StringBuilder sb = new StringBuilder(tem);
        sb.insert(tem.indexOf("usdt"), "/");
        System.out.println(sb.toString().toUpperCase());

        HuobiKLineVo huobiKLineVo = JSONObject.parseObject("{\"ch\":\"market.btcusdt.kline.1min\",\"status\":\"ok\",\"ts\":1640922519150,\"data\":[{\"id\":1640922480,\"open\":47174.06,\"close\":47166.96,\"low\":47162.61,\"high\":47174.07,\"amount\":0.5752087223463951,\"vol\":27131.38731662,\"count\":133}]}", HuobiKLineVo.class);

        System.out.println(huobiKLineVo);

        Map<String, String> map = new HashMap<>();
        map.put("period", "1min");
        map.put("size", "1");
        map.put("symbol", "btcusdt");

        String url = "https://api.huobi.pro/market/history/kline";
        String s = OkHttpUtil.get(url, map);
        System.out.println(s);

    }

}
