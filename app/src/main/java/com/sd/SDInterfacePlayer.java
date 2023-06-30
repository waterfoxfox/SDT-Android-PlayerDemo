/**
 * 客户端底层接口（播放相关接口）
 */
package com.sd;
import android.content.Context;
import android.util.Log;

import android.app.Activity;
import org.webrtc.EglBase;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;


public class SDInterfacePlayer {
    /**
     * 加载api所需要的动态库
     */
    static {
        System.loadLibrary("TerminalSdk");
    }

    private EglBase eglBase;
    private static final String TAG = "SDMedia";

    //构造访问jni底层库的对象，请勿修改本变量名，jni层有使用
    private long fNativeAppId = 0;

    private VideoRenderer mRenderer = null;
    private boolean mPlayAudioOnly = false;
    private boolean mPlayVideoOnly = false;
    private boolean mInited = false;
    private int mPlayIndex = 0;

    public SDInterfacePlayer() {

    }

    //decoderMode = 0(软解码)  1(硬解码)  2(硬解码优先)
	public void Init(final Activity act, final SurfaceViewRenderer surfaceView, final boolean playAudioOnly, final boolean playVideoOnly, final int decoderMode ) {
        Init(act, surfaceView, playAudioOnly, playVideoOnly, false, decoderMode);
	}
		
    public void Init(final Activity act, final SurfaceViewRenderer surfaceView, final boolean playAudioOnly, final boolean playVideoOnly, final boolean playAudioOnJava, final int decoderMode ) {

        eglBase = EglBase.create();

        mPlayAudioOnly = playAudioOnly;
        mPlayVideoOnly = playVideoOnly;
        if (playAudioOnly == false) {
            surfaceView.init(eglBase.getEglBaseContext(), null);
            mRenderer = new VideoRenderer(surfaceView);
        }
        else {
            mRenderer = null;
        }

        nativeInitCtx(act.getApplicationContext());
        fNativeAppId = nativeCreate(playAudioOnly, playVideoOnly, playAudioOnJava, decoderMode);
        mInited = true;
    }

    public void Destroy() {

        if (mInited)
        {
            nativeStopRtpPlay();
            nativeDestroy();
            mInited = false;
        }
    }


    public void startPlay(final int nIndex, final int renderWidth, final int renderHeight, final boolean renderKeepRatio) {
        mPlayIndex = nIndex;
        if (mInited) 
		{
            if (mPlayAudioOnly == false) 
			{
                if (mPlayVideoOnly == false) 
				{
                    Log.i(TAG, "start play video audio on index:" + nIndex + " with render w:" + renderWidth + " h:" + renderHeight);
                } 
				else 
				{
                    Log.i(TAG, "start play video only on index:" + nIndex + " with render w:" + renderWidth + " h:" + renderHeight);
                }
                nativeStartRtpPlay(nIndex, mRenderer.GetRenderPointer(), renderWidth, renderHeight, renderKeepRatio);
            }
            else 
			{
                Log.i(TAG, "start play audio only on index:" + nIndex);
                nativeStartRtpPlay(nIndex, 0, 0, 0, true);
            }
        }
        else 
		{
            Log.e(TAG, "StartRtpPlay failed for index:" + mPlayIndex + " should Init first.");
        }
    }

    public void stopPlay() {

        if (mInited)
        {
            Log.i(TAG, "stop play on index:" + mPlayIndex);
            nativeStopRtpPlay();
        }
        else {
            Log.e(TAG, "StopRtpPlay failed should Init first for index:" + mPlayIndex);
        }
    }


    /**
     * Jni interface
     */
    private static native void nativeInitCtx(Context ctx);
    //decoderMode = 0(软解码)  1(硬解码)  2(硬解码优先)
    private native long nativeCreate(boolean playAudioOnly, boolean playVideoOnly, boolean playAudioOnJava, final int decoderMode);
    private native void nativeStartRtpPlay(final int nIndex, final long renderPointer, final int renderWidth, final int renderHeight, final boolean renderKeepRatio);
    private native void nativeStopRtpPlay();
    private native void nativeDestroy();
}
