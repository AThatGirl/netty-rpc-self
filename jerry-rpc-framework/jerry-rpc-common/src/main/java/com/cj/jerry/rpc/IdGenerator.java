package com.cj.jerry.rpc;

import com.cj.jerry.rpc.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.LongAdder;

@Slf4j
public class IdGenerator {
//集群不行
//    private static LongAdder longAdder = new LongAdder();
//
//    public static long getId() {
//        longAdder.increment();
//        return longAdder.sum();
//    }

    //雪花算法
    //机房号码 5bit
    //机器号 5bit
    //时间戳 42bit
    //随机数（比如公司成立日期）
    //同一个机房的同一个机器号的同一个时间可以因为并发量很大需要多个id
    //序列号 12bit
    public static final long START_STAMP = DateUtils.get("2022-01-01").getTime();
    public static final long DATA_CENTER_BIT = 5L;
    public static final long MACHINE_BIT = 5L;
    public static final long SEQUENCE_BIT = 12L;

    //最大值
    public static final long DATA_CENTER_MAX = ~(-1L << DATA_CENTER_BIT);
    public static final long MACHINE_MAX = ~(-1L << MACHINE_BIT);
    public static final long SEQUENCE_MAX = ~(-1L << SEQUENCE_BIT);

    //时间戳42+机房号5+机器号5+序列号12
    public static final long TIMESTAMP_LEFT = SEQUENCE_BIT + MACHINE_BIT + DATA_CENTER_BIT;
    public static final long DATA_CENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    public static final long MACHINE_LEFT = SEQUENCE_BIT;

    private long dataCenterId;
    private long machineId;
    private LongAdder sequenceId = new LongAdder();

    //时钟回拨的问题
    private long lastStamp = -1L;

    public IdGenerator(long dataCenterId, long machineId) {
        //判断传入的参数是否合法
        if (dataCenterId > DATA_CENTER_MAX || machineId > MACHINE_MAX) {
            throw new IllegalArgumentException("传入的数据中心编号或机器号不合法");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    public long getId() {
        //处理时间戳的问题
        long currentTime = System.currentTimeMillis();

        long timeStamp = currentTime - START_STAMP;
        System.out.println(timeStamp);
        System.out.println(DateUtils.get(START_STAMP));
        if (timeStamp < lastStamp) {
            log.error("currentTime:{},lastStamp:{}",DateUtils.get(timeStamp),DateUtils.get(lastStamp));
            throw new RuntimeException("您的时钟服务器进行了时钟回调");
        }
        if (timeStamp == lastStamp) {
            sequenceId.increment();
            if (sequenceId.sum() >= SEQUENCE_MAX) {
                timeStamp = getNextMillis();
            }
        } else {
            sequenceId.reset();
        }
        lastStamp = timeStamp;
        long sequence = sequenceId.sum();
        return timeStamp << TIMESTAMP_LEFT | dataCenterId << DATA_CENTER_BIT | machineId << MACHINE_LEFT | sequence;
    }

    private long getNextMillis() {

        long currentTimeMillis = System.currentTimeMillis() - START_STAMP;
        while (currentTimeMillis == lastStamp) {
            currentTimeMillis = System.currentTimeMillis() - START_STAMP;
        }
        return currentTimeMillis;
    }
}

