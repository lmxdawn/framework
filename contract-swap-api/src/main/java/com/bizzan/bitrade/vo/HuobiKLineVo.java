package com.bizzan.bitrade.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class HuobiKLineVo {


    @JsonProperty("ch")
    private String ch;
    @JsonProperty("status")
    private String status;
    @JsonProperty("ts")
    private Long ts;
    @JsonProperty("data")
    private List<DataDTO> data;

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @JsonProperty("id")
        private Integer id;
        @JsonProperty("open")
        private Double open;
        @JsonProperty("close")
        private Double close;
        @JsonProperty("low")
        private Double low;
        @JsonProperty("high")
        private Double high;
        @JsonProperty("amount")
        private Double amount;
        @JsonProperty("vol")
        private Double vol;
        @JsonProperty("count")
        private Integer count;
    }
}
