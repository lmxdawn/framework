package com.bizzan.bitrade.controller;

import com.bizzan.bitrade.entity.Recharge;
import com.bizzan.bitrade.entity.transform.AuthMember;
import com.bizzan.bitrade.service.*;
import com.bizzan.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;


import static com.bizzan.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * @author GS
 * @date 2018年01月26日
 */
@RestController
@Slf4j
@RequestMapping(value = "/recharge", method = RequestMethod.POST)
public class RechargeController {

    @Autowired
    private RechargeService rechargeService;

    /**
     * 充值记录
     */
    @RequestMapping("list")
    public MessageResult withdrawCode(@SessionAttribute(SESSION_MEMBER) AuthMember user, int page, int pageSize) {

        MessageResult mr = new MessageResult(0, "success");

        Page<Recharge> records = rechargeService.findAllByMemberId((int) user.getId(), page, pageSize);
        mr.setData(records);
        return mr;
    }

}
