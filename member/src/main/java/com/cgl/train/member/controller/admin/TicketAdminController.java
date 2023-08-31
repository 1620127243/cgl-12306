package com.cgl.train.member.controller.admin;

import com.cgl.train.common.resp.CommonResp;
import com.cgl.train.common.resp.PageResp;
import com.cgl.train.member.req.TicketQueryReq;
import com.cgl.train.member.resp.TicketQueryResp;
import com.cgl.train.member.service.TicketService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/ticket")
public class TicketAdminController {

    @Resource
    private TicketService ticketService;

    @GetMapping("/query-list")
    public CommonResp<PageResp<TicketQueryResp>> queryList(@Valid TicketQueryReq req) {
        PageResp<TicketQueryResp> list = ticketService.queryList(req);
        return new CommonResp<>(list);
    }

}
