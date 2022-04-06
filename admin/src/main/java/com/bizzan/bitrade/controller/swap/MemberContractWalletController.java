package com.bizzan.bitrade.controller.swap;

import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.alibaba.fastjson.JSON;
import com.bizzan.bitrade.annotation.AccessLog;
import com.bizzan.bitrade.constant.AdminModule;
import com.bizzan.bitrade.constant.PageModel;
import com.bizzan.bitrade.controller.common.BaseAdminController;
import com.bizzan.bitrade.entity.*;
import com.bizzan.bitrade.model.screen.ContractOrderEntrustScreen;
import com.bizzan.bitrade.model.screen.MemberContractWalletScreen;
import com.bizzan.bitrade.model.vo.MemberContractWalletOutVO;
import com.bizzan.bitrade.model.vo.MemberContractWalletVO;
import com.bizzan.bitrade.service.ContractCoinService;
import com.bizzan.bitrade.service.MemberContractWalletService;
import com.bizzan.bitrade.service.MemberService;
import com.bizzan.bitrade.util.ExcelUtil;
import com.bizzan.bitrade.util.MessageResult;
import com.bizzan.bitrade.util.PredicateUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 持仓管理
 */
@RestController
@RequestMapping("/swap/position")
@Slf4j
public class MemberContractWalletController extends BaseAdminController {
    @Autowired
    private MemberContractWalletService memberContractWalletService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ContractCoinService contractCoinService;

    @RequiresPermissions("swap:position:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "永续合约用户持仓管理 列表")
    public MessageResult detail(
            PageModel pageModel,
            MemberContractWalletScreen screen,
            HttpServletResponse response) throws IOException {
        if (pageModel.getDirection() == null && pageModel.getProperty() == null) {
            ArrayList<Sort.Direction> directions = new ArrayList<>();
            directions.add(Sort.Direction.DESC);
            pageModel.setDirection(directions);
            List<String> property = new ArrayList<>();
            property.add("usdtBalance"); // 默认金额排序
            pageModel.setProperty(property);
        }
        //获取查询条件
        Predicate predicate = getPredicate(screen);

        // 导出
        if (screen.getIsOut() == 1) {
            Iterable<MemberContractWallet> allOut = memberContractWalletService.findAllOut(predicate);
            Set<Long> memberSet = new HashSet<>();
            allOut.forEach(v -> {
                memberSet.add(v.getMemberId());
            });
            Map<Long, Member> memberMap = memberService.mapByMemberIds(new ArrayList<>(memberSet));

            List<MemberContractWalletOutVO> voList = new ArrayList<>();
            allOut.forEach(v -> {
                MemberContractWalletOutVO vo = new MemberContractWalletOutVO();
                vo.setMemberId(v.getMemberId());
                vo.setSymbol(v.getContractCoin().getSymbol());
                vo.setUsdtBalance(v.getUsdtBalance().toPlainString());
                vo.setUsdtFrozenBalance(v.getUsdtFrozenBalance().toPlainString());

                ContractOrderPattern usdtPattern = v.getUsdtPattern();
                String strUsdtPattern = "";
                if (usdtPattern == ContractOrderPattern.CROSSED) {
                    strUsdtPattern = "全仓";
                } else if (usdtPattern == ContractOrderPattern.FIXED) {
                    strUsdtPattern = "逐仓";
                } else {
                    strUsdtPattern = "--";
                }
                vo.setUsdtPattern(strUsdtPattern);

                String usdtBuyPositionMoney = "0.00 | 0.00%";
                BigDecimal usdtBuyPosition = v.getUsdtBuyPosition();
                BigDecimal usdtFrozenBuyPosition = v.getUsdtFrozenBuyPosition();
                BigDecimal currentPrice = v.getCurrentPrice();
                BigDecimal usdtBuyPrice = v.getUsdtBuyPrice();
                BigDecimal usdtShareNumber = v.getUsdtShareNumber();
                BigDecimal usdtBuyPrincipalAmount = v.getUsdtBuyPrincipalAmount();
                if (usdtBuyPosition.compareTo(BigDecimal.ZERO) > 0 && usdtFrozenBuyPosition.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal buyPl = currentPrice.divide(usdtBuyPrice, BigDecimal.ROUND_DOWN).multiply(usdtBuyPosition.add(usdtFrozenBuyPosition)).multiply(usdtShareNumber);
                    BigDecimal percent = buyPl.divide(usdtBuyPrincipalAmount, 4, BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100));
                    usdtBuyPositionMoney = buyPl.setScale(2, BigDecimal.ROUND_DOWN).toPlainString() + "   |   " + percent.setScale(2, BigDecimal.ROUND_DOWN) + "%";
                }
                vo.setUsdtBuyPositionMoney(usdtBuyPositionMoney);

                vo.setUsdtBuyLeverage("多" + v.getUsdtBuyLeverage().stripTrailingZeros().toPlainString() + "X");

                vo.setUsdtBuyPosition(usdtBuyPosition.toPlainString());
                vo.setUsdtBuyPrincipalAmount(usdtBuyPrincipalAmount.setScale(2, BigDecimal.ROUND_DOWN).toPlainString());
                vo.setUsdtFrozenBuyPosition(usdtFrozenBuyPosition.toPlainString());

                String usdtSellPositionMoney = "0.00 | 0.00%";
                BigDecimal usdtSellPosition = v.getUsdtSellPosition();
                BigDecimal usdtFrozenSellPosition = v.getUsdtFrozenSellPosition();
                BigDecimal usdtSellPrice = v.getUsdtSellPrice();
                BigDecimal usdtSellPrincipalAmount = v.getUsdtSellPrincipalAmount();
                if (usdtSellPosition.compareTo(BigDecimal.ZERO) > 0 && usdtFrozenSellPosition.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal sellPl = currentPrice.divide(usdtSellPrice, BigDecimal.ROUND_DOWN).multiply(usdtSellPosition.add(usdtFrozenSellPosition)).multiply(usdtShareNumber);
                    BigDecimal percent = sellPl.divide(usdtSellPrincipalAmount, 4, BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100));
                    usdtSellPositionMoney = sellPl.setScale(2, BigDecimal.ROUND_DOWN).toPlainString() + "   |   " + percent.setScale(2, BigDecimal.ROUND_DOWN) + "%";
                }
                vo.setUsdtSellPositionMoney(usdtSellPositionMoney);

                vo.setUsdtSellLeverage("空" + v.getUsdtSellLeverage().stripTrailingZeros().toPlainString() + "X");

                vo.setUsdtSellPosition(v.getUsdtSellPosition().toPlainString());
                vo.setUsdtSellPrincipalAmount(usdtSellPrincipalAmount.setScale(2, BigDecimal.ROUND_DOWN).toPlainString());
                vo.setUsdtFrozenSellPosition(v.getUsdtFrozenSellPosition().toPlainString());

                Long memberId = vo.getMemberId();
                if (memberMap.containsKey(memberId)) {
                    Member member = memberMap.get(memberId);
                    vo.setEmail(member.getEmail());
                    vo.setMobilePhone(member.getMobilePhone());
                    vo.setRealName(member.getRealName());
                }
                voList.add(vo);
            });

            ExcelUtil.listToExcel(voList, MemberContractWalletOutVO.class.getDeclaredFields(), response.getOutputStream());

            return null;

        }



        Page<MemberContractWallet> all = memberContractWalletService.findAll(predicate, pageModel.getPageable());

        List<Long> memberIds = all.getContent().stream().distinct().map(MemberContractWallet::getMemberId).collect(Collectors.toList());
        Map<Long, Member> memberMap = memberService.mapByMemberIds(memberIds);

        Page<MemberContractWalletVO> page = all.map(v -> {
            MemberContractWalletVO vo = new MemberContractWalletVO();
            BeanUtils.copyProperties(v, vo);
            Long memberId = vo.getMemberId();
            if (memberMap.containsKey(memberId)) {
                Member member = memberMap.get(memberId);
                vo.setEmail(member.getEmail());
                vo.setMobilePhone(member.getMobilePhone());
                vo.setRealName(member.getRealName());
            }
            return vo;
        });


        // 获取最新价格
        String serviceName = "SWAP-API";
        String marketUrl = "http://" + serviceName + "/swap/symbol-thumb";
//        ResponseEntity<List> thumbsResult = restTemplate.getForEntity(marketUrl, List.class);
//        List<CoinThumb> thumbList = (List<CoinThumb>)thumbsResult.getBody();

        ParameterizedTypeReference<List<CoinThumb>> typeRef = new ParameterizedTypeReference<List<CoinThumb>>() {};
        ResponseEntity<List<CoinThumb>> responseEntity = restTemplate.exchange(marketUrl, HttpMethod.POST, new HttpEntity<>(null), typeRef);
        List<CoinThumb> thumbList =responseEntity.getBody();

        List<MemberContractWalletVO> list = page.getContent();
        for(MemberContractWalletVO wallet :list) {
            for(int i = 0; i < thumbList.size(); i++) {
                CoinThumb thumb = thumbList.get(i);
                if(wallet.getContractCoin().getSymbol().equals(thumb.getSymbol())) {
                    wallet.setCurrentPrice(thumb.getClose());
                }
            }

            // 设置CNY / USDT汇率
            wallet.setCnyRate(BigDecimal.valueOf(7));
        }
        return success(page);
    }

    private Predicate getPredicate(MemberContractWalletScreen screen) {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        QMemberContractWallet qMemberContractWallet = QMemberContractWallet.memberContractWallet;
        if (screen.getContractId() != null) {
            ContractCoin coin = contractCoinService.findOne(screen.getContractId());
            booleanExpressions.add(qMemberContractWallet.contractCoin.eq(coin));
        }
        if(screen.getMemberId() != null) {
            booleanExpressions.add(qMemberContractWallet.memberId.eq(screen.getMemberId()));
        }
        if(StringUtils.isNotEmpty(screen.getPhone())) {
            Member member = memberService.findByPhone(screen.getPhone());
            booleanExpressions.add(qMemberContractWallet.memberId.eq(member.getId()));
        }
        if(StringUtils.isNotEmpty(screen.getEmail())) {
            Member member = memberService.findByEmail(screen.getEmail());
            booleanExpressions.add(qMemberContractWallet.memberId.eq(member.getId()));
        }
        if(screen.getUsdtBalance() != null) {
            booleanExpressions.add(qMemberContractWallet.usdtBalance.goe(screen.getUsdtBalance()));
        }
        if(screen.getUsdtFrozenBalance() != null) {
            booleanExpressions.add(qMemberContractWallet.usdtFrozenBalance.goe(screen.getUsdtFrozenBalance()));
        }
        if(screen.getUsdtPattern() != null) {
            booleanExpressions.add(qMemberContractWallet.usdtPattern.eq(screen.getUsdtPattern()));
        }
        if(screen.getUsdtBuyLeverage() != null) {
            booleanExpressions.add(qMemberContractWallet.usdtBuyLeverage.goe(screen.getUsdtBuyLeverage()));
        }
        if(screen.getUsdtSellLeverage() != null) {
            booleanExpressions.add(qMemberContractWallet.usdtSellLeverage.goe(screen.getUsdtSellLeverage()));
        }
        if(screen.getUsdtBuyPosition() != null) {
            booleanExpressions.add(qMemberContractWallet.usdtBuyPosition.goe(screen.getUsdtBuyPosition()));
        }
        if(screen.getUsdtFrozenBuyPosition() != null) {
            booleanExpressions.add(qMemberContractWallet.usdtFrozenBuyPosition.goe(screen.getUsdtFrozenBuyPosition()));
        }
        if(screen.getUsdtBuyPrincipalAmount() != null) {
            booleanExpressions.add(qMemberContractWallet.usdtBuyPrincipalAmount.goe(screen.getUsdtBuyPrincipalAmount()));
        }
        if(screen.getUsdtSellPosition() != null) {
            booleanExpressions.add(qMemberContractWallet.usdtSellPosition.goe(screen.getUsdtSellPosition()));
        }
        if(screen.getUsdtFrozenSellPosition() != null) {
            booleanExpressions.add(qMemberContractWallet.usdtFrozenSellPosition.goe(screen.getUsdtFrozenSellPosition()));
        }
        if(screen.getUsdtSellPrincipalAmount() != null) {
            booleanExpressions.add(qMemberContractWallet.usdtSellPrincipalAmount.goe(screen.getUsdtSellPrincipalAmount()));
        }

        return PredicateUtils.getPredicate(booleanExpressions);
    }


    /**
     * 强制市价平仓
     * @param walletId
     * @return
     */
    @RequiresPermissions("swap:order:force-close")
    @PostMapping("force-close")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "永续合约用户持仓管理 强制平仓")
    public MessageResult forceClose(Long walletId) {
        MemberContractWallet wallet = memberContractWalletService.findOne(walletId);
        if(wallet == null) {
            return MessageResult.error("撤销委托失败");
        }
        return MessageResult.success("操作成功");
    }

}
