package com.bizzan.bitrade.controller.system;

import com.bizzan.bitrade.annotation.AccessLog;
import com.bizzan.bitrade.constant.AdminModule;
import com.bizzan.bitrade.constant.PageModel;
import com.bizzan.bitrade.controller.common.BaseAdminController;
import com.bizzan.bitrade.dto.CoinDTO;
import com.bizzan.bitrade.dto.CoinprotocolDTO;
import com.bizzan.bitrade.entity.Automainconfig;
import com.bizzan.bitrade.entity.Coinext;
import com.bizzan.bitrade.service.AutomainconfigService;
import com.bizzan.bitrade.service.CoinService;
import com.bizzan.bitrade.service.CoinextService;
import com.bizzan.bitrade.service.CoinprotocolService;
import com.bizzan.bitrade.util.BindingResultUtil;
import com.bizzan.bitrade.util.MessageResult;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 币种扩展管理
 */
@Slf4j
@RestController
@RequestMapping("/system/automainconfig")
public class AutomainconfigController extends BaseAdminController {

    @Autowired
    private CoinService coinService;

    @Autowired
    private CoinprotocolService coinprotocolService;

    @Autowired
    private AutomainconfigService automainconfigService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @RequiresPermissions("system:automainconfig:coin-list")
    @GetMapping("/coin-list")
    @AccessLog(module = AdminModule.SYSTEM, operation = "归集配置里获取币种列表")
    public MessageResult coinList() {

        List<CoinDTO> list = coinService.list();

        return success(list);
    }

    @RequiresPermissions("system:automainconfig:protocol-list")
    @GetMapping("/protocol-list")
    @AccessLog(module = AdminModule.SYSTEM, operation = "归集配置里获取币种协议列表")
    public MessageResult protocolList() {

        List<CoinprotocolDTO> list = coinprotocolService.list();

        return success(list);
    }

    @RequiresPermissions("system:automainconfig:page-query")
    @PostMapping("/page-query")
    @AccessLog(module = AdminModule.SYSTEM, operation = "获取归集配置列表")
    public MessageResult pageQuery(PageModel pageModel) {

        BooleanExpression predicate = null;

        Page<Automainconfig> all = automainconfigService.findAll(predicate, pageModel.getPageable());
        return success(all);
    }

    @RequiresPermissions("system:automainconfig:merge")
    @PostMapping("/merge")
    @AccessLog(module = AdminModule.SYSTEM, operation = "创建/修改归集配置")
    public MessageResult merge(@Valid Automainconfig automainconfig, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }

        // 查询是否存在
        Automainconfig one = automainconfigService.findOne(automainconfig.getCoinname(), automainconfig.getProtocol());
        if (automainconfig.getId() != null) {
            if (one != null && !one.getId().equals(automainconfig.getId())) {
                result = error("当前协议的币种已存在");
                return result;
            }
        } else if (one != null) {
            result = error("当前协议的币种已存在");
            return result;
        }

        // 删除redis缓存
        redisTemplate.delete("automainconfig");

        automainconfig = automainconfigService.save(automainconfig);
        result = success("操作成功");
        result.setData(automainconfig);
        return result;
    }

}
