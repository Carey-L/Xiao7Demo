package com.example.bean.ip;

import com.example.interfce.IpDataObtain;

/**
 * 百度查询 IP 数据，目前只需要 ip、国家信息
 *
 * @author laiweisheng
 * @date 2024/10/11
 */
public class BaiDuIpDataBean extends IpDataObtain {

    public String ip;

    public BaiDuIpDetailDataBean data;

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public String getCountry() {
        return data != null ? data.country : "";
    }

    public static class BaiDuIpDetailDataBean {

        public String country;
    }
}
