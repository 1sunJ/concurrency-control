package com._sunj.concurrency_demo.service;

import com._sunj.concurrency_demo.entities.MemberV1;
import com._sunj.concurrency_demo.repository.MemberV1Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferV1Service {

    private final MemberV1Repository memberV1Repository;

    /**
     * 동시성을 고려하지 않은 거래
     */
    @Transactional
    public void transfer1(Long fromMemberId, Long toMemberId, int amount) {
        if (fromMemberId.equals(toMemberId)) {
            throw new IllegalArgumentException("송금 회원 id와 수신 회원 id가 같을 수 없습니다.");
        }

        MemberV1 fromMember = memberV1Repository.findById(fromMemberId).orElseThrow(NoSuchElementException::new);
        MemberV1 toMember = memberV1Repository.findById(toMemberId).orElseThrow(NoSuchElementException::new);

        fromMember.withdraw(amount);
        toMember.deposit(amount);
    }

    /**
     * 비관적 락
     */
    @Transactional
    public void transfer2(Long fromMemberId, Long toMemberId, int amount) {
        long start = System.currentTimeMillis(); // 트랜잭션 시작 시간 측정
        if (fromMemberId.equals(toMemberId)) {
            throw new IllegalArgumentException("송금 회원 id와 수신 회원 id가 같을 수 없습니다.");
        }

        // 데이터 조회 1 : 각각의 조회 -> 교착 상태를 유발할 가능성이 있음
//        MemberV1 fromMember = memberV1Repository.findByIdForUpdate(fromMemberId).orElseThrow(NoSuchElementException::new);
//        MemberV1 toMember = memberV1Repository.findByIdForUpdate(toMemberId).orElseThrow(NoSuchElementException::new);

        // 데이터 조회 2 : 한 번에 조회 + 정렬하여 조회 -> 상대적 안전
        Long smallerId = fromMemberId < toMemberId ? fromMemberId : toMemberId;
        Long biggerId = smallerId.equals(fromMemberId) ? toMemberId : fromMemberId;
        List<MemberV1> members = memberV1Repository.findByIdsForUpdate(smallerId, biggerId);

        MemberV1 fromMember = members.stream().filter(m -> m.getId().equals(fromMemberId)).findFirst().orElseThrow(NoSuchElementException::new);
        MemberV1 toMember = members.stream().filter(m -> m.getId().equals(toMemberId)).findFirst().orElseThrow(NoSuchElementException::new);

        fromMember.withdraw(amount);
        toMember.deposit(amount);

        long end = System.currentTimeMillis(); // 트랜잭션 종료 시간 측정
        log.info("Transaction time for transfer2: {} ms", (end - start));

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
