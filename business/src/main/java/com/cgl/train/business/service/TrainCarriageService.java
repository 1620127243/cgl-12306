package com.cgl.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.cgl.train.business.domain.TrainCarriage;
import com.cgl.train.business.domain.TrainCarriageExample;
import com.cgl.train.business.enums.SeatColEnum;
import com.cgl.train.business.mapper.TrainCarriageMapper;
import com.cgl.train.business.req.TrainCarriageQueryReq;
import com.cgl.train.business.req.TrainCarriageSaveReq;
import com.cgl.train.business.resp.TrainCarriageQueryResp;
import com.cgl.train.common.exception.BusinessException;
import com.cgl.train.common.exception.BusinessExceptionEnum;
import com.cgl.train.common.resp.PageResp;
import com.cgl.train.common.util.SnowUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainCarriageService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainCarriageService.class);

    @Resource
    private TrainCarriageMapper trainCarriageMapper;
    @Lazy
    @Resource
    private DailyTrainCarriageService dailyTrainCarriageService;
    @Lazy
    @Resource
    private DailyTrainSeatService dailyTrainSeatService;
    @Lazy
    @Resource
    private TrainSeatService trainSeatService;


    public void save(TrainCarriageSaveReq req) {
        DateTime now = DateTime.now();
        //计算列数和总座位数
        List<SeatColEnum> seatColEnums=SeatColEnum.getColsByType(req.getSeatType());
        req.setColCount(seatColEnums.size());
        req.setSeatCount(req.getRowCount()*req.getColCount());

        TrainCarriage trainCarriage = BeanUtil.copyProperties(req, TrainCarriage.class);
        if (ObjectUtil.isNull(trainCarriage.getId())) {
            TrainCarriage trainCarriageDB=selectByUnique(req.getTrainCode(), req.getIndex());
            if(ObjectUtil.isNotEmpty(trainCarriageDB)){
                throw new BusinessException(BusinessExceptionEnum.BUSINESS_TRAIN_CARRIAGE_INDEX_UNIQUE_ERROR);
            }
            trainCarriage.setId(SnowUtil.getSnowflakeNextId());
            trainCarriage.setCreateTime(now);
            trainCarriage.setUpdateTime(now);
            trainCarriageMapper.insert(trainCarriage);
            trainSeatService.genTrainSeat(req.getTrainCode());
        } else {
            trainCarriage.setUpdateTime(now);
            trainCarriageMapper.updateByPrimaryKey(trainCarriage);
        }
    }
    private TrainCarriage selectByUnique(String trainCode,Integer index){
        TrainCarriageExample trainCarriageExample=new TrainCarriageExample();
        trainCarriageExample.createCriteria()
                .andTrainCodeEqualTo(trainCode)
                .andIndexEqualTo(index);
        List<TrainCarriage> list = trainCarriageMapper.selectByExample(trainCarriageExample);
        if (CollUtil.isNotEmpty(list)) {
            return list.get(0);
        } else {
            return null;
        }
    }

    public PageResp<TrainCarriageQueryResp> queryList(TrainCarriageQueryReq req) {
        TrainCarriageExample trainCarriageExample = new TrainCarriageExample();
        trainCarriageExample.setOrderByClause("train_code asc, `index` asc");
        TrainCarriageExample.Criteria criteria = trainCarriageExample.createCriteria();
        if(ObjectUtil.isNotEmpty(req.getTrainCode())){
            criteria.andTrainCodeEqualTo(req.getTrainCode());
        }

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<TrainCarriage> trainCarriageList = trainCarriageMapper.selectByExample(trainCarriageExample);

        PageInfo<TrainCarriage> pageInfo = new PageInfo<>(trainCarriageList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<TrainCarriageQueryResp> list = BeanUtil.copyToList(trainCarriageList, TrainCarriageQueryResp.class);

        PageResp<TrainCarriageQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }


    public void delete(Long id) {
        TrainCarriage trainCarriage=trainCarriageMapper.selectByPrimaryKey(id);
        dailyTrainCarriageService.deleteByTrainCodeAndCarriageIndex(trainCarriage.getTrainCode(), trainCarriage.getIndex());
        dailyTrainSeatService.deleteByTrainCodeAndCarriageIndex(trainCarriage.getTrainCode(), trainCarriage.getIndex());

        trainSeatService.deleteByTrainCodeAndCarriageIndex(trainCarriage.getTrainCode(), trainCarriage.getIndex());
        trainCarriageMapper.deleteByPrimaryKey(id);
    }
    public List<TrainCarriage> selectByTrainCode(String trainCode){
        TrainCarriageExample trainCarriageExample = new TrainCarriageExample();
        trainCarriageExample.setOrderByClause("`index` asc");
        TrainCarriageExample.Criteria criteria = trainCarriageExample.createCriteria();
        criteria.andTrainCodeEqualTo(trainCode);
        return trainCarriageMapper.selectByExample(trainCarriageExample);
    }
    public void deleteByTrainCode(String trainCode){

        TrainCarriageExample trainCarriageExam=new TrainCarriageExample();
        trainCarriageExam.createCriteria().andTrainCodeEqualTo(trainCode);
        trainCarriageMapper.deleteByExample(trainCarriageExam);

    }
}
