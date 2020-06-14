package com.TravelNotes.app.ui.VideoStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.fragment.app.Fragment;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.videolan.libvlc.interfaces.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import com.TravelNotes.app.Event;
import com.TravelNotes.app.R;
import com.google.android.material.snackbar.Snackbar;
import com.kongqw.rockerlibrary.view.RockerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class VideoStream extends Fragment {
    private static final boolean USE_TEXTURE_VIEW = false;
    private static final boolean ENABLE_SUBTITLES = true;
    private static final String TAG = "VideoStream";
    private static String VLC_STREAM_PROTOCOL = "rtsp"; //影像串流 protocol
    private static int VLC_STREAM_PORT = 8090; //影像串流 port

    private View view;
    private Event loadingDialog;

    private static String CAMERA_ON = "Cameraon";
    private static String CAMERA_OFF = "Cameraoff";
    private static String UP = "Up";
    private static String DOWN = "Down";
    private static String LEFT = "Left";
    private static String RIGHT = "Right";
    private static String AUTO_START = "Autostart";
    private static String AUTO_STOP = "Autostop";
    private static String STOP = "Stop";
    private Drawable defaultToggleButtonColor;

    private IVLCVout vout;
    private LibVLC libVLC;
    private MediaPlayer mediaPlayer;
    private SurfaceView mSurface;
    private SurfaceHolder holder;
    private int mVideoWidth;
    private int mVideoHeight;

    private TextView textViewState;
    private EditText editTextAddr;
    private Button btnConnect;
    private Button btnDisconnect;
    private Button btnManualControl;
    private ImageButton btnImageDisconnect;
    private ToggleButton btnAutoControl;
    private RockerView rockerController;

    private ClientHandler clientHandler;
    private ClientThread clientThread;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_video_stream, container, false);

        Guideline guideline = view.findViewById(R.id.guideline);
        guideline.setGuidelineBegin(getActionBarHeight());

        clientHandler = new ClientHandler(this);
        setListener();
        defaultToggleButtonColor = btnAutoControl.getBackground();

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if( keyCode == KeyEvent.KEYCODE_BACK )
                    Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).show();
                return false;
            }
        });

        return view;
    }

    /**
     * getActionBarHeight
     * @return height of action bar
     */
    private int getActionBarHeight() {
        TypedValue tv = new TypedValue();
        if (view.getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            return TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        return -1;
    }

    private void closeKeyboard() {
        View ve = requireActivity().getCurrentFocus();
        if (ve != null){
            InputMethodManager imm = (InputMethodManager)requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(ve.getWindowToken(), 0);
        }
    }

    private void setListener(){
        textViewState = view.findViewById(R.id.textViewState);
        mSurface = view.findViewById(R.id.vidolayout);
        editTextAddr = view.findViewById(R.id.editTextAddr);
        btnConnect = view.findViewById(R.id.buttonConnect);
        btnManualControl = view.findViewById(R.id.buttonManualControl);
        btnAutoControl = view.findViewById(R.id.buttonAutoControl);
        btnDisconnect = view.findViewById(R.id.buttonDisconnect);
        btnImageDisconnect = view.findViewById(R.id.imageDisconnectButton);
        rockerController = view.findViewById(R.id.rocker);

        holder = mSurface.getHolder();

        btnConnect.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard();
                String[] temp = editTextAddr.getText().toString().split(":");
                Pattern numPattern = Pattern.compile("[0-9]*");
                try {
                    if(!Pattern.matches("^[0-9+]{1,3}.[0-9+]{1,3}.[0-9+]{1,3}.[0-9+]{1,3}$", temp[0])) {
                        Snackbar.make(view, "IP Address error", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    if(!numPattern.matcher(temp[1]).matches()){
                        Snackbar.make(view, "Port number error", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    if(Integer.parseInt(temp[1]) >= 65536){
                        Snackbar.make(view, "Port out of range", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                }catch (ArrayIndexOutOfBoundsException e){
                    Snackbar.make(view, "Cannot format ip address", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                clientThread = new ClientThread(temp[0], Integer.parseInt(temp[1]), clientHandler);
                clientThread.start();
                //防止在連線中再點一次
                btnConnect.setEnabled(false);
                btnDisconnect.setEnabled(false);
            }
        });
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClientThread.setRunning(false);
            }
        });
        btnManualControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clientThread.txMsg(CAMERA_ON);
                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        });
        btnAutoControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    clientThread.txMsg(CAMERA_OFF);
                    clientThread.txMsg(AUTO_START);
                }else{
                    clientThread.txMsg(AUTO_STOP);
                }
            }
        });
        btnImageDisconnect.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClientThread.setRunning(false);
                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        });
        rockerController.setCallBackMode(RockerView.CallBackMode.CALL_BACK_MODE_STATE_CHANGE);
        rockerController.setOnShakeListener(RockerView.DirectionMode.DIRECTION_4_ROTATE_45, new RockerView.OnShakeListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void direction(RockerView.Direction direction) {
                String nowDirection = null;
                switch (direction){
                    case DIRECTION_UP:
                        nowDirection = UP;
                        break;
                    case DIRECTION_DOWN:
                        nowDirection = DOWN;
                        break;
                    case DIRECTION_LEFT:
                        nowDirection = LEFT;
                        break;
                    case DIRECTION_RIGHT:
                        nowDirection = RIGHT;
                        break;
                }
                clientThread.txMsg(STOP);
                clientThread.txMsg(nowDirection);
            }

            @Override
            public void onFinish() {
                clientThread.txMsg(STOP);
            }
        });
    }

    private void playStream(String src){
        Uri UriSrc = Uri.parse(src);
        if(UriSrc == null){
            Toast.makeText(view.getContext(),
                    "UriSrc == null", Toast.LENGTH_LONG).show();
        }else{
            //try {
                ArrayList<String> args = new ArrayList<>();
                args.add("-vvv");
                args.add("--network-caching=" + 1000);//網路快取
                args.add(":rtsp-frame-buffer-size=1000"); //RTSP幀緩衝大小，預設大小為100000
                libVLC = new LibVLC(view.getContext(), args);

                // Create media player
                mediaPlayer = new MediaPlayer(libVLC);
                MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);
                mediaPlayer.setEventListener(mPlayerListener);

                // Set up video output
                final IVLCVout vout = mediaPlayer.getVLCVout();
                vout.setVideoView(mSurface);
                vout.addCallback(new IVLCVout.Callback() {
                    @Override
                    public void onSurfacesCreated(IVLCVout ivlcVout) {
                        setSize(ivlcVout);
                    }

                    @Override
                    public void onSurfacesDestroyed(IVLCVout ivlcVout) {

                    }
                });
                vout.attachViews();

                // Set URL
                final Media media = new Media(libVLC, UriSrc);
                mediaPlayer.setMedia(media);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TimeUnit.SECONDS.sleep(6);
                            mediaPlayer.play();
                        } catch (InterruptedException ignore) {
                        }
                    }
                }).start();

                Toast.makeText(view.getContext(),
                        "Connect to Raspi",
                        Toast.LENGTH_LONG).show();
            /*}catch (Exception e){
                Toast.makeText(view.getContext(), "Error creating player!", Toast.LENGTH_LONG).show();
            }*/
        }
    }

    /*************
     * Surface
     *************/
    private void setSize(IVLCVout ivlcVout) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mVideoWidth = displayMetrics.widthPixels;
        mVideoHeight = displayMetrics.heightPixels;

        // set display size
        ViewGroup.LayoutParams lp = mSurface.getLayoutParams();
        lp.width = mVideoWidth;
        lp.height = mVideoHeight;
        mSurface.setLayoutParams(lp);
        mSurface.invalidate();
        ivlcVout.setWindowSize(mVideoWidth, mVideoHeight);
    }

    private void updateState(String state, String color){
        textViewState.setText(state);
        textViewState.setTextColor(Color.parseColor(color));
    }

    private void clienStart(){
        updateState("connected","#28FF28");
        btnConnect.setEnabled(false);
        btnDisconnect.setEnabled(true);

        clientThread.txMsg(CAMERA_OFF);
        clientThread.txMsg(AUTO_STOP);

        //Button Animation
        Transition transition = new Slide(Gravity.START);
        transition.setDuration(600);
        transition.addTarget(R.id.buttonManualControl);
        transition.addTarget(R.id.buttonAutoControl);

        TransitionManager.beginDelayedTransition((ViewGroup)view.getParent(), transition);
        btnManualControl.setVisibility(btnManualControl.isCursorVisible() ? View.VISIBLE : View.GONE);
        btnAutoControl.setVisibility(btnAutoControl.isCursorVisible() ? View.VISIBLE : View.GONE);
    }

    private void clientEnd(){
        Log.d(TAG, "clientEnd: ");
        clientThread = null;
        updateState("clientEnd","#ff0000");
        btnConnect.setEnabled(true);
        btnDisconnect.setEnabled(false);

        btnManualControl.setVisibility(View.GONE);
        btnAutoControl.setVisibility(View.GONE);
    }

    private void clientError(){
        clientThread = null;
        updateState("Timed Out","#ff0000");
        btnConnect.setEnabled(true);
        btnDisconnect.setEnabled(false);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private static class MyPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<VideoStream> mOwner;

        public MyPlayerListener(VideoStream owner) {
            mOwner = new WeakReference<VideoStream>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            VideoStream player = mOwner.get();
            Log.d(TAG, "Player EVENT: "+event.type);
            switch(event.type) {
                case MediaPlayer.Event.EndReached:
                    Log.d(TAG, "MediaPlayerEndReached");
                    player.releasePlayer();
                    break;
                case MediaPlayer.Event.EncounteredError:
                    Log.d(TAG, "Media Player Error, re-try");
                    //player.releasePlayer();
                    break;
                case MediaPlayer.Event.Playing:
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                default:
                    break;
            }
        }
    }

    public static class ClientHandler extends Handler {
        static final int UPDATE_STATE = 0;
        static final int UPDATE_START = 1;
        static final int UPDATE_END = 2;
        static final int UPDATE_ERROR = 3;
        private VideoStream parent;

        ClientHandler(VideoStream parent) {
            super();
            this.parent = parent;
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case UPDATE_STATE:
                    String str = (String)msg.obj;
                    String[] temp = str.split(":");
                    parent.updateState(temp[0], temp[1]);
                    break;
                case UPDATE_START:
                    parent.clienStart();
                    break;
                case UPDATE_END:
                    parent.clientEnd();
                    break;
                case UPDATE_ERROR:
                    parent.clientError();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void loadData(){
        editTextAddr = view.findViewById(R.id.editTextAddr);
        SharedPreferences settings = view.getContext().getSharedPreferences("key", 0);
        editTextAddr.setText(settings.getString("Text", view.getContext().getString(R.string.default_ip)));
    }

    private void saveData(){
        editTextAddr = view.findViewById(R.id.editTextAddr);
        SharedPreferences settings = view.getContext().getSharedPreferences("key", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("Text", editTextAddr.getText().toString());
        editor.apply();
    }

    // TODO: handle this cleaner
    private void releasePlayer() {
        if (libVLC == null)
            return;
        mediaPlayer.stop();
        final IVLCVout vout = mediaPlayer.getVLCVout();
        vout.detachViews();
        holder = null;
        libVLC.release();
        libVLC = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();

        if(requireActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && clientThread != null) {
            String[] temp = editTextAddr.getText().toString().split(":");
            playStream(VLC_STREAM_PROTOCOL + "://" + temp[0] + ":" + VLC_STREAM_PORT + "/");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    public void onStop() {
        super.onStop();
        saveData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clientThread = null;
        releasePlayer();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("client", clientThread);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d(TAG, "onViewStateRestored: ");
        if(savedInstanceState != null){
            clientThread = savedInstanceState.getParcelable("client");
            ((ViewGroup.MarginLayoutParams)btnImageDisconnect.getLayoutParams()).topMargin=getActionBarHeight();
        }
    }
}
