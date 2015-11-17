package tv.yewai.live.douyu.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.*;
import tv.yewai.live.R;
import tv.yewai.live.douyu.vo.ContentServerVo;

public class ServerUtil {

    public static Map<String, Integer> imgs = new HashMap<String, Integer>();

    static {
        imgs.put("[emot:dy001]", R.drawable.dy001);
        imgs.put("[emot:dy002]", R.drawable.dy002);
        imgs.put("[emot:dy003]", R.drawable.dy003);
        imgs.put("[emot:dy004]", R.drawable.dy004);
        imgs.put("[emot:dy005]", R.drawable.dy005);
        imgs.put("[emot:dy006]", R.drawable.dy006);
        imgs.put("[emot:dy007]", R.drawable.dy007);
        imgs.put("[emot:dy008]", R.drawable.dy008);
        imgs.put("[emot:dy009]", R.drawable.dy009);
        imgs.put("[emot:dy010]", R.drawable.dy010);
        imgs.put("[emot:dy011]", R.drawable.dy011);
        imgs.put("[emot:dy012]", R.drawable.dy012);
        imgs.put("[emot:dy013]", R.drawable.dy013);
        imgs.put("[emot:dy014]", R.drawable.dy014);
        imgs.put("[emot:dy015]", R.drawable.dy015);
        imgs.put("[emot:dy016]", R.drawable.dy016);
        imgs.put("[emot:dy017]", R.drawable.dy017);
        imgs.put("[emot:dy101]", R.drawable.dy101);
        imgs.put("[emot:dy102]", R.drawable.dy102);
        imgs.put("[emot:dy103]", R.drawable.dy103);
        imgs.put("[emot:dy104]", R.drawable.dy104);
        imgs.put("[emot:dy105]", R.drawable.dy105);
        imgs.put("[emot:dy106]", R.drawable.dy106);
        imgs.put("[emot:dy107]", R.drawable.dy107);
        imgs.put("[emot:dy108]", R.drawable.dy108);
        imgs.put("[emot:dy109]", R.drawable.dy109);
        imgs.put("[emot:dy110]", R.drawable.dy110);
        imgs.put("[emot:dy111]", R.drawable.dy111);
        imgs.put("[emot:dy112]", R.drawable.dy112);
        imgs.put("[emot:dy113]", R.drawable.dy113);
        imgs.put("[emot:dy114]", R.drawable.dy114);
        imgs.put("[emot:dy115]", R.drawable.dy115);
        imgs.put("[emot:dy116]", R.drawable.dy116);
        imgs.put("[emot:dy117]", R.drawable.dy117);
        imgs.put("[emot:dy118]", R.drawable.dy118);
        imgs.put("[emot:dy119]", R.drawable.dy119);
        imgs.put("[emot:dy120]", R.drawable.dy120);
        imgs.put("[emot:dy121]", R.drawable.dy121);
        imgs.put("[emot:dy122]", R.drawable.dy122);
        imgs.put("[emot:dy123]", R.drawable.dy123);
        imgs.put("[emot:dy124]", R.drawable.dy124);
        imgs.put("[emot:dy125]", R.drawable.dy125);
        imgs.put("[emot:dy126]", R.drawable.dy126);
        imgs.put("[emot:dy127]", R.drawable.dy127);
        //特殊图标
        imgs.put("[yw_d]", R.drawable.yuwan_big);//鱼丸
        imgs.put("[yw_x]", R.drawable.yuwan_small);
        imgs.put("[520]", R.drawable.h_520);//520鱼丸
        imgs.put("[66]", R.drawable.h_66);//666鱼翅
        imgs.put("[hj]", R.drawable.hj);//火箭
        imgs.put("[fj]", R.drawable.fj);//飞机
        imgs.put("[zan]", R.drawable.zan);//赞
        imgs.put("[dl]", R.drawable.dl);//赞
        imgs.put("[cq_1]", R.drawable.cq_1);//酬勤
        imgs.put("[cq_2]", R.drawable.cq_2);
        imgs.put("[cq_3]", R.drawable.cq_3);
        imgs.put("[cq_4]", R.drawable.cq_04);
        imgs.put("[tz]", R.drawable.tz);//通知
        imgs.put("[mobile]", R.drawable.mobile);//移动端
        imgs.put("[zb]", R.drawable.anchor);//主播
        imgs.put("[fg]", R.drawable.roomadmin);//房管
        imgs.put("[sa]", R.drawable.sa);//超管

        //酬勤等级
        imgs.put("[cqother]", R.drawable.cqother);//其他
        imgs.put("[cqlev1]", R.drawable.cq_no01);//初级
        imgs.put("[cqlev2]", R.drawable.cq_no02);//中级
        imgs.put("[cqlev3]", R.drawable.cq_no03);//高级
        imgs.put("[cqlev4]", R.drawable.cq_no04);//超级


        //用户等级
        imgs.put("[user1]", R.drawable.user1);//
        imgs.put("[user2]", R.drawable.user2);//
        imgs.put("[user3]", R.drawable.user3);//
        imgs.put("[user4]", R.drawable.user4);//
        imgs.put("[user5]", R.drawable.user5);//
        imgs.put("[user6]", R.drawable.user6);//
        imgs.put("[user7]", R.drawable.user7);//
        imgs.put("[user8]", R.drawable.user8);//
        imgs.put("[user9]", R.drawable.user9);//
        imgs.put("[user10]", R.drawable.user10);//
        imgs.put("[user11]", R.drawable.user11);//
        imgs.put("[user12]", R.drawable.user12);//
        imgs.put("[user13]", R.drawable.user13);//
        imgs.put("[user14]", R.drawable.user14);//
        imgs.put("[user15]", R.drawable.user15);//
        imgs.put("[user16]", R.drawable.user16);//
        imgs.put("[user17]", R.drawable.user17);//
        imgs.put("[user18]", R.drawable.user18);//
        imgs.put("[user19]", R.drawable.user19);//
        imgs.put("[user20]", R.drawable.user20);//
        imgs.put("[user21]", R.drawable.user21);//
        imgs.put("[user22]", R.drawable.user22);//
        imgs.put("[user23]", R.drawable.user23);//
        imgs.put("[user24]", R.drawable.user24);//
        imgs.put("[user25]", R.drawable.user25);//
        imgs.put("[user26]", R.drawable.user26);//
        imgs.put("[user27]", R.drawable.user27);//
        imgs.put("[user28]", R.drawable.user28);//
        imgs.put("[user29]", R.drawable.user29);//
        imgs.put("[user30]", R.drawable.user30);//
        imgs.put("[user31]", R.drawable.user31);//


    }

    public static List<ContentServerVo> QueryContentServerList(String serverList) {
        String serverListJson = "[{\"" + serverList.replaceAll("@S/", "\"},{\"").replaceAll("@A=", "\":\"").replaceAll("@S", "\",\"") + "\"}]";
        serverListJson = serverListJson.substring(0, serverListJson.indexOf(",{\"\"}")) + "]";
        List contentServerList = new ArrayList();
        JSONArray jsonArray = JSON.parseArray(serverListJson);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            ContentServerVo vo = (ContentServerVo) JSON.toJavaObject(jsonObject, ContentServerVo.class);
            contentServerList.add(vo);
        }
        return contentServerList;
    }

    public static String toDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH-mm");
        return sdf.format(new Date());
    }

    //id = R.drawable.dy001
    private static Bitmap getImg(Activity ac, String imgStr) {
        Bitmap b = BitmapFactory.decodeResource(ac.getResources(), imgs.get(imgStr));
        return b;
    }


    //构建正则表达式
    private static Pattern buildPattern() {
        StringBuilder patternString = new StringBuilder(imgs.keySet().size() * 3);
        patternString.append('(');
        for (String s : imgs.keySet()) {
            patternString.append(Pattern.quote(s));
            patternString.append('|');
        }
        patternString.replace(patternString.length() - 1, patternString.length(), ")");

        return Pattern.compile(patternString.toString());
    }

    //根据文本替换成图片
    public static CharSequence replace(CharSequence text, Activity ac) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        Pattern mPattern = buildPattern();
        Matcher matcher = mPattern.matcher(text);
        while (matcher.find()) {
            int resId = imgs.get(matcher.group());
            builder.setSpan(new ImageSpan(ac, resId), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }
}
