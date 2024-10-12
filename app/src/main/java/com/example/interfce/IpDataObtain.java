package com.example.interfce;

import com.example.bean.ip.CommonIpDataBean;

/**
 * 由对应 bean 实现，通用获取 IP、国家信息
 *
 * @author laiweisheng
 * @date 2024/10/11
 */
public abstract class IpDataObtain {

    public abstract String getIp();

    public abstract String getCountry();

    public String getCommonIpDataJson() {
        return new CommonIpDataBean(getIp(), getCountry()).toString();
    }
}
