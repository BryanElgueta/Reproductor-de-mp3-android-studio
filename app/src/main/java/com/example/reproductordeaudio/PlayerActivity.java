package com.example.reproductordeaudio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    Button btnplay , btnnext , btnprev , btnff , btnfr;
    TextView txtsname , txtsstart , txtsstop;
    SeekBar seekmusic;
    ImageView imageview;

    String sname;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaplayer;
    int position;
    ArrayList<File> mySongs;
    Thread updateseekbar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() ==android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportActionBar().setTitle("Estas escuchando:");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnprev = findViewById(R.id.btnprev);
        btnnext = findViewById(R.id.btnnext);
        btnplay = findViewById(R.id.playbtn);
        btnff   = findViewById(R.id.btnff);
        btnfr   = findViewById(R.id.btnfr);
        txtsname   = findViewById(R.id.txtsn);
        txtsstart = findViewById(R.id.txtsstart);
        txtsstop = findViewById(R.id.txtsstop);
        seekmusic = findViewById(R.id.seekbar);
        imageview = findViewById(R.id.imageview);

        if (mediaplayer != null)
        {
            mediaplayer.stop();
            mediaplayer.release();
        }

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = i.getStringExtra("songname");
        position = bundle.getInt("pos", 0);
        txtsname.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        sname = mySongs.get(position).getName();
        txtsname.setText(sname);

        mediaplayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaplayer.start();

        updateseekbar = new Thread() {
            @Override
            public void run() {
                int totalDuration = mediaplayer.getDuration();
                int currentposition = 0;

                while (currentposition<totalDuration){
                    try {
                        sleep(500);
                        currentposition = mediaplayer.getCurrentPosition();
                        seekmusic.setProgress(currentposition);
                    }
                    catch (InterruptedException | IllegalAccessError e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };

        seekmusic.setMax(mediaplayer.getDuration());
        updateseekbar.start();
        seekmusic.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorprimary), PorterDuff.Mode.MULTIPLY);
        seekmusic.getThumb().setColorFilter(getResources().getColor(R.color.colorprimary), PorterDuff.Mode.SRC_IN);

        seekmusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaplayer.seekTo(seekBar.getProgress());

            }
        });

        String endtime = createtime(mediaplayer.getDuration());
        txtsstop.setText(endtime);

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currenttime = createtime(mediaplayer.getCurrentPosition());
                txtsstart.setText(currenttime);
                handler.postDelayed(this, delay);

            }
        }, delay);


        btnplay.setOnClickListener(view -> {
            if (mediaplayer.isPlaying())
            {
                btnplay.setBackgroundResource(R.drawable.ic_play);
                mediaplayer.pause();
            }
            else
            {
                btnplay.setBackgroundResource(R.drawable.ic_pause);
                mediaplayer.start();
            }
        });

        //siguiente listener

        mediaplayer.setOnCompletionListener(mediaPlayer -> btnnext.performClick());

        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaplayer.stop();
                mediaplayer.release();
                position = ((position+1)%mySongs.size());
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaplayer = mediaplayer.create(getApplicationContext(), u);
                sname = mySongs.get(position).getName();
                txtsname.setText(sname);
                mediaplayer.start();
                btnplay.setBackgroundResource(R.drawable.ic_pause);
                startAnimation(imageview);

                String endTime = createtime(mediaplayer.getDuration());
                txtsstop.setText(endTime);
                final Handler handler = new Handler();
                final int delay = 1000;

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String currentTime = createtime(mediaplayer.getCurrentPosition());
                        txtsstart.setText(currentTime);
                        handler.postDelayed(this, delay);
                    }
                }, delay);

            }

    });
        btnprev.setOnClickListener(view -> {
            mediaplayer.stop();
            mediaplayer.release();
            position = ((position-1<0))?(mySongs.size()-1):(position-1);

            Uri u = Uri.parse(mySongs.get(position).toString());
            mediaplayer = MediaPlayer.create(getApplicationContext(), u);
            sname = mySongs.get(position).getName();
            txtsname.setText(sname);

            mediaplayer.start();
            btnplay.setBackgroundResource(R.drawable.ic_pause);
            startAnimation(imageview);

            String endTime = createtime(mediaplayer.getDuration());
            txtsstop.setText(endTime);

        });

        btnff.setOnClickListener(view -> {
            if (mediaplayer.isPlaying())
            {
                mediaplayer.seekTo(mediaplayer.getCurrentPosition()+10000);
            }

        });

        btnfr.setOnClickListener(view -> {
            if (mediaplayer.isPlaying())
            {
                mediaplayer.seekTo(mediaplayer.getCurrentPosition()-10000);
            }
        }
        );
    }

        public void startAnimation(View view)
        {
            ObjectAnimator animator = ObjectAnimator.ofFloat(imageview, "rotation", 0f,360f);
            animator.setDuration(1000);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(animator);
            animatorSet.start();
        }


        public String createtime(int duration)
        {
            String time = "";
            int min = duration/1000/60;
            int sec = duration/1000%60;

            time+=min+":";

            if (sec<10)
            {
                time+="0";
            }
            time+=sec;

            return time;
        }

}