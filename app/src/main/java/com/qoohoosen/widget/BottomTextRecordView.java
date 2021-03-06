package com.qoohoosen.widget;

import static com.qoohoosen.utils.Constable.TIMER_1000;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qoohoosen.app.R;
import com.qoohoosen.utils.UtilsAnimation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import io.supercharge.shimmerlayout.ShimmerLayout;

public class BottomTextRecordView {
    private String TAG = BottomTextRecordView.this.getClass().getSimpleName();
    private Context context;

    //Widgets
    private LinearLayout viewContainer;
    private ImageView imageViewAudio;
    private View imageViewLockArrow;
    private View imageViewLock;
    private View imageViewMic;
    private View dustin;
    private View dustin_cover;
    private ImageView imageViewStop;
    private ImageView imageViewSend;
    private View layoutDustin;
    private View layoutMessage;
    private View imageViewEmoji;
    private View layoutSlideCancel;
    private View layoutLock;
    private View layoutEffect1;
    private View layoutEffect2;
    private View linearLayoutMic;
    private ImageView imageAudioAnimate;

    private EditText editTextMessage;
    private TextView timeText;
    private TextView textViewSlide;
    private ShimmerLayout shimmerLayoutSlide;

    //Anims
//    private Animation animBlink;
    private Animation animJump;
    private Animation animJumpFast;

    private boolean isDeleting;
    private boolean stopTrackingAction;
    private Handler handler;

    private int audioTotalTime;
    private TimerTask timerTask;
    private Timer audioTimer;
    private SimpleDateFormat timeFormatter;

    private float lastX, lastY;
    private float firstX, firstY;

    private float directionOffset, cancelOffset, lockOffset;
    private float dp = 0;
    private boolean isLocked = false;

    private UserBehaviour userBehaviour = UserBehaviour.NONE;
    private RecordingListener recordingListener;

    private boolean isLayoutDirectionRightToLeft;

    private int screenWidth, screenHeight;


    private RecordPermissionHandler recordPermissionHandler;
    private boolean canRecord = true;

    public BottomTextRecordView() {
        super();
    }

    public void initView(ViewGroup view) {

        if (view == null)
            return;

        context = view.getContext();

        view.removeAllViews();
        view.addView(LayoutInflater.from(view.getContext())
                .inflate(R.layout.inflater_bottom_text_record_view, null));

        timeFormatter = new SimpleDateFormat("mm:ss", Locale.getDefault());

        DisplayMetrics displayMetrics = view.getContext().getResources().getDisplayMetrics();
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;

        isLayoutDirectionRightToLeft = view.getContext().getResources()
                .getBoolean(R.bool.is_right_to_left);

        viewContainer = view.findViewById(R.id.layoutContainer);

        imageViewEmoji = view.findViewById(R.id.imageViewEmoji);
        editTextMessage = view.findViewById(R.id.editTextMessage);

//        send = view.findViewById(R.id.imageSend);
//        stop = view.findViewById(R.id.imageStop);
//        audio = view.findViewById(R.id.imageAudio);

        imageViewAudio = view.findViewById(R.id.imageViewAudio);
        imageViewStop = view.findViewById(R.id.imageViewStop);
        imageViewSend = view.findViewById(R.id.imageViewSend);
        imageViewLock = view.findViewById(R.id.imageViewLock);
        imageViewLockArrow = view.findViewById(R.id.imageViewLockArrow);
        layoutDustin = view.findViewById(R.id.layoutDustin);
        layoutMessage = view.findViewById(R.id.layoutMessage);
        textViewSlide = view.findViewById(R.id.textViewSlide);
        shimmerLayoutSlide = view.findViewById(R.id.shimmer_layout_slide);
        timeText = view.findViewById(R.id.textViewTime);
        layoutSlideCancel = view.findViewById(R.id.layoutSlideCancel);
        layoutEffect2 = view.findViewById(R.id.layoutEffect2);
        linearLayoutMic = view.findViewById(R.id.linearLayoutMic);
        imageAudioAnimate = view.findViewById(R.id.imageAudioAnimate);
        layoutEffect1 = view.findViewById(R.id.layoutEffect1);
        layoutLock = view.findViewById(R.id.layoutLock);
        imageViewMic = view.findViewById(R.id.imageViewMic);
        dustin = view.findViewById(R.id.dustin);
        dustin_cover = view.findViewById(R.id.dustin_cover);

        handler = new Handler(Looper.getMainLooper());

        dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1,
                view.getContext().getResources().getDisplayMetrics());

//        animBlink = UtilsAnimation.loadAnimation(view.getContext(),
//                R.anim.blink);
        animJump = AnimationUtils.loadAnimation(view.getContext(),
                R.anim.jump);
        animJumpFast = AnimationUtils.loadAnimation(view.getContext(),
                R.anim.jump_fast);

        setupRecording();
    }


    public View setContainerView(int layoutResourceID) {
        View view = LayoutInflater.from(viewContainer.getContext()).inflate(layoutResourceID,
                null);

        if (view == null)
            return null;


        viewContainer.removeAllViews();
        viewContainer.addView(view);
        return view;
    }


    public void changeSlideToCancelText(int textResourceId) {
        textViewSlide.setText(textResourceId);
    }

    public RecordingListener getRecordingListener() {
        return recordingListener;
    }

    public void setRecordingListener(RecordingListener recordingListener) {
        this.recordingListener = recordingListener;
    }

    public void setRecordPermissionHandler(RecordPermissionHandler recordPermissionHandler) {
        this.recordPermissionHandler = recordPermissionHandler;
    }

    public View getSendView() {
        return imageViewSend;
    }

    public EditText getMessageView() {
        return editTextMessage;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupRecording() {

        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty()) {
                    if (imageViewSend.getVisibility() != View.GONE) {
                        imageViewSend.setVisibility(View.GONE);
                    }

                } else {
                    if (imageViewSend.getVisibility() != View.VISIBLE && !isLocked) {
                        imageViewSend.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        imageViewAudio.setOnTouchListener((view, motionEvent) -> {

            if (isDeleting)
                return true;

//            if (!isRecordPermissionGranted())
//                return true;

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {


                cancelOffset = (float) (screenWidth / 2.8);
                lockOffset = (float) (screenWidth / 2.5);

                if (firstX == 0) {
                    firstX = motionEvent.getRawX();
                }

                if (firstY == 0) {
                    firstY = motionEvent.getRawY();
                }

                startRecord();

            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP
                    || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    stopRecording(RecordingBehaviour.RELEASED, 1100L);
                }

            } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {

                if (stopTrackingAction) {
                    return true;
                }

                UserBehaviour direction = UserBehaviour.NONE;

                float motionX = Math.abs(firstX - motionEvent.getRawX());
                float motionY = Math.abs(firstY - motionEvent.getRawY());

                if (isLayoutDirectionRightToLeft
                        ? (motionX > directionOffset && lastX > firstX && lastY > firstY)
                        : (motionX > directionOffset && lastX < firstX && lastY < firstY)) {

                    if (isLayoutDirectionRightToLeft
                            ? (motionX > motionY && lastX > firstX)
                            : (motionX > motionY && lastX < firstX)) {
                        direction = UserBehaviour.CANCELING;

                    } else if (motionY > motionX && lastY < firstY) {
                        direction = UserBehaviour.LOCKING;
                    }

                } else if (isLayoutDirectionRightToLeft
                        ? (motionX > motionY && motionX > directionOffset && lastX > firstX)
                        : (motionX > motionY && motionX > directionOffset && lastX < firstX)) {
                    direction = UserBehaviour.CANCELING;
                } else if (motionY > motionX && motionY > directionOffset && lastY < firstY) {
                    direction = UserBehaviour.LOCKING;
                }

                if (direction == UserBehaviour.CANCELING) {
                    if (userBehaviour == UserBehaviour.NONE || motionEvent.getRawY()
                            + imageViewAudio.getWidth() / 2 > firstY) {
                        userBehaviour = UserBehaviour.CANCELING;
                    }

                    if (userBehaviour == UserBehaviour.CANCELING) {
                        translateX(-(firstX - motionEvent.getRawX()));
                    }
                } else if (direction == UserBehaviour.LOCKING) {
                    if (userBehaviour == UserBehaviour.NONE || motionEvent.getRawX()
                            + imageViewAudio.getWidth() / 2 > firstX) {
                        userBehaviour = UserBehaviour.LOCKING;
                    }

                    if (userBehaviour == UserBehaviour.LOCKING) {
                        translateY(-(firstY - motionEvent.getRawY()));
                    }
                }

                lastX = motionEvent.getRawX();
                lastY = motionEvent.getRawY();
            }
            view.onTouchEvent(motionEvent);
            return true;
        });

        imageViewStop.setOnClickListener(view -> {
            isLocked = false;
            stopRecording(RecordingBehaviour.LOCK_DONE, 900L);
        });

//        imageViewStop.setOnClickListener(new DebounceClickListener(v -> {
//            isLocked = false;
//            stopRecording(RecordingBehaviour.LOCK_DONE);
//        }, DEBOUNCE_INTERVAL));
    }

    private void translateY(float y) {
        if (y < -lockOffset) {
            locked();
            imageViewAudio.setTranslationY(0);
            return;
        }

        if (layoutLock.getVisibility() != View.VISIBLE)
            UtilsAnimation.slideUp(layoutLock, 600L);


        imageViewAudio.setTranslationY(y);
        layoutLock.setTranslationY(y / 2);
        imageViewAudio.setTranslationX(0);
    }

    private void translateX(float x) {

        if (isLayoutDirectionRightToLeft ? x > cancelOffset : x < -cancelOffset) {
            canceled();
            imageViewAudio.setTranslationX(0);
            layoutSlideCancel.setTranslationX(0);
            return;
        }

        imageViewAudio.setTranslationX(x);
        layoutSlideCancel.setTranslationX(x);
        layoutLock.setTranslationY(0);
        imageViewAudio.setTranslationY(0);

        if (Math.abs(x) < imageViewMic.getWidth() / 2) {
            if (layoutLock.getVisibility() != View.VISIBLE)
                UtilsAnimation.slideUp(layoutLock, 600L);

        } else {
            if (layoutLock.getVisibility() != View.GONE)
                layoutLock.setVisibility(View.GONE);

        }
    }

    private void locked() {
        stopTrackingAction = true;
        stopRecording(RecordingBehaviour.LOCKED, 50L);
        isLocked = true;
    }

    private void canceled() {
        stopTrackingAction = true;
        stopRecording(RecordingBehaviour.CANCELED, 100L);
    }


    private synchronized void stopRecording(RecordingBehaviour recordingBehaviour, long duration) {

        if (editTextMessage.getVisibility() == View.VISIBLE)
            return;

        stopTrackingAction = true;
        firstX = 0;
        firstY = 0;
        lastX = 0;
        lastY = 0;

        userBehaviour = UserBehaviour.NONE;

        imageViewAudio.animate().scaleX(1f).scaleY(1f).translationX(0).translationY(0)
                .setDuration(100).setInterpolator(new LinearInterpolator()).start();

        layoutSlideCancel.setTranslationX(0);

        shimmerLayoutSlide.stopShimmerAnimation();
        layoutSlideCancel.setVisibility(View.GONE);

        layoutLock.setVisibility(View.GONE);
        layoutLock.setTranslationY(0);
        imageViewLockArrow.clearAnimation();
        imageViewLock.clearAnimation();

        if (isLocked) {
            return;
        }

        handler.postDelayed(() -> {


            if (recordingBehaviour == RecordingBehaviour.LOCKED) {
                imageViewStop.setVisibility(View.VISIBLE);

                if (recordingListener != null)
                    recordingListener.onRecordingLocked();

            } else if (recordingBehaviour == RecordingBehaviour.CANCELED) {

                timeText.setVisibility(View.INVISIBLE);
                imageViewMic.setVisibility(View.INVISIBLE);
                imageViewStop.setVisibility(View.GONE);
                layoutEffect2.setVisibility(View.GONE);
                layoutEffect1.setVisibility(View.GONE);

                audioTimer.purge();
                timerTask.cancel();
                delete();

                if (recordingListener != null)
                    recordingListener.onRecordingCanceled();


            } else if (recordingBehaviour == RecordingBehaviour.RELEASED
                    || recordingBehaviour == RecordingBehaviour.LOCK_DONE) {


                timeText.setVisibility(View.INVISIBLE);
                imageViewMic.setVisibility(View.INVISIBLE);
                editTextMessage.setVisibility(View.VISIBLE);
                layoutMessage.setVisibility(View.VISIBLE);

                UtilsAnimation.slideDown(linearLayoutMic, 400L);
                UtilsAnimation.slideDown(imageAudioAnimate, 300L);


                imageViewEmoji.setVisibility(View.VISIBLE);

                imageViewStop.setVisibility(View.GONE);
                editTextMessage.requestFocus();
                layoutEffect2.setVisibility(View.GONE);
                layoutEffect1.setVisibility(View.GONE);

                layoutSlideCancel.setVisibility(View.GONE);
                layoutLock.setVisibility(View.GONE);


                audioTimer.purge();
                timerTask.cancel();
                audioTotalTime = 0;


//                if (editTextMessage.getVisibility() == View.VISIBLE)
//                    return;
                if (recordingListener != null)
                    recordingListener.onRecordingCompleted();


            }
        }, duration);
    }

    private synchronized void startRecord() {

        if (editTextMessage.getVisibility() != View.VISIBLE)
            return;

        stopTrackingAction = false;

        layoutMessage.setVisibility(View.GONE);

        editTextMessage.setVisibility(View.INVISIBLE);
        imageViewEmoji.setVisibility(View.INVISIBLE);
        imageViewAudio.animate().scaleXBy(1f).scaleYBy(1f)
                .setDuration(200).setInterpolator(new OvershootInterpolator()).start();

        UtilsAnimation.slideUp(linearLayoutMic, 300L);
        UtilsAnimation.slideUp(imageAudioAnimate, 200L);
        UtilsAnimation.slideUp(timeText, 500L);
        UtilsAnimation.slideUp(layoutLock, 400L);
        UtilsAnimation.slideUp(layoutSlideCancel, 500L);

        UtilsAnimation.slideUp(imageViewMic, 500L);
        UtilsAnimation.slideUp(layoutEffect2, 500L);
        UtilsAnimation.slideUp(layoutEffect1, 500L);


        shimmerLayoutSlide.startShimmerAnimation();


//        timeText.startAnimation(animBlink);
        imageViewLockArrow.clearAnimation();
        imageViewLock.clearAnimation();
        imageViewLockArrow.startAnimation(animJumpFast);
        imageViewLock.startAnimation(animJump);

        if (audioTimer == null) {
            audioTimer = new Timer();
            timeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> {
                    timeText.setText(timeFormatter
                            .format(new Date((long) audioTotalTime * TIMER_1000)));
                    audioTotalTime = audioTotalTime + 1;
                });
            }
        };

        audioTotalTime = 0;
        audioTimer.schedule(timerTask, 0, TIMER_1000);

        if (recordingListener != null)
            recordingListener.onRecordingStarted();
    }

    private void delete() {
        imageViewMic.setVisibility(View.VISIBLE);
        imageViewMic.setRotation(0);
        isDeleting = true;
        imageViewAudio.setEnabled(false);

        handler.postDelayed(() -> {
            isDeleting = false;
            imageViewAudio.setEnabled(true);

        }, 1250);

        imageViewMic
                .animate()
                .translationY(-dp * 150)
                .rotation(180)
                .scaleXBy(0.6f)
                .scaleYBy(0.6f)
                .setDuration(1000)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animation) {

                        float displacement = 0;

                        if (isLayoutDirectionRightToLeft) {
                            displacement = dp * 40;
                        } else {
                            displacement = -dp * 40;
                        }

                        dustin.setTranslationX(displacement);
                        dustin_cover.setTranslationX(displacement);

                        dustin_cover.animate().translationX(0).rotation(-120)
                                .setDuration(700).setInterpolator(new DecelerateInterpolator())
                                .start();

                        dustin.animate().translationX(0)
                                .setDuration(700)
                                .setInterpolator(new DecelerateInterpolator())
                                .setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                        dustin.setVisibility(View.VISIBLE);
                                        dustin_cover.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                }).start();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        imageViewMic.animate().translationY(0)
                                .scaleX(1).scaleY(1)
                                .setDuration(700)
                                .setInterpolator(new LinearInterpolator()).setListener(
                                new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        imageViewMic.setVisibility(View.INVISIBLE);
                                        imageViewMic.setRotation(0);

                                        float displacement = 0;

                                        if (isLayoutDirectionRightToLeft) {
                                            displacement = dp * 40;
                                        } else {
                                            displacement = -dp * 40;
                                        }

                                        dustin_cover.animate().rotation(0)
                                                .setDuration(300).setStartDelay(50).start();
                                        dustin.animate().translationX(displacement)
                                                .setDuration(400).setStartDelay(250)
                                                .setInterpolator(new DecelerateInterpolator())
                                                .start();
                                        dustin_cover.animate().translationX(displacement)
                                                .setDuration(400).setStartDelay(250)
                                                .setInterpolator(new DecelerateInterpolator())
                                                .setListener(new Animator.AnimatorListener() {
                                                    @Override
                                                    public void onAnimationStart(Animator animation) {

                                                    }

                                                    @Override
                                                    public void onAnimationEnd(Animator animation) {
                                                        editTextMessage.setVisibility(View.VISIBLE);
                                                        editTextMessage.requestFocus();
                                                        layoutMessage.setVisibility(View.VISIBLE);
//                                                        imageAudioAnimate.setVisibility(View.GONE);
//                                                        linearLayoutMic.setVisibility(View.INVISIBLE);
                                                        UtilsAnimation.slideDown(linearLayoutMic, 400L);
                                                        UtilsAnimation.slideDown(imageAudioAnimate, 300L);
                                                        UtilsAnimation.slideDown(layoutLock, 350L);

                                                    }

                                                    @Override
                                                    public void onAnimationCancel(Animator animation) {

                                                    }

                                                    @Override
                                                    public void onAnimationRepeat(Animator animation) {

                                                    }
                                                }).start();
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                }
                        ).start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }


//    public void animateRecordButton(final float maxPeak) {
////        if (imageAudioAnimate != null && imageAudioAnimate.getVisibility() == View.VISIBLE)
////            imageAudioAnimate.animate().scaleX(1 + maxPeak).scaleY(1 + maxPeak).setDuration(40L).start();
//    }

    private boolean isRecordPermissionGranted() {

        if (recordPermissionHandler == null) {
            canRecord = true;
        }

        canRecord = recordPermissionHandler.isPermissionGranted();

        return canRecord;
    }

    private void showErrorLog(String s) {
        Log.e(TAG, s);
    }


    public enum UserBehaviour {
        CANCELING,
        LOCKING,
        NONE
    }

    public enum RecordingBehaviour {
        CANCELED,
        LOCKED,
        LOCK_DONE,
        RELEASED
    }

    public interface RecordingListener {

        void onRecordingStarted();

        void onRecordingLocked();

        void onRecordingCompleted();

        void onRecordingCanceled();

    }


    public interface RecordPermissionHandler {
        boolean isPermissionGranted();
    }
}
