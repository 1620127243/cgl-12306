package com.cgl.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.cgl.train.business.domain.*;
import com.cgl.train.business.enums.ConfirmOrderStatusEnum;
import com.cgl.train.business.enums.SeatColEnum;
import com.cgl.train.business.enums.SeatTypeEnum;
import com.cgl.train.business.mapper.ConfirmOrderMapper;
import com.cgl.train.business.req.ConfirmOrderDoReq;
import com.cgl.train.business.req.ConfirmOrderQueryReq;
import com.cgl.train.business.req.ConfirmOrderTicketReq;
import com.cgl.train.business.resp.ConfirmOrderQueryResp;
import com.cgl.train.common.context.LoginMemberContext;
import com.cgl.train.common.exception.BusinessException;
import com.cgl.train.common.exception.BusinessExceptionEnum;
import com.cgl.train.common.resp.PageResp;
import com.cgl.train.common.util.SnowUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ConfirmOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderService.class);

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @Resource
    private DailyTrainTicketService dailyTrainTicketService;

    @Resource
    private DailyTrainCarriageService dailyTrainCarriageService;

    @Resource
    private DailyTrainSeatService dailyTrainSeatService;
    @Resource
    private AfterConfirmOrderService afterConfirmOrderService;

    public void save(ConfirmOrderDoReq req) {
        DateTime now = DateTime.now();
        ConfirmOrder confirmOrder = BeanUtil.copyProperties(req, ConfirmOrder.class);
        if (ObjectUtil.isNull(confirmOrder.getId())) {
            confirmOrder.setId(SnowUtil.getSnowflakeNextId());
            confirmOrder.setCreateTime(now);
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.insert(confirmOrder);
        } else {
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.updateByPrimaryKey(confirmOrder);
        }
    }

    public PageResp<ConfirmOrderQueryResp> queryList(ConfirmOrderQueryReq req) {
        ConfirmOrderExample confirmOrderExample = new ConfirmOrderExample();
        confirmOrderExample.setOrderByClause("id desc");
        ConfirmOrderExample.Criteria criteria = confirmOrderExample.createCriteria();

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<ConfirmOrder> confirmOrderList = confirmOrderMapper.selectByExample(confirmOrderExample);

        PageInfo<ConfirmOrder> pageInfo = new PageInfo<>(confirmOrderList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<ConfirmOrderQueryResp> list = BeanUtil.copyToList(confirmOrderList, ConfirmOrderQueryResp.class);

        PageResp<ConfirmOrderQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        confirmOrderMapper.deleteByPrimaryKey(id);
    }
    public void doConfirm(ConfirmOrderDoReq req) {
        //业务数据校验，车次是否存在、余票是否存在、车次是否有效、同乘客同车次校验
        //保存确认订单表
        DateTime now=DateTime.now();
        Date date = req.getDate();
        String trainCode= req.getTrainCode();
        String start = req.getStart();
        String end = req.getEnd();
        List<ConfirmOrderTicketReq> tickets = req.getTickets();
        ConfirmOrder confirmOrder=new ConfirmOrder();
        confirmOrder.setId(SnowUtil.getSnowflakeNextId());
        confirmOrder.setCreateTime(now);
        confirmOrder.setUpdateTime(now);
        confirmOrder.setMemberId(LoginMemberContext.getId());

        confirmOrder.setDate(date);
        confirmOrder.setTrainCode(trainCode);


        confirmOrder.setStart(start);
        confirmOrder.setEnd(end);
        confirmOrder.setDailyTrainTicketId(req.getDailyTrainTicketId());
        confirmOrder.setStatus(ConfirmOrderStatusEnum.INIT.getCode());

        confirmOrder.setTickets(JSON.toJSONString(tickets));
        confirmOrderMapper.insert(confirmOrder);

        //查询余票记录
        DailyTrainTicket dailyTrainTicket=dailyTrainTicketService.selectByUnique(date,trainCode, start, end);
        LOG.info("查出余票记录:{}",dailyTrainTicket);

        //扣减余票数量，判断余票是否足够
        reduceTickets(req,dailyTrainTicket);
        //选座
        // 最终的选座结果
        List<DailyTrainSeat> finalSeatList = new ArrayList<>();
        ConfirmOrderTicketReq ticketReqTemp=tickets.get(0);
        if(StrUtil.isNotBlank(ticketReqTemp.getSeat())){
            LOG.info("本次购票有选座");
            List<SeatColEnum> colEnumList=SeatColEnum.getColsByType(ticketReqTemp.getSeatTypeCode());
            LOG.info("本次选座的座位类型包括的列:{}",colEnumList);
            List<String> referSeatList=new ArrayList<>();
            for(int i=1;i<=2;i++){
                for(SeatColEnum seatColEnum:colEnumList){
                    referSeatList.add(seatColEnum.getCode()+i);
                }
            }
            int absoluteIndex=referSeatList.indexOf(ticketReqTemp.getSeat());
            List<Integer> offsetList=new ArrayList<>();
            for(ConfirmOrderTicketReq ticketReq:tickets){
                int index=referSeatList.indexOf(ticketReq.getSeat());
                offsetList.add(index-absoluteIndex);
            }
            LOG.info("计算座位偏移值:{}",offsetList);
            getSeat(finalSeatList,
                    date,
                    trainCode,
                    ticketReqTemp.getSeatTypeCode(),ticketReqTemp.getSeat().split("")[0],
                    offsetList,
                    dailyTrainTicket.getStartIndex(),
                    dailyTrainTicket.getEndIndex()
                    );


        }else {
            LOG.info("本次购票有选座");
            for(ConfirmOrderTicketReq ticketReq:tickets){
                getSeat(finalSeatList,
                        date,
                        trainCode,
                        ticketReq.getSeatTypeCode(),
                        null,
                        null,
                        dailyTrainTicket.getStartIndex(),
                        dailyTrainTicket.getEndIndex()
                );
            }
        }
        LOG.info("最终选座：{}", finalSeatList);
        //选座后事务处理
        try {
            afterConfirmOrderService.afterDoConfirm(dailyTrainTicket, finalSeatList, tickets, confirmOrder);
        } catch (Exception e) {
            LOG.error("保存购票信息失败", e);
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_EXCEPTION);
        }

    }
    private void getSeat(List<DailyTrainSeat> finalSeatList, Date date,String trainCode,String seatType,String column,List<Integer> offsetList,Integer startIndex,Integer endIndex){
        List<DailyTrainSeat> getSeatList=new ArrayList<>();
        List<DailyTrainCarriage> carriageList=dailyTrainCarriageService.selectBySeatType(date,trainCode,seatType);
        LOG.info("共查出{}个符合条件的车厢", carriageList.size());
        for(DailyTrainCarriage dailyTrainCarriage:carriageList){
            LOG.info("开始从车厢{}选座", dailyTrainCarriage.getIndex());
            getSeatList = new ArrayList<>();
            List<DailyTrainSeat> seatList=dailyTrainSeatService.selectByCarriage(date,trainCode,dailyTrainCarriage.getIndex());
            LOG.info("车厢{}的座位数：{}", dailyTrainCarriage.getIndex(), seatList.size());
            for(int i=0;i<seatList.size();i++){
                DailyTrainSeat dailyTrainSeat=seatList.get(i);
                Integer seatIndex=dailyTrainSeat.getCarriageSeatIndex();
                String col=dailyTrainSeat.getCol();
                //判断当前座位不能被选过
                boolean alreadyChooseFlag=false;
                for(DailyTrainSeat finalSeat:finalSeatList){
                    if(finalSeat.getId().equals(dailyTrainSeat.getId())){
                        alreadyChooseFlag=true;
                        break;
                    }
                }
                if (alreadyChooseFlag) {
                    LOG.info("座位{}被选中过，不能重复选中，继续判断下一个座位", seatIndex);
                    continue;
                }
                //判断column比对列号
                if(StrUtil.isBlank(column)){
                    LOG.info("无选座");
                }else{
                    if(!column.equals(col)){
                        LOG.info("座位{}列值不对，继续判断下一个座位，当前列值：{}，目标列值：{}", seatIndex, col, column);
                        continue;
                    }
                }
                boolean isChoose=calSell(dailyTrainSeat,startIndex,endIndex);
                if(isChoose){
                    LOG.info("选中座位");
                    getSeatList.add(dailyTrainSeat);
                }else {
                    continue;
                }
                // 根据offset选剩下的座位
                boolean isGetAllOffsetSeat = true;
                if(CollUtil.isNotEmpty(offsetList)){
                    LOG.info("有偏移值：{}，校验偏移的座位是否可选", offsetList);
                    for(int j=1;j< offsetList.size();j++){
                        Integer offset=offsetList.get(j);
                        int nextIndex=i+offset;
                        if(nextIndex>=seatList.size()){
                            LOG.info("座位{}不可选，偏移后的索引超出了这个车箱的座位数", nextIndex);
                            isGetAllOffsetSeat = false;
                            break;
                        }
                        DailyTrainSeat nextDailyTrainSeat=seatList.get(nextIndex);

                        boolean isChooseNext = calSell(nextDailyTrainSeat, startIndex, endIndex);
                        if (isChooseNext) {
                            LOG.info("座位{}被选中", nextDailyTrainSeat.getCarriageSeatIndex());
                            getSeatList.add(nextDailyTrainSeat);
                        } else {
                            LOG.info("座位{}不可选", nextDailyTrainSeat.getCarriageSeatIndex());
                            isGetAllOffsetSeat = false;
                            break;
                        }
                    }
                }
                if(!isGetAllOffsetSeat){
                    getSeatList=new ArrayList<>();
                    continue;
                }
                // 保存选好的座位
                finalSeatList.addAll(getSeatList);
                return;
            }
        }
    }
    /**
     * 计算某座位在区间内是否可卖
     * 例：sell=10001，本次购买区间站1~4，则区间已售000
     * 全部是0，表示这个区间可买；只要有1，就表示区间内已售过票
     *
     * 选中后，要计算购票后的sell，比如原来是10001，本次购买区间站1~4
     * 方案：构造本次购票造成的售卖信息01110，和原sell 10001按位与，最终得到11111
     */
    private boolean calSell(DailyTrainSeat dailyTrainSeat, Integer startIndex, Integer endIndex){
        String sell=dailyTrainSeat.getSell();
        String sellPart=sell.substring(startIndex,endIndex);
        if(Integer.parseInt(sellPart)>0){
            LOG.info("座位{}在本次车站区间{}~{}已售过票，不可选中该座位", dailyTrainSeat.getCarriageSeatIndex(), startIndex, endIndex);
            return false;
        }else{
            LOG.info("座位{}在本次车站区间{}~{}未售过票，可选中该座位", dailyTrainSeat.getCarriageSeatIndex(), startIndex, endIndex);
            //计算当前区间售票信息
            String curSell=sellPart.replace('0','1');
            curSell=StrUtil.fillBefore(curSell,'0',endIndex);
            curSell=StrUtil.fillAfter(curSell,'0',sell.length());
            // 当前区间售票信息curSell 01110与库里的已售信息sell 00001按位与，即可得到该座位卖出此票后的售票详情
            int newSellInt= NumberUtil.binaryToInt(curSell)|NumberUtil.binaryToInt(sell);
            String newSell=NumberUtil.getBinaryStr(newSellInt);
            newSell=StrUtil.fillBefore(newSell,'0',sell.length());
            LOG.info("座位{}被选中，原售票信息：{}，车站区间：{}~{}，即：{}，最终售票信息：{}"
                    , dailyTrainSeat.getCarriageSeatIndex(), sell, startIndex, endIndex, curSell, newSell);
            dailyTrainSeat.setSell(newSell);
            return true;
        }
    }

    private static void reduceTickets(ConfirmOrderDoReq req, DailyTrainTicket dailyTrainTicket) {
        for (ConfirmOrderTicketReq ticketReq : req.getTickets()) {
            String seatTypeCode = ticketReq.getSeatTypeCode();
            SeatTypeEnum seatTypeEnum = EnumUtil.getBy(SeatTypeEnum::getCode, seatTypeCode);
            switch (seatTypeEnum) {
                case YDZ -> {
                    int countLeft = dailyTrainTicket.getYdz() - 1;
                    if (countLeft < 0) {
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setYdz(countLeft);
                }
                case EDZ -> {
                    int countLeft = dailyTrainTicket.getEdz() - 1;
                    if (countLeft < 0) {
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setEdz(countLeft);
                }
                case RW -> {
                    int countLeft = dailyTrainTicket.getRw() - 1;
                    if (countLeft < 0) {
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setRw(countLeft);
                }
                case YW -> {
                    int countLeft = dailyTrainTicket.getYw() - 1;
                    if (countLeft < 0) {
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setYw(countLeft);
                }
            }
        }
    }

}