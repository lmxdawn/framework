package com.bizzan.bitrade.controller;

import com.bizzan.bitrade.entity.Addressext;
import com.bizzan.bitrade.entity.transform.AuthMember;
import com.bizzan.bitrade.service.AddressextService;
import com.bizzan.bitrade.util.DESEncryptUtil;
import com.bizzan.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.bizzan.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * @author Jammy
 * @Description: coin
 * @date 2019/4/214:20
 */
@RestController
@RequestMapping("/address")
public class AddressController extends BaseController {

    @Autowired
    private AddressextService addressextService;

    // 读取地址
    @GetMapping("read")
    public MessageResult read(@SessionAttribute(SESSION_MEMBER) AuthMember user,
                              @RequestParam(value = "coinprotocol") Integer coinprotocol) throws Exception {
        Integer memberid = (int) user.getId();
        Addressext read = addressextService.read(memberid, coinprotocol);

        if (read != null) {
            read.setAddress(DESEncryptUtil.DecryptData(read.getAddress()));
        }

        return success(read);
    }


    // 创建地址
    @PostMapping("create")
    public MessageResult create(@SessionAttribute(SESSION_MEMBER) AuthMember user,
                                @RequestParam(value = "coinprotocol") Integer coinprotocol) throws Exception {
        Integer memberid = (int) user.getId();
        Addressext read = addressextService.read(memberid, coinprotocol);

        if (read != null) {
            read.setAddress(DESEncryptUtil.DecryptData(read.getAddress()));
            return success(read);
        }

        Addressext addressext = addressextService.notUsed(coinprotocol);
        if (addressext == null) {
            return error(" network error");
        }
        addressext.setMemberid(memberid);
        addressext.setStatus(1);

        addressextService.create(addressext.getId(), addressext.getMemberid());

        addressext.setAddress(DESEncryptUtil.DecryptData(addressext.getAddress()));

        return success(addressext);
    }


}
