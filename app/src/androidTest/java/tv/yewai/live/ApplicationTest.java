package tv.yewai.live;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import tv.yewai.live.utils.TtsUtil;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testTTS(){
        TtsUtil.build(this.getContext()).tms("你好");
    }

    //http://www.douyutv.com//api/v1/login?aid=dytoolm1&client_sys=ANDROID&time=1445869453&auth=72a7cd4a8b8e24d66be02977a6e4e9ab&username=ystar9&password=81621594
    public void testAudio(){
        Log.v("TAG", "xxx");
      // Log.v("TAG", ImageUtilEngine.call(this.getContext(), "a", "b"));
    }

}