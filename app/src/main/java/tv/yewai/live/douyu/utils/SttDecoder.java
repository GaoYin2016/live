package tv.yewai.live.douyu.utils;

import java.util.HashMap;

import tv.yewai.live.douyu.vo.SttEncodingItem;

public class SttDecoder {
    HashMap<String, String> itemsMap = new HashMap();

    public int Parse(String str) {
        SttEncodingItem item = new SttEncodingItem();
        int sp = 0;
        char[] chars = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        while (sp < chars.length) {
            if (chars[sp] == '/') {
                if (item.Key == null) {
                    item.Key = "";
                }
                item.Value = sb.toString();
                sb.delete(0, sb.length());
                this.itemsMap.put(item.Key, item.Value);
                item = new SttEncodingItem();
            } else if (chars[sp] == '@') {
                sp++;
                if (sp < chars.length) {
                    if (chars[sp] == 'A') {
                        sb.append('@');
                    } else if (chars[sp] == 'S') {
                        sb.append('/');
                    } else if (chars[sp] == '=') {
                        item.Key = sb.toString();
                        sb.delete(0, sb.length());
                    }

                }

            } else {
                sb.append(chars[sp]);
            }
            sp++;
        }

        if ((sp > 0) && (sp == chars.length) && (chars[(sp - 1)] != '/')) {
            if (item.Key == null) {
                item.Key = "";
            }
            item.Value = sb.toString();
            sb.delete(0, sb.length());
            this.itemsMap.put(item.Key, item.Value);
            item = new SttEncodingItem();
        }
        return this.itemsMap.size();
    }

    public void Clear() {
        this.itemsMap.clear();
    }

    public String GetItem(String key) {
        return (String) this.itemsMap.get(key);
    }
}
