package com.cgl.train.member.controller;

import com.cgl.train.common.response.CommonResp;
import com.cgl.train.member.req.MemberRegisterReq;
import com.cgl.train.member.service.MemberService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
public class MemberController {
    @Resource
    private MemberService memberService;
    @GetMapping("/count")
    public CommonResp<Integer> getCount(){
        CommonResp<Integer> commonResp=new CommonResp<>();
        commonResp.setContent(memberService.count());
        return commonResp;
    }
    @PostMapping("/register")
    public CommonResp<Long> register(@Valid MemberRegisterReq req){
        CommonResp<Long> commonResp=new CommonResp<>();
        commonResp.setContent(memberService.register(req));
        return commonResp;
    }
}
