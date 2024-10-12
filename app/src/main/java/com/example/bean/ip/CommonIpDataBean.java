package com.example.bean.ip;

/**
 * 通用的 IP 数据
 *
 * @author laiweisheng
 * @date 2024/10/11
 */
public class CommonIpDataBean {

    public String ip;

    public String country;

    public CommonIpDataBean() {

    }

    public CommonIpDataBean(String ip) {
        this.ip = ip;
    }

    public CommonIpDataBean(String ip, String country) {
        this.ip = ip;
        this.country = country;
    }

    @Override
    public String toString() {
        return "{" +
                "\"ip\":\"" + ip + "\"," +
                "\"country\":\"" + country + "\"" +
                "}";
    }
}
