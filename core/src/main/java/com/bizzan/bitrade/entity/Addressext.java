package com.bizzan.bitrade.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Data
@Table(name = "addressext")
@ToString
public class Addressext {
    @Id
    @NotNull(message = "自增ID")
    @Excel(name = "ID", orderNum = "1", width = 20)
    private Integer id;

    @Excel(name = "状态", orderNum = "1", width = 20)
    @NotNull(message = "状态不得为空")
    private Integer status;

    @Excel(name = "地址", orderNum = "1", width = 20)
    @NotBlank(message = "地址不得为空")
    private String address;

    @Excel(name = "协议ID", orderNum = "1", width = 20)
    private Integer coinprotocol;

    @Excel(name = "用户ID", orderNum = "1", width = 20)
    private Integer memberid;
    
}
