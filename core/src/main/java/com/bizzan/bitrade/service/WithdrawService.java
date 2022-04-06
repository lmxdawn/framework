package com.bizzan.bitrade.service;

import com.bizzan.bitrade.constant.TransactionType;
import com.bizzan.bitrade.dao.MemberWalletDao;
import com.bizzan.bitrade.dao.WithdrawDao;
import com.bizzan.bitrade.entity.Coin;
import com.bizzan.bitrade.entity.MemberTransaction;
import com.bizzan.bitrade.entity.Withdraw;
import com.bizzan.bitrade.exception.InformationExpiredException;
import com.bizzan.bitrade.pagination.Criteria;
import com.bizzan.bitrade.pagination.Restrictions;
import com.bizzan.bitrade.service.Base.BaseService;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class WithdrawService extends BaseService<Withdraw> {

    @Autowired
    private WithdrawDao withdrawDao;

    @Autowired
    private MemberWalletDao memberWalletDao;
    @Autowired
    private MemberTransactionService memberTransactionService;

    public Withdraw findOne(Integer id) {
        return withdrawDao.findFirstById(id);
    }

    public Page<Withdraw> findAll(Predicate predicate, Pageable pageable) {
        return withdrawDao.findAll(predicate, pageable);
    }

    public Iterable<Withdraw> findAllOut(Predicate predicate) {
        return withdrawDao.findAll(predicate, new Sort(Sort.Direction.DESC, "id"));
    }

    public Page<Withdraw> findAllByMemberId(Integer memberId, int page, int pageSize) {
        Sort orders = Criteria.sortStatic("id.desc");
        PageRequest pageRequest = new PageRequest(page, pageSize, orders);
        Criteria<Withdraw> specification = new Criteria<>();
        specification.add(Restrictions.eq("memberid", memberId, false));
        return withdrawDao.findAll(specification, pageRequest);
    }

    @Transactional
    public boolean create(Withdraw withdraw) throws Exception {

        Coin coin = new Coin();
        coin.setName(withdraw.getCoinname());

        // 冻结用户资产
        int ret = memberWalletDao.freezeBalanceByMemberId(withdraw.getMemberid(), coin, BigDecimal.valueOf(withdraw.getMoney()));
        if (ret <= 0) {
            throw new InformationExpiredException("Information Expired");
        }

        withdrawDao.save(withdraw);

        return true;
    }

    @Transactional
    public void save(Withdraw withdraw) {
        // 如果是驳回，则解冻余额
        if (withdraw.getStatus() == -1) {
            Coin coin = new Coin();
            coin.setName(withdraw.getCoinname());
            memberWalletDao.unfreezeBalanceByMemberId(withdraw.getMemberid(), coin, BigDecimal.valueOf(withdraw.getMoney()));
        } else if (withdraw.getStatus() == 2) {
            // 如果是人工处理
            Coin coin = new Coin();
            coin.setName(withdraw.getCoinname());
            memberWalletDao.noUnfreezeBalanceByMemberId(withdraw.getMemberid(), coin, BigDecimal.valueOf(withdraw.getMoney()));

            MemberTransaction transaction = new MemberTransaction();
            transaction.setAmount(BigDecimal.valueOf(withdraw.getMoney()));
            transaction.setSymbol(coin.getName());
            transaction.setMemberId(withdraw.getMemberid().longValue());
            transaction.setType(TransactionType.WITHDRAW);
            transaction.setFee(BigDecimal.ZERO);
            memberTransactionService.save(transaction);
        }
        withdrawDao.update(withdraw.getId(), withdraw.getStatus(), withdraw.getWithdrawinfo(), withdraw.getHash(), withdraw.getProcesstime());
    }

}
