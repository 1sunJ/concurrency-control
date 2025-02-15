package com._sunj.concurrency_demo.repository;

import com._sunj.concurrency_demo.entities.MemberV1;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberV1Repository extends JpaRepository<MemberV1, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MemberV1 m where m.id= :id")
    Optional<MemberV1> findByIdsForUpdate(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MemberV1 m where m.id= :id1 or m.id= :id2")
    List<MemberV1> findByIdsForUpdate(Long id1, Long id2);

}
