package com.cgl.train.business.controller.admin;

import com.cgl.train.business.req.ConfirmOrderDoReq;
import com.cgl.train.business.req.ConfirmOrderQueryReq;
import com.cgl.train.business.resp.ConfirmOrderQueryResp;
import com.cgl.train.business.service.ConfirmOrderService;
import com.cgl.train.common.resp.CommonResp;
import com.cgl.train.common.resp.PageResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/confirm-order")
public class ConfirmOrderAdminController {

    @Resource
    private ConfirmOrderService confirmOrderService;

    @PostMapping("/do")
    public CommonResp<Object> doConfirm(@Valid @RequestBody ConfirmOrderDoReq req) {
        confirmOrderService.doConfirm(req);
        return new CommonResp<>();
    }
    @GetMapping("/query-list")
    public CommonResp<PageResp<ConfirmOrderQueryResp>> queryList(@Valid ConfirmOrderQueryReq req) {
        PageResp<ConfirmOrderQueryResp> list = confirmOrderService.queryList(req);
        return new CommonResp<>(list);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id) {
        confirmOrderService.delete(id);
        return new CommonResp<>();
    }

}
