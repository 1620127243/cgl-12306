package com.cgl.train.business.feign;

import com.cgl.train.common.req.MemberTicketReq;
import com.cgl.train.common.resp.CommonResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

// @FeignClient("member")
@FeignClient(name = "member")
public interface MemberFeign {

    @GetMapping("/member/feign/ticket/save")
    CommonResp<Object> save(@RequestBody MemberTicketReq req);

}
