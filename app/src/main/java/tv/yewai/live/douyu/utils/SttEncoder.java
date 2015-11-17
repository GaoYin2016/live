package tv.yewai.live.douyu.utils;

public class SttEncoder {
    private StringBuilder sb = new StringBuilder();

    public String GetResualt() {
        return this.sb.toString();
    }

    public void Clear() {
        this.sb.delete(0, this.sb.length());
    }

    public void AddItem(String value) {
        int sp = 0;
        char[] chars = value.toCharArray();

        while (sp < chars.length) {
            if (chars[sp] == '/') {
                this.sb.append("@S");
            } else if (chars[sp] == '@') {
                this.sb.append("@A");
            } else {
                this.sb.append(chars[sp]);
            }
            sp++;
        }
        this.sb.append('/');
    }

    public void AddItem(String key, String value) {
        int sp = 0;
        char[] chars = key.toCharArray();
        while (sp < chars.length) {
            if (chars[sp] == '/') {
                this.sb.append("@S");
            } else if (chars[sp] == '@') {
                this.sb.append("@A");
            } else {
                this.sb.append(chars[sp]);
            }
            sp++;
        }
        this.sb.append("@=");

        sp = 0;
        chars = value.toCharArray();
        while (sp < chars.length) {
            if (chars[sp] == '/') {
                this.sb.append("@S");
            } else if (chars[sp] == '@') {
                this.sb.append("@A");
            } else {
                this.sb.append(chars[sp]);
            }
            sp++;
        }
        this.sb.append('/');
    }

    public void AddItem(int value) {
        AddItem(value);
    }

    public void AddItem(String key, int value) {
        AddItem(key, value);
    }

    public void AddItem(double value) {
        AddItem(value);
    }

    public void AddItem(String key, double value) {
        AddItem(key, value);
    }
}
