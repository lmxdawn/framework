package com.bizzan.bitrade.dao;

import com.bizzan.bitrade.dao.base.BaseDao;
import com.bizzan.bitrade.entity.Withdraw;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface WithdrawDao extends BaseDao<Withdraw> {


    @Modifying
    @Query("update Withdraw a set a.status=:status, a.withdrawinfo=:withdrawinfo, a.hash=:hash, a.processtime=:processtime where a.id=:id and a.status=0 or a.status=3")
    void update(@Param("id") Integer id, @Param("status") Integer status, @Param("withdrawinfo") String withdrawinfo, @Param("hash") String hash, @Param("processtime") Long processtime);

    Withdraw findFirstById(Integer id);
}
