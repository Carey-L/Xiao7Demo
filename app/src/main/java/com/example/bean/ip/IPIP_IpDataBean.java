package com.example.bean.ip;

import com.example.interfce.IpDataObtain;

import java.util.List;

/**
 * IPIP 查询 IP 数据，目前只需要 ip、国家信息
 *
 * @author laiweisheng
 * @date 2024/10/11
 */
public class IPIP_IpDataBean extends IpDataObtain {

    public IPIP_IpDetailDataBean data;

    @Override
    public String getIp() {
        return data != null ? data.ip : "";
    }

    @Override
    public String getCountry() {
        return data != null ? (data.location != null && !data.location.isEmpty()) ? data.location.get(0) : "" : "";
    }

    public static class IPIP_IpDetailDataBean {

        public String ip;

        /**
         * 示例 "location": ["中国", "广东", "珠海", "", "电信"]
         */
        public List<String> location;
    }
}
