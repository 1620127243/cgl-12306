package com.cgl.train.member.service;

import com.cgl.train.member.mapper.MemberMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    @Resource
    private MemberMapper memberMapper;
    public int count(){
        return (int) memberMapper.countByExample(null);
    }

}
