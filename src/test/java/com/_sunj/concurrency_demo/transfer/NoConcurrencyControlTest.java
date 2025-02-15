package com._sunj.concurrency_demo.transfer;

import com._sunj.concurrency_demo.entities.MemberV1;
import com._sunj.concurrency_demo.repository.MemberV1Repository;
import com._sunj.concurrency_demo.service.TransferV1Service;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SpringBootTest
public class NoConcurrencyControlTest {

    @Autowired
    private MemberV1Repository memberV1Repository;

    @Autowired
    private TransferV1Service transferV1Service;

    private final int THREAD_COUNT = 200;

    @BeforeEach
    public void setMemberData() {
        MemberV1 m1 = new MemberV1("1sunj");
        m1.deposit(100000);
        MemberV1 m2 = new MemberV1("2sunj");
        m2.deposit(100000);
        memberV1Repository.saveAll(List.of(m1, m2));
    }

    @Test
    public void testConcurrentUpdateWithoutLock() throws InterruptedException {
        Long memberId1 = 1L;
        Long memberId2 = 2L;
        MemberV1 m1 = memberV1Repository.findById(memberId1).orElseThrow(NoSuchElementException::new);
        MemberV1 m2 = memberV1Repository.findById(memberId2).orElseThrow(NoSuchElementException::new);
        int initialBalance1 = m1.getBalance();
        int initialBalance2 = m2.getBalance();

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        AtomicInteger transferSum = new AtomicInteger(0);
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int randomValue = new Random().nextInt(10) * 100;
            executorService.execute(() -> {
                log.info("randomValue : {}", randomValue);
                transferSum.addAndGet(randomValue);
                transferV1Service.transfer1(memberId1, memberId2, randomValue); // 1000원 이내의 랜덤 금액 송금
                latch.countDown();
            });
        }

        latch.await();
        executorService.shutdown();

        log.info("transferSum : {}", transferSum);
        m1 = memberV1Repository.findById(memberId1).orElseThrow(NoSuchElementException::new);
        m2 = memberV1Repository.findById(memberId2).orElseThrow(NoSuchElementException::new);
        int finalBalance1 = m1.getBalance();
        int finalBalance2 = m2.getBalance();

        log.info("==== Test Summary ====");
        log.info("Initial Balance1: {}", initialBalance1);
        log.info("Initial Balance2: {}", initialBalance2);
        log.info("Final Balance1: {}", finalBalance1);
        log.info("Final Balance2: {}", finalBalance2);
        log.info("=========================");

        Assertions.assertNotEquals(initialBalance1 - transferSum.get(), finalBalance1, "동시성 제어가 적절하게 되지 않았음");
        Assertions.assertNotEquals(initialBalance2 + transferSum.get(), finalBalance2, "동시성 제어가 적절하게 되지 않았음");
    }

}
