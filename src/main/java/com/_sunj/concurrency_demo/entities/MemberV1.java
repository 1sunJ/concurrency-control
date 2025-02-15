package com._sunj.concurrency_demo.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Slf4j
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MemberV1 {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_v1_id")
    private Long id;

    private String name;

    private int balance;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDt;

    @UpdateTimestamp
    private LocalDateTime updatedDt;

    /********************* method *********************/

    public MemberV1(String name) {
        this.name = name;
    }

    public void withdraw(int amount) {
        if (balance < amount) {
            throw new IllegalArgumentException("잔액 부족");
        }
        balance -= amount;
    }

    public void deposit(int amount) {
        balance += amount;
    }

    @PostUpdate
    public void logUpdate() {
        log.info("Updated MemberV1 ID: {} -> Balance: {}", id, balance);
    }

}
