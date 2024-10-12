package com.example.bean.ip;

import com.example.interfce.IpDataObtain;

/**
 * 腾讯查询 IP 数据，目前只需要 ip、国家信息
 *
 * @author laiweisheng
 * @date 2024/10/11
 */
public class TencentIpDataBean extends IpDataObtain {

    public String ip;

    public String country;

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public String getCountry() {
        return country;
    }
}
