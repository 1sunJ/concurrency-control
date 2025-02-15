package com._sunj.concurrency_demo.service;

import com._sunj.concurrency_demo.entities.MemberV2;
import com._sunj.concurrency_demo.repository.MemberV2Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferV2Service {

    private final MemberV2Repository memberV2Repository;

    private static final int MAX_RETRIES = 3; // 최대 3회 재시도

    /**
     * 낙관적 락
     */
    public void transferWithRetry(Long fromMemberId, Long toMemberId, int amount) {
        long start = System.currentTimeMillis(); // 트랜잭션 시작 시간 측정

        if (fromMemberId.equals(toMemberId)) {
            throw new IllegalArgumentException("송금 회원 id와 수신 회원 id가 같을 수 없습니다.");
        }

        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                performTransfer(fromMemberId, toMemberId, amount);
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                retryCount ++;
                if (retryCount == MAX_RETRIES) {
                    throw new RuntimeException("반복된 트랜잭션 충돌로 실패 처리합니다.");
                }
            }
        }

        long end = System.currentTimeMillis(); // 트랜잭션 종료 시간 측정
        log.info("Transaction time for transfer2: {} ms", (end - start));

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Transactional
    public void performTransfer(Long fromMemberId, Long toMemberId, int amount) {
        MemberV2 fromMember = memberV2Repository.findById(fromMemberId).orElseThrow(NoSuchElementException::new);
        MemberV2 toMember = memberV2Repository.findById(toMemberId).orElseThrow(NoSuchElementException::new);
        fromMember.withdraw(amount);
        toMember.deposit(amount);
    }

}
