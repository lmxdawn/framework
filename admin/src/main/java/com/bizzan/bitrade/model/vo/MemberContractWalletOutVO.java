package com.bizzan.bitrade.model.vo;

import com.bizzan.bitrade.annotation.Excel;
import com.bizzan.bitrade.entity.ContractCoin;
import com.bizzan.bitrade.entity.ContractOrderPattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberContractWalletOutVO {

    @Excel(name = "用户ID")
    private Long memberId;
    @Excel(name = "邮箱")
    private String email;
    @Excel(name = "手机号")
    private String mobilePhone;
    @Excel(name = "真实姓名")
    private String realName;
    @Excel(name = "合约账户")
    private String symbol; // 转换
    @Excel(name = "可用余额")
    private String usdtBalance; // 转换
    @Excel(name = "冻结余额")
    private String usdtFrozenBalance; // 转换
    @Excel(name = "仓位模式")
    private String usdtPattern; // 转换
    @Excel(name = "多仓当前盈亏")
    private String usdtBuyPositionMoney; // 转换
    @Excel(name = "多仓杠杆")
    private String usdtBuyLeverage; // 转换
    @Excel(name = "多仓仓位")
    private String usdtBuyPosition; // 转换
    @Excel(name = "多仓保证金")
    private String usdtBuyPrincipalAmount; // 转换
    @Excel(name = "冻结多仓")
    private String usdtFrozenBuyPosition; // 转换
    @Excel(name = "空仓盈亏")
    private String usdtSellPositionMoney; // 转换
    @Excel(name = "空仓杠杆")
    private String usdtSellLeverage; // 转换
    @Excel(name = "空仓仓位")
    private String usdtSellPosition; // 转换
    @Excel(name = "空仓保证金")
    private String usdtSellPrincipalAmount; // 转换
    @Excel(name = "冻结空仓")
    private String usdtFrozenSellPosition; // 转换

}
