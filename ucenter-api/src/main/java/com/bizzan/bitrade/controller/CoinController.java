package com.bizzan.bitrade.controller;

import com.bizzan.bitrade.dto.CoinDTO;
import com.bizzan.bitrade.dto.CoinextDTO;
import com.bizzan.bitrade.entity.Coinext;
import com.bizzan.bitrade.entity.transform.AuthMember;
import com.bizzan.bitrade.service.CoinextService;
import com.bizzan.bitrade.service.MemberWalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import com.bizzan.bitrade.constant.PageModel;
import com.bizzan.bitrade.controller.BaseController;
import com.bizzan.bitrade.entity.Coin;
import com.bizzan.bitrade.service.CoinService;
import com.bizzan.bitrade.util.MessageResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bizzan.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * @author Jammy
 * @Description: coin
 * @date 2019/4/214:20
 */
@RestController
@RequestMapping("coin")
public class CoinController extends BaseController {
    @Autowired
    private CoinService coinService;
    @Autowired
    private CoinextService coinextService;
    @Autowired
    private MemberWalletService memberWalletService;

    @GetMapping("legal")
    public MessageResult legal() {
        List<Coin> legalAll = coinService.findLegalAll();
        return success(legalAll);
    }

    @GetMapping("legal/page")
    public MessageResult findLegalCoinPage(PageModel pageModel) {
        Page all = coinService.findLegalCoinPage(pageModel);
        return success(all);
    }

    @RequestMapping("supported")
    public List<Map<String, String>> findCoins() {
        List<Coin> coins = coinService.findAll();
        List<Map<String, String>> result = new ArrayList<>();
        coins.forEach(coin -> {
            if (coin.getHasLegal().equals(Boolean.FALSE)) {
                Map<String, String> map = new HashMap<>();
                map.put("name", coin.getName());
                map.put("nameCn", coin.getNameCn());
                map.put("withdrawFee", String.valueOf(coin.getMinTxFee()));
                map.put("enableRecharge", String.valueOf(coin.getCanRecharge().getOrdinal()));
                map.put("minWithdrawAmount", String.valueOf(coin.getMinWithdrawAmount()));
                map.put("enableWithdraw", String.valueOf(coin.getCanWithdraw().getOrdinal()));
                result.add(map);
            }
        });
        return result;
    }

    // 查询所有币种
    @GetMapping("list")
    public MessageResult list() {
        List<CoinDTO> coinList = coinService.list();
        List<CoinextDTO> coinextList = coinextService.list();
        Map<String, Object> map = new HashMap<>();
        map.put("coinList", coinList);
        map.put("coinextList", coinextList);
        return success(map);
    }

    // 查询币种的余额
    @GetMapping("balance")
    public MessageResult balance(@SessionAttribute(SESSION_MEMBER) AuthMember user,
                                 @RequestParam(value = "coinName") String coinName) {
        Long memberid = user.getId();
        BigDecimal balance = memberWalletService.getBalance(memberid, coinName);
        return success(balance);
    }

}
