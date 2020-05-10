package com.finalproject.app.ui.VideoStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.fragment.app.Fragment;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import com.finalproject.app.R;
import com.google.android.material.snackbar.Snackbar;
import com.kongqw.rockerlibrary.view.RockerView;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class VideoStream extends Fragment {
    private static final boolean USE_TEXTURE_VIEW = false;
    private static final boolean ENABLE_SUBTITLES = true;
    private static String VLC_STREAM_PROTOCOL = "rtsp"; //影像串流 protocol
    private static int VLC_STREAM_PORT = 8090; //影像串流 port

    private View view;
    private ProgressDialog mDialog;

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
    private SurfaceView mVideoView;
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

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener( new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if( keyCode == KeyEvent.KEYCODE_BACK )
                {
                    if(clientThread != null)
                        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
                        clientThread.txMsg(CAMERA_OFF);
                        clientThread = null;
                }
                return false;
            }

        } );

        return view;
    }

    /**
     *
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

    private void setListener(){
        textViewState = view.findViewById(R.id.textViewState);
        mVideoView = view.findViewById(R.id.vidolayout);
        editTextAddr = view.findViewById(R.id.editTextAddr);
        btnConnect = view.findViewById(R.id.buttonConnect);
        btnManualControl = view.findViewById(R.id.buttonManualControl);
        btnAutoControl = view.findViewById(R.id.buttonAutoControl);
        btnDisconnect = view.findViewById(R.id.buttonDisconnect);
        btnImageDisconnect = view.findViewById(R.id.imageDisconnectButton);
        rockerController = view.findViewById(R.id.rocker);

        btnConnect.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] temp = editTextAddr.getText().toString().split(":");
                Pattern numPattern = Pattern.compile("[0-9]*");
                try {
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

                btnConnect.setEnabled(false);
                btnDisconnect.setEnabled(false);
            }
        });
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clientThread != null){
                    clientThread.txMsg(AUTO_STOP);
                    clientThread.txMsg(CAMERA_OFF);
                    videoStop();
                    clientThread.setRunning(false);
                }
            }
        });
        btnManualControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clientThread.isRunning()) {
                    clientThread.txMsg(CAMERA_ON);
                    Objects.requireNonNull(getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        });
        btnAutoControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    btnManualControl.setEnabled(false);
                    btnAutoControl.setBackgroundColor(view.getResources().getColor(R.color.green));
                    clientThread.txMsg(AUTO_START);
                    videoStop();
                }else{
                    btnManualControl.setEnabled(true);
                    btnAutoControl.setBackground(defaultToggleButtonColor);
                    clientThread.txMsg(AUTO_STOP);
                }
            }
        });
        btnImageDisconnect.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clientThread != null){
                    videoStop();
                    clientThread.txMsg(CAMERA_OFF);
                    Objects.requireNonNull(getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
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
                if(clientThread != null){
                    clientThread.txMsg(STOP);
                    clientThread.txMsg(nowDirection);
                }
                //Snackbar.make(view, nowDirection, Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onFinish() {
                if(clientThread != null){
                    clientThread.txMsg(STOP);
                }
            }
        });
    }

    private void clearCurrentFrame() {
        mVideoView.setVisibility(View.GONE);
        mVideoView.setVisibility(View.VISIBLE);
    }

    private void playStream(String src){
        Uri UriSrc = Uri.parse(src);
        if(UriSrc == null){
            Toast.makeText(view.getContext(),
                    "UriSrc == null", Toast.LENGTH_LONG).show();
        }else{
            ArrayList<String> args = new ArrayList<>();
            args.add("-vvv");
            args.add(":network-caching=500");//網路快取
            args.add(":rtsp-frame-buffer-size=1000"); //RTSP幀緩衝大小，預設大小為100000
            libVLC = new LibVLC(view.getContext(), args);
            mediaPlayer = new MediaPlayer(libVLC);

            // Set URL
            final Media media = new Media(libVLC, UriSrc);
            mediaPlayer.setMedia(media);
            media.release();

            vout = mediaPlayer.getVLCVout();
            vout.setVideoView(mVideoView);
            vout.attachViews();

            try {
                TimeUnit.SECONDS.sleep(4);
                mediaPlayer.play();
            } catch (InterruptedException e){

            }
            setDimension();

            Toast.makeText(view.getContext(),
                    "Connect to Raspi",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void setDimension() {
        // Adjust the size of the video
        // so it fits on the screen
        float videoProportion = 1.2f;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        vout.setWindowSize(screenWidth+30, screenHeight-50);
    }

    private void updateState(String state){
        String[] temp = state.split(":");
        textViewState.setText(temp[0]);
        textViewState.setTextColor(Color.parseColor(temp[1]));
    }

    private void clienStart(){
        if(clientThread != null)
            updateState("connected:#28FF28");

        btnDisconnect.setEnabled(true);
        btnAutoControl.setChecked(false);

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
        clientThread = null;
        updateState("clientEnd:#ff0000");

        btnConnect.setEnabled(true);
        btnDisconnect.setEnabled(false);

        //Button Animation
        Transition transition = new Slide(Gravity.START);
        transition.setDuration(600);
        transition.addTarget(R.id.buttonManualControl);
        transition.addTarget(R.id.buttonAutoControl);

        TransitionManager.beginDelayedTransition((ViewGroup)view.getParent(), transition);
        btnManualControl.setVisibility(View.GONE);
        btnAutoControl.setVisibility(View.GONE);
    }

    private void clientError(){
        clientThread = null;
        updateState("Timed Out:#ff0000");

        btnConnect.setEnabled(true);
        btnDisconnect.setEnabled(false);
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
                    parent.updateState((String)msg.obj);
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

    private void videoStop(){
        clearCurrentFrame();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.detachViews();
        }
        mVideoView.setVisibility(View.INVISIBLE);
        rockerController.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onStop() {
        super.onStop();
        saveData();
        videoStop();
        //Objects.requireNonNull(getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearCurrentFrame();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            libVLC.release();
        }
    }



    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if(clientThread != null && clientHandler != null){
            outState.putParcelable("client", clientThread);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if(savedInstanceState != null){
            clientThread = savedInstanceState.getParcelable("client");

            ((ViewGroup.MarginLayoutParams)btnImageDisconnect.getLayoutParams()).topMargin=getActionBarHeight();

            String[] temp = editTextAddr.getText().toString().split(":");
            playStream(VLC_STREAM_PROTOCOL + "://" + temp[0] + ":" + VLC_STREAM_PORT + "/");
        }
    }
}
