package com.cgl.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.cgl.train.common.exception.BusinessException;
import com.cgl.train.common.exception.BusinessExceptionEnum;
import com.cgl.train.common.util.JwtUtil;
import com.cgl.train.common.util.SnowUtil;
import com.cgl.train.member.domain.Member;
import com.cgl.train.member.domain.MemberExample;
import com.cgl.train.member.mapper.MemberMapper;
import com.cgl.train.member.req.MemberLoginReq;
import com.cgl.train.member.req.MemberSendMsgReq;
import com.cgl.train.member.resp.MemberLoginResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {
    private static final Logger LOG = LoggerFactory.getLogger(MemberService.class);
    private String myCode="";
    @Resource
    private MemberMapper memberMapper;
    public Integer count(){
        return (int) memberMapper.countByExample(null);
    }

    public String sendMsg(MemberSendMsgReq req){
        String mobile=req.getMobile();
        //生成验证码
        String code=RandomUtil.randomString(4);
        myCode=code;
//        session.setAttribute(mobile,code);
        LOG.info("生成短信验证码：{}", code);
        //保存短信记录表：手机号，短信验证码、有效期、是否已使用、业务类型、发送时间、使用时间
        LOG.info("保存短信记录表");
        //对接短信通道，发送短信
        LOG.info("对接短信通道");
        return code;

    }
    public MemberLoginResp login(MemberLoginReq req){
        String mobile=req.getMobile();
        String code=req.getCode();
        if(myCode.equals(code)){
            Member memberDB=selectByMobile(mobile);
            if(ObjectUtil.isEmpty(memberDB)){
                memberDB=register(mobile);
            }
            MemberLoginResp resp= BeanUtil.copyProperties(memberDB,MemberLoginResp.class);
            String token= JwtUtil.createToken(resp.getId(),resp.getMobile());
            resp.setToken(token);
            return resp;
        }else {
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_CODE_ERROR);
        }
//        Object codeInSession=session.getAttribute(mobile);
//        LOG.info(codeInSession.toString());
//        if(ObjectUtil.isNotEmpty(codeInSession)&&codeInSession.equals(code)){
//            Member memberDB=selectByMobile(mobile);
//            if(ObjectUtil.isEmpty(memberDB)){
//                memberDB=register(mobile);
//            }
//            MemberLoginResp resp= BeanUtil.copyProperties(memberDB,MemberLoginResp.class);
//            //token
//            return resp;
//        }else{
//            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_CODE_ERROR);
//        }

    }
    private Member register(String mobile){
        Member member=new Member();
        //雪花算法生成ID
        member.setId(SnowUtil.getSnowflakeNextId());
        member.setMobile(mobile);
        memberMapper.insert(member);
        return member;
    }
    private Member selectByMobile(String mobile){
        MemberExample example=new MemberExample();
        example.createCriteria().andMobileEqualTo(mobile);
        List<Member> list= memberMapper.selectByExample(example);
        if(CollUtil.isEmpty(list)) {
            return null;
        }else {
            return list.get(0);
        }
    }


}
