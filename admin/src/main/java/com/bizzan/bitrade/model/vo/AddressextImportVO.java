package com.bizzan.bitrade.model.vo;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Data
public class AddressextImportVO {

    @NotNull(message = "请选择协议")
    private Integer protocol;
    @NotBlank(message = "请输入密钥")
    private String key;
    @NotBlank(message = "请输入导入地址")
    private String txt;
    @NotBlank(message = "请输入Google验证码")
    private String GoogleCode;

}
