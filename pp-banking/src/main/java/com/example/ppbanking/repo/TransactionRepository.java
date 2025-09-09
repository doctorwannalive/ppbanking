package com.example.ppbanking.repo;

import com.example.ppbanking.domain.Transaction;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("select t from Transaction t where t.senderId = :uid or t.receiverId = :uid order by t.createdAt desc")
    List<Transaction> findAllForUser(@Param("uid") Long userId);
}