package hsyeo.watchmi;

import android.view.MotionEvent;
import android.view.ViewConfiguration;

/** source from
 * http://stackoverflow.com/questions/12414680/how-to-implement-a-two-finger-double-click-in-android
 * */

public abstract class TwoFingersDoubleTapDetector {
    private static final int TIMEOUT = ViewConfiguration.getDoubleTapTimeout() + 100;
    private long mFirstDownTime = 0;
    private boolean mSeparateTouches = false;
    private byte mTwoFingerTapCount = 0;

    private void reset(long time) {
        mFirstDownTime = time;
        mSeparateTouches = false;
        mTwoFingerTapCount = 0;
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if(mFirstDownTime == 0 || event.getEventTime() - mFirstDownTime > TIMEOUT)
                    reset(event.getDownTime());
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if(event.getPointerCount() == 2)
                    mTwoFingerTapCount++;
                else
                    mFirstDownTime = 0;
                break;
            case MotionEvent.ACTION_UP:
                if(!mSeparateTouches)
                    mSeparateTouches = true;
                else if(mTwoFingerTapCount == 2 && event.getEventTime() - mFirstDownTime < TIMEOUT) {
                    onTwoFingersDoubleTap();
                    mFirstDownTime = 0;
                    return true;
                }
        }

        return false;
    }

    public abstract void onTwoFingersDoubleTap();
}
