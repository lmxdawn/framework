package com.bizzan.bitrade.dao;

import com.bizzan.bitrade.dao.base.BaseDao;
import com.bizzan.bitrade.entity.Addressext;
import com.bizzan.bitrade.entity.Coinext;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AddressextDao extends BaseDao<Addressext> {

    Addressext findFirstByMemberidAndCoinprotocol(Integer memberid, Integer coinprotocol);

    Addressext findFirstByCoinprotocolAndStatus(Integer coinprotocol, Integer status);

    @Modifying
    @Query(value = "update Addressext a set a.memberid = (:memberid),a.status = 1 where a.id = (:id) and a.status = 0")
    Integer updateMemberIdById(@Param("id") Integer id, @Param("memberid") Integer memberid);

}
