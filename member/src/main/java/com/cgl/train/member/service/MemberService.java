package com.cgl.train.member.service;

import cn.hutool.core.collection.CollUtil;
import com.cgl.train.common.exception.BusinessException;
import com.cgl.train.common.exception.BusinessExceptionEnum;
import com.cgl.train.member.domain.Member;
import com.cgl.train.member.domain.MemberExample;
import com.cgl.train.member.mapper.MemberMapper;
import com.cgl.train.member.req.MemberRegisterReq;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {
    @Resource
    private MemberMapper memberMapper;
    public Integer count(){
        return (int) memberMapper.countByExample(null);
    }
    public long register(MemberRegisterReq req){
        String mobile=req.getMobile();
        MemberExample example=new MemberExample();
        example.createCriteria().andMobileEqualTo(mobile);
        List<Member> list= memberMapper.selectByExample(example);
        if(CollUtil.isNotEmpty(list)) {
//            return list.get(0).getId();
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_EXIST);

        }
        Member member=new Member();
        member.setId(System.currentTimeMillis());
        member.setMobile(mobile);
        memberMapper.insert(member);
        return member.getId();
    }

}
