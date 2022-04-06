package com.bizzan.bitrade.dto;

import com.bizzan.bitrade.annotation.Excel;
import com.bizzan.bitrade.annotation.ExcelSheet;
import lombok.Data;

@Data
@ExcelSheet
public class BaseMemberDTO {

    @Excel(name="订单编号")
    private Long memberId ;

    @Excel(name="用户名")
    private String username ;

    @Excel(name="邮箱")
    private String email ;

    @Excel(name="手机号")
    private String mobilePhone ;

    @Excel(name="真实姓名")
    private String realName ;
}
