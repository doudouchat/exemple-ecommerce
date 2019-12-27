package com.exemple.ecommerce.resource.core.dao;

import java.util.UUID;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.exemple.ecommerce.resource.account.model.AccountHistory;

@Dao
public interface AccountHistoryDao {

    @Select
    PagingIterable<AccountHistory> findById(UUID id);
}
