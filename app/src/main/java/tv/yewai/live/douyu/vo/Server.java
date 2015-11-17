package tv.yewai.live.douyu.vo;

/**
 * 弹幕登陆服务器
 * Created by Star on 2015/10/23.
 */
public class Server {

    private String ip;
    private String port;

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "Server{" +
                "ip='" + ip + '\'' +
                ", port='" + port + '\'' +
                '}';
    }
}
