package com._sunj.concurrency_demo.repository;

import com._sunj.concurrency_demo.entities.MemberV2;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberV2Repository extends JpaRepository<MemberV2, Long> {
}
