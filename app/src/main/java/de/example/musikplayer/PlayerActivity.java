package de.example.musikplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
    Button btnplay, btnnext, btnprev, btnff, btnfr;    /*Initialisierung aller Buttons ,die in der activity_player.xml stehen */
    TextView txtsname, txtsstart, txtsstop;
    SeekBar seekmusic;


    String sname;                                                 /*mediaplayerObject erstellen*/
    public static final String EXTRA_NAME = "song_name";

    private MediaPlayer mediaplayer;
    ArrayList<File> mySongs;
    int position;
    Thread updateseekbar;          /* */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        btnprev = findViewById(R.id.btnprev);                         /*alle Objekten in der onCreate-Methode initializieren*/
        btnnext = findViewById(R.id.btnnext);
        btnplay = findViewById(R.id.playbtn);
        btnff = findViewById(R.id.btnff);
        btnfr = findViewById(R.id.btnfr);
        txtsname = findViewById(R.id.txtsn);
        txtsstart = findViewById(R.id.txtsstart);
        txtsstop = findViewById(R.id.txtsstop);
        seekmusic = findViewById(R.id.seekbar);

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = i.getStringExtra("songname");
        position = bundle.getInt("pos", 0);                  /*ich benutze songs,songname,pos auch in der mainActivity.java in der onItemClick-methode*/
        txtsname.setSelected(true);                                        // Songname  die in der List angeklickt wird in der mediaplayer  angezeigt
        Uri uri = Uri.parse(mySongs.get(position).toString());
        sname = mySongs.get(position).getName();
        txtsname.setText(sname);

        if (mediaplayer != null) {               /*mediaplayer checken*/
            mediaplayer.stop();
            mediaplayer.release();
        }

        mediaplayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaplayer.start();

        updateseekbar = new Thread() {
            @Override
            public void run() {
                int totalDuration = mediaplayer.getDuration();
                int currentposition = 0;
                while (currentposition < totalDuration) {
                    try {
                        sleep(500);                                       /*sleep von 500 millisekundes*/
                        currentposition = mediaplayer.getCurrentPosition();      /*dann wird mit getCurrentposition()  zu neuer currentposition gehen*/

                    } catch (InterruptedException | IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        System.out.println("Added new project");
        seekmusic.setMax(mediaplayer.getDuration());                         /*maximale Position von Seekbar*/
        seekmusic.getProgressDrawable().setColorFilter(getResources().getColor(R.color.design_default_color_primary), PorterDuff.Mode.MULTIPLY);
        seekmusic.getThumb().setColorFilter(getResources().getColor(R.color.design_default_color_primary), PorterDuff.Mode.SRC_IN);
        updateseekbar.start();
        seekmusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {     /*für die automatische Update der Seekbar, wenn der Nutzer es manuelle ändert*/
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaplayer.seekTo(seekBar.getProgress());                     /*automatisch zu der seekbar Position, die vom Benutzer ausgewählt wird*/
            }
        });


        String endTime = createTime(mediaplayer.getDuration());                    /* ich habe die String endTime definiert um die Dauer des Liedes mit hilfe der createTime Methode in Sekunden zu konvertieren.*/
        txtsstop.setText(endTime);                                                 /*txtsstop.setText(endTime) gibt die in sekunde umgerechnete totalDuration des Liedes.*/
        final Handler handler = new Handler();                            /* currentime wird nach jede Seconde updaten mithilfe diese Handler*/
        final int delay = 1000;                                     /*delay für das Update ist 1000 millisekunde gleich eine Sekunde*/

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaplayer.getCurrentPosition());          // umrechenen der currentposition in minuten und sekunden
                txtsstart.setText(currentTime);
                seekmusic.setProgress(mediaplayer.getCurrentPosition());                    // update  der currentposition und currentTime mit delay nach (jede Sekunde)
                handler.postDelayed(this, delay);
            }
        }, delay);

        btnplay.setOnClickListener(new View.OnClickListener() {            /*play- und pauseButton emplementieren*/
            @Override
            public void onClick(View v) {
                if (mediaplayer.isPlaying()) {
                    btnplay.setBackgroundResource(R.drawable.ic_play);
                    mediaplayer.pause();
                } else {
                    btnplay.setBackgroundResource(R.drawable.ic_pause);
                    mediaplayer.start();
                }

              mediaplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Toast.makeText(PlayerActivity.this, "The music is finished", Toast.LENGTH_LONG).show();      //
                    }
                });
            }
        });
        btnnext.setOnClickListener(new View.OnClickListener() {         /*implementierung von btnnext für den Vorwärt*/
            @Override
            public void onClick(View v) {
                mediaplayer.stop();
                mediaplayer.release();

                startNextSong();                               /*geht auf dem nächsten Son */

                String endTime = createTime(mediaplayer.getDuration());
                txtsstop.setText(endTime);                             /*Anzeige der Totalduration des Sons*/

                seekmusic.setProgress(0);                        // set seekbar position to 0
                seekmusic.setMax(mediaplayer.getDuration());    // gibt eine neuen Maximal seekbar Dauer für den neuen son wenn man auf btnnext anklickt

                btnplay.setBackgroundResource(R.drawable.ic_pause);
            }
        });

        // Automatisches Spiel von nächstem son
        mediaplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
               // btnnext.performClick();                                      /*geht automatisch auf dem nächsten Son*/
                startNextSong();                                                 /*StartNextsong() funktion aurufen für das automatische Spiel des nächsten Son*/

                seekmusic.setMax(mediaplayer.getDuration());                         /*maximale Position von Seekbar*/
                seekmusic.setProgress(0);                             //setzte die Anfangszeit auf 0
                String endTime = createTime(mediaplayer.getDuration());
                txtsstop.setText(endTime);                             /*Anzeige der Totalduration des Sons*/
            }
        });

        btnprev.setOnClickListener(new View.OnClickListener() {                  /*implementierung von btnprev  für die Rückswärt*/
            @Override
            public void onClick(View v) {
                mediaplayer.stop();
                mediaplayer.release();
                position = ((position - 1) < 0) ? (mySongs.size() - 1) : (position - 1);
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaplayer = MediaPlayer.create(getApplicationContext(), u);
                sname = mySongs.get(position).getName();
                txtsname.setText(sname);
                mediaplayer.start();
                String endTime = createTime(mediaplayer.getDuration());
                txtsstop.setText(endTime);                             /*Anzeige der Totalduration des Sons*/

                // setzte die Anfangszeit auf 0
                seekmusic.setProgress(0);
                seekmusic.setMax(mediaplayer.getDuration());          // gibt Maximal seekbar Dauer für den neuen son wenn man auf btnprev anklickt
                btnplay.setBackgroundResource(R.drawable.ic_pause);
            }
        });

        btnff.setOnClickListener(new View.OnClickListener() {                /*implementierung von btnff für die Vorwärtsspulen  */
            @Override
            public void onClick(View v) {
                if (mediaplayer.isPlaying()) {
                    mediaplayer.seekTo(mediaplayer.getCurrentPosition() + 10000);     // inkrementation mit 10000 millisekunden
                }
            }
        });

        btnfr.setOnClickListener(new View.OnClickListener() {                 /*implementierung von btnfr  für die Rückwärtsspulen */
            @Override
            public void onClick(View v) {
                if (mediaplayer.isPlaying()) {
                    mediaplayer.seekTo(mediaplayer.getCurrentPosition() - 10000);     // Dekrementation mit 10000 millisekunden
                }
            }
        });
    }

    private void startNextSong() {                                      /*Methode zum starten der nextson */
        position = ((position + 1) % mySongs.size());
        Uri u = Uri.parse(mySongs.get(position).toString());
        mediaplayer = MediaPlayer.create(getApplicationContext(), u);
        sname = mySongs.get(position).getName();
        txtsname.setText(sname);

        mediaplayer.start();
    }


    @Override
    protected void onPause() {           // diese Methode onPause dient dazu ,dass sich bei der Abwechslung der Items keine gleichzeitige Spiel der son ergibt
        super.onPause();
        if (mediaplayer != null) {
            if (mediaplayer.isPlaying()) {
                System.out.println("");
                mediaplayer.stop();
            }
        }
    }

    public String createTime(int duration) {                         /*Methode um die Zeit von Minuten zu Sekunden umzurechnen*/

        String time = "";
        int minute = duration / 1000 / 60;                           // von millisekunden zu minuten
        int seconde = duration / 1000 % 60;                          // von millisekunden zu sekunde
        time += minute + ":";
        if (seconde < 10) {
            time += "0";
        }
        time += seconde;
        return time;
    }

}