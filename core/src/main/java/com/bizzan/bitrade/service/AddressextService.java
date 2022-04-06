package com.bizzan.bitrade.service;

import com.bizzan.bitrade.dao.AddressextDao;
import com.bizzan.bitrade.entity.Addressext;
import com.bizzan.bitrade.service.Base.BaseService;
import com.bizzan.bitrade.util.JDBCUtils;
import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class AddressextService extends BaseService<Addressext> {

    @Autowired
    private AddressextDao addressextDao;
    @Autowired
    JDBCUtils jdbcUtils;

    public Addressext read(Integer memberid, Integer coinprotocol) {
        return addressextDao.findFirstByMemberidAndCoinprotocol(memberid, coinprotocol);
    }

    // 查询未使用的
    public Addressext notUsed(Integer coinprotocol) {
        return addressextDao.findFirstByCoinprotocolAndStatus(coinprotocol, 0);
    }

    // 创建
    @Transactional
    public Integer create(Integer id, Integer memberid) {
        return addressextDao.updateMemberIdById(id, memberid);
    }

    // 批量导入
    @Transactional
    public void addBatch(List<Addressext> addressexts) {
        jdbcUtils.addressAddBatch(addressexts);
    }

    public Page<Addressext> findAll(Predicate predicate, Pageable pageable) {
        return addressextDao.findAll(predicate, pageable);
    }

}
