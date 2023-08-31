package com.cgl.train.business.controller;

import com.cgl.train.business.req.ConfirmOrderDoReq;
import com.cgl.train.business.service.ConfirmOrderService;
import com.cgl.train.common.resp.CommonResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/confirm-order")
public class ConfirmOrderController {
    @Resource
    private ConfirmOrderService confirmOrderService;

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderController.class);

    @PostMapping("/do")
    public CommonResp<Object> doConfirm(@Valid @RequestBody ConfirmOrderDoReq req) {
        confirmOrderService.doConfirm(req);
        return new CommonResp<>();
    }



}
