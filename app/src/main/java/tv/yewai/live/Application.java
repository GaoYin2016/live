package tv.yewai.live;

import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import java.util.Set;

/**
 * Created by Star on 2015/10/22.
 */
public class Application extends android.app.Application {

    private Config config = new Config() ;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection ;

    public MediaProjectionManager getmMediaProjectionManager() {
        return mMediaProjectionManager;
    }

    public void setmMediaProjectionManager(MediaProjectionManager mMediaProjectionManager) {
        this.mMediaProjectionManager = mMediaProjectionManager;
    }

    public MediaProjection getmMediaProjection() {
        return mMediaProjection;
    }

    public void setmMediaProjection(MediaProjection mMediaProjection) {
        this.mMediaProjection = mMediaProjection;
    }

    public Config getConfig() {
        return config;
    }

}
