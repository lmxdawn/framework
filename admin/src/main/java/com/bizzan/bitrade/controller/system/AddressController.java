package com.bizzan.bitrade.controller.system;

import com.bizzan.bitrade.annotation.AccessLog;
import com.bizzan.bitrade.constant.AdminModule;
import com.bizzan.bitrade.constant.PageModel;
import com.bizzan.bitrade.constant.SysConstant;
import com.bizzan.bitrade.controller.common.BaseAdminController;
import com.bizzan.bitrade.dto.CoinprotocolDTO;
import com.bizzan.bitrade.entity.Addressext;
import com.bizzan.bitrade.entity.Admin;
import com.bizzan.bitrade.entity.QAddressext;
import com.bizzan.bitrade.model.screen.AddressextScreen;
import com.bizzan.bitrade.model.vo.AddressextImportVO;
import com.bizzan.bitrade.service.AddressextService;
import com.bizzan.bitrade.service.CoinprotocolService;
import com.bizzan.bitrade.util.*;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * 地址管理
 */
@Slf4j
@RestController
@RequestMapping("/system/address")
public class AddressController extends BaseAdminController {

    @Autowired
    private CoinprotocolService coinprotocolService;

    @Autowired
    private AddressextService addressextService;

    @RequiresPermissions("system:address:protocol-list")
    @GetMapping("/protocol-list")
    @AccessLog(module = AdminModule.SYSTEM, operation = "地址管理里获取币种协议列表")
    public MessageResult protocolList() {

        List<CoinprotocolDTO> list = coinprotocolService.list();

        return success(list);
    }

    @RequiresPermissions("system:address:page-query")
    @PostMapping("/page-query")
    @AccessLog(module = AdminModule.SYSTEM, operation = "获取地址列表")
    public MessageResult pageQuery(PageModel pageModel, AddressextScreen addressextScreen) {

        List<BooleanExpression> booleanExpressions = new ArrayList<>();

        if (addressextScreen.getProtocol() != -1) {
            booleanExpressions.add(QAddressext.addressext.coinprotocol.eq(addressextScreen.getProtocol()));
        }
        if (addressextScreen.getStatus() != -1) {
            booleanExpressions.add(QAddressext.addressext.status.eq(addressextScreen.getStatus()));
        }

        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);

        Page<Addressext> all = addressextService.findAll(predicate, pageModel.getPageable());

        all = all.map(item -> {
            try {
                item.setAddress(DESEncryptUtil.DecryptData(item.getAddress()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return item;
        });

        return success(all);
    }

    @RequiresPermissions("system:address:merge")
    @PostMapping("/merge")
    @AccessLog(module = AdminModule.SYSTEM, operation = "导入地址")
    public MessageResult merge(
            @SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin,
            @Valid AddressextImportVO addressextImportVO, BindingResult bindingResult
    ) throws Exception {

        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }

        Boolean aBoolean = GoogleAuthenticator.authCode(addressextImportVO.getGoogleCode(), admin.getGoogleKey());
        if (!aBoolean) {
            return error("Google验证码错误");
        }

        String[] lines = addressextImportVO.getTxt().split("\\r?\\n");
        List<Addressext> addressexts = new ArrayList<>();
        for (String line : lines) {
            String s = DESEncryptUtil.DecryptData(line, addressextImportVO.getKey());

            if (s.contains("0x")) {
                s = s.toLowerCase();
            }

            Addressext addressext = new Addressext();
            addressext.setStatus(0);
            addressext.setAddress(DESEncryptUtil.EncryptData(s));
            addressext.setCoinprotocol(addressextImportVO.getProtocol());
            addressext.setMemberid(0);
            addressexts.add(addressext);
        }

        addressextService.addBatch(addressexts);

        result = success("操作成功");
        return result;
    }

}
