package com.bizzan.bitrade.dto;

import com.bizzan.bitrade.annotation.Excel;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString(callSuper = true)
public class MemberWalletDTO{

    private Long id ;

    @Excel(name="用户ID")
    private Long memberId = 0L;

    @Excel(name="用户名")
    private String username = "";

    @Excel(name="邮箱")
    private String email = "";

    @Excel(name="手机号")
    private String mobilePhone = "";

    @Excel(name="真实姓名")
    private String realName = "";

    @Excel(name="币种名称")
    private String unit = "";

    @Excel(name="钱包地址")
    private String address = "";

    @Excel(name="可用币数")
    private BigDecimal balance = BigDecimal.ZERO;

    @Excel(name="冻结币数")
    private BigDecimal frozenBalance = BigDecimal.ZERO;

    @Excel(name="总币个数")
    private BigDecimal allBalance = BigDecimal.ZERO;
}
