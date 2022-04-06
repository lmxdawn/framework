package com.bizzan.bitrade.model.vo;

import com.bizzan.bitrade.entity.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberContractWalletVO {

    private Long id;
    private Long memberId;

    private ContractCoin contractCoin;

    /**
     * 金本位合约账户
     */
    private BigDecimal usdtBalance;// USDT余额（金本位用）

    private BigDecimal usdtFrozenBalance; // 冻结保证金（金本位用）

    private ContractOrderPattern usdtPattern; // 1逐仓 2全仓

    private BigDecimal usdtBuyLeverage;// 做多杠杆倍数（金本位））

    private BigDecimal usdtSellLeverage;// 做空杠杆倍数（金本位）

    private BigDecimal usdtBuyPosition;// 开多仓位(USDT本位)

    private BigDecimal usdtFrozenBuyPosition;// 冻结仓位(USDT本位)

    private BigDecimal usdtBuyPrice;// 多仓均价(USDT本位)

    private BigDecimal usdtBuyPrincipalAmount;// 多仓保证金(USDT本位)

    private BigDecimal usdtSellPosition;// 开空仓位(USDT本位,多少张)

    private BigDecimal usdtFrozenSellPosition;// 冻结仓位(USDT本位,多少张)

    private BigDecimal usdtShareNumber;// 合约面值(USDT本位,1张=多少USDT)

    private BigDecimal usdtSellPrice;// 空仓开仓均价(USDT本位)

    private BigDecimal usdtSellPrincipalAmount;// 空仓保证金(USDT本位)

    // 用户总盈亏 = usdtProfit + usdtLoss
    private BigDecimal usdtProfit;// 盈利

    private BigDecimal usdtLoss;// 亏损

    /**
     * 币本位合约账户
     */
    private BigDecimal coinBalance;// 币种余额（币本位用）

    private BigDecimal coinFrozenBalance;

    private ContractOrderPattern coinPattern; // 1逐仓 2全仓

    private BigDecimal coinBuyLeverage;// 做多杠杆倍数（币本位））

    private BigDecimal coinSellLeverage;// 做空杠杆倍数（币本位）

    private BigDecimal coinBuyPosition;// 开多仓位(币本位)

    private BigDecimal coinFrozenBuyPosition;// 冻结仓位(币本位)

    private BigDecimal coinBuyPrice;// 多仓均价(币本位)

    private BigDecimal coinBuyPrincipalAmount;// 多仓保证金(币本位)

    private BigDecimal coinSellPosition;// 开空仓位(币本位)

    private BigDecimal coinFrozenSellPosition;// 冻结仓位(币本位)

    private BigDecimal coinShareNumber;// 合约面值(币本位，一张=多少Coin)

    private BigDecimal coinSellPrice;// 空仓均价(币本位)

    private BigDecimal coinSellPrincipalAmount;// 空仓保证金(币本位)

    private BigDecimal usdtTotalProfitAndLoss = BigDecimal.ZERO; // 持仓合约权益（空 + 多）

    private BigDecimal coinTotalProfitAndLoss = BigDecimal.ZERO; // 持仓合约权益（空 + 多）

    private BigDecimal currentPrice;

    private BigDecimal cnyRate = BigDecimal.valueOf(7L);
    
    private String email;
    private String mobilePhone;
    private String realName;

}
