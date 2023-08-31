package com.cgl.train.member.controller;

import com.cgl.train.common.context.LoginMemberContext;
import com.cgl.train.common.resp.CommonResp;
import com.cgl.train.common.resp.PageResp;
import com.cgl.train.member.req.PassengerQueryReq;
import com.cgl.train.member.req.PassengerSaveReq;
import com.cgl.train.member.resp.PassengerQueryResp;
import com.cgl.train.member.service.PassengerService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/passenger")
public class PassengerController {
    @Resource
    private PassengerService passengerService;

    @PostMapping("/save")
    public CommonResp<Object> register(@Valid @RequestBody PassengerSaveReq req){
        passengerService.save(req);
        return new CommonResp<>();
    }
    @GetMapping("/query-list")
    public CommonResp<PageResp<PassengerQueryResp>> queryList(@Valid PassengerQueryReq req){
        req.setMemberId(LoginMemberContext.getId());
        PageResp<PassengerQueryResp> list = passengerService.queryList(req);
        return new CommonResp<>(list);

    }
    @GetMapping("/query-mine")
    public CommonResp<List<PassengerQueryResp>> queryMine() {
        List<PassengerQueryResp> list = passengerService.queryMine();
        return new CommonResp<>(list);
    }
    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        passengerService.delete(id);
        return new CommonResp<>();

    }

}
