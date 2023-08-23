package com.cgl.train.member.controller;

import com.cgl.train.common.response.CommonResp;
import com.cgl.train.member.req.MemberLoginReq;
import com.cgl.train.member.req.MemberSendMsgReq;
import com.cgl.train.member.resp.MemberLoginResp;
import com.cgl.train.member.service.MemberService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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
//    @PostMapping("/register")
//    public CommonResp<Long> register(@Valid MemberRegisterReq req){
//        CommonResp<Long> commonResp=new CommonResp<>();
//        commonResp.setContent(memberService.register(req));
//        return commonResp;
//    }
    @PostMapping("/sendMsg")
    public CommonResp<String> sentMsg(@Valid @RequestBody MemberSendMsgReq req, HttpSession session){
        String code=memberService.sendMsg(req,session);
        return new CommonResp<>(code);
    }
    @PostMapping("/login")
    public CommonResp<MemberLoginResp> login(@Valid @RequestBody MemberLoginReq req,HttpSession session){
        MemberLoginResp resp=memberService.login(req,session);
        return new CommonResp<>(resp);

    }
}
