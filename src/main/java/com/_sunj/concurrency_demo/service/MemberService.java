package com._sunj.concurrency_demo.service;

import com._sunj.concurrency_demo.entities.MemberV1;
import com._sunj.concurrency_demo.entities.MemberV2;
import com._sunj.concurrency_demo.repository.MemberV1Repository;
import com._sunj.concurrency_demo.repository.MemberV2Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberV1Repository memberV1Repository;
    private final MemberV2Repository memberV2Repository;

    public MemberV1 findMemberV1(Long memberId) {
        return memberV1Repository.findById(memberId).orElseThrow(NoSuchElementException::new);
    }

    public MemberV2 findMemberV2(Long memberId) {
        return memberV2Repository.findById(memberId).orElseThrow(NoSuchElementException::new);
    }

}
