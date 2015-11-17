package tv.yewai.live.douyu.utils;

public class BigHexStringUtils {
    private String hexStr = "";

    public void clear() {
        this.hexStr = "";
    }

    public void addHexStr(String pStr) {
        this.hexStr += pStr;
    }

    public String getHexStr() {
        return this.hexStr;
    }

    public boolean isFullHexStr() {
        int msgLength = HexUtils.getHexStringLength(this.hexStr);
        if (msgLength + 16 == this.hexStr.replace(" ", "").length()) {
            return true;
        }
        return false;
    }
}
