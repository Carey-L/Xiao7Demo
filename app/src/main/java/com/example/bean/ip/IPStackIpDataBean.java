package com.example.bean.ip;

import com.example.interfce.IpDataObtain;

/**
 * IPStack 查询 IP 数据，目前只需要 ip、国家信息
 *
 * @author laiweisheng
 * @date 2024/10/11
 */
public class IPStackIpDataBean extends IpDataObtain {

    public String ip;

    public String country_name;

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public String getCountry() {
        return country_name;
    }
}
