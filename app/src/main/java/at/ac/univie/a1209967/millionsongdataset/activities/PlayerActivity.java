package at.ac.univie.a1209967.millionsongdataset.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.Image;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.RequiresPermission;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import android.os.Handler;

import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import at.ac.univie.a1209967.millionsongdataset.Management;
import at.ac.univie.a1209967.millionsongdataset.R;
import at.ac.univie.a1209967.millionsongdataset.adapter.GenreAdapter;
import at.ac.univie.a1209967.millionsongdataset.adapter.SongAdapter;
import at.ac.univie.a1209967.millionsongdataset.controller.MusicController;
import at.ac.univie.a1209967.millionsongdataset.entities.Song;
import at.ac.univie.a1209967.millionsongdataset.getter.GenreGetter;
import at.ac.univie.a1209967.millionsongdataset.service.MusicService;

 /**
 * Created by Adlbert on 04.05.2016.
 * Diese Klasse wurde aus dem Tutorium zur Erstellung eines Musikplayers entnommen.
 * http://code.tutsplus.com/tutorials/create-a-music-player-on-android-song-playback--mobile-22778
 */

public class PlayerActivity extends AppCompatActivity implements MediaPlayerControl, View.OnClickListener, AdapterView.OnItemSelectedListener {

    public Management management;

    private ArrayList<Song> songList;
    private ArrayList<String> genreList;
    private GenreAdapter genreAdt;
    private SongAdapter songAdt;
    public ArrayList<Song> historyList;
    public ArrayList<Song> favList;
    private ListView songView;
    private ListView genreView;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private MusicController controller;
    private boolean paused=false, playbackPaused=true, pref=false, shuffle=false, loop=false;
    private Handler seekHandler = new Handler();

    private String selectedItem = "";
    private Button nextBtn;
    private Button prefBtn;
    private ImageButton playBtn;
    private ImageButton shuffleBtn;
    private ImageButton skipfBtn;
    private ImageButton skipbBtn;
    private ImageButton repeatBtn;
    private Button historyBtn;
    private Button searchBtn;
    private Button favouritBtn;
    private Button offerBtn;
    private ImageButton menuBtn;
    private ImageButton favBtn;
    private ImageButton infoBtn;
    private TextView titelView;
    private TextView interpretView;
    private TextView albumView;
    private TextView maxTimeView;
    private TextView minTimeView;
    private SeekBar timeBar;
    private int currentPos;

    private static final String CLIENT_ID = "c57b7c1903b146ae93e23873ab5abf3f";
    private static final String REDIRECT_URI = "MillionSongDataset-a1209967://callback";
    private static final int REQUEST_CODE = 1337;

    public final static String EXTRA_HIST = "at.ac.univie.a1209967.millionsongdataset.HISTLIST";
    public final static String EXTRA_SONGLIST = "at.ac.univie.a1209967.millionsongdataset.SONGLIST";
    public final static String EXTRA_SONG ="at.ac.univie.a1209967.millionsongdataset.SONG";





     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        management = Management.getInstance();

        songView = (ListView)findViewById(R.id.song_list);
        genreView = (ListView)findViewById(R.id.genre_list);

        songList = new ArrayList<Song>();
        historyList = new ArrayList<Song>();
        favList = new ArrayList<Song>();

        currentPos = 0;


        genreList =new ArrayList<>(Arrays.asList("acoustic", "afrobeat", "alt-rock", "alternative", "ambient", "anime", "black-metal", "bluegrass", "blues", "bossanova", "brazil", "breakbeat", "british", "cantopop", "chicago-house", "children", "chill", "classical", "club", "comedy", "country", "dance", "dancehall", "death-metal", "deep-house", "detroit-techno", "disco", "disney", "drum-and-bass", "dub", "dubstep", "edm", "electro", "electronic", "emo", "folk", "forro", "french", "funk", "garage", "german", "gospel", "goth", "grindcore", "groove", "grunge", "guitar", "happy", "hard-rock", "hardcore", "hardstyle", "heavy-metal", "hip-hop", "holidays", "honky-tonk", "house", "idm", "indian", "indie", "indie-pop", "industrial", "iranian", "j-dance", "j-idol", "j-pop", "j-rock", "jazz", "k-pop", "kids", "latin", "latino", "malay", "mandopop", "metal", "metal-misc", "metalcore", "minimal-techno", "movies", "mpb", "new-age", "new-release", "opera", "pagode", "party", "philippines-opm", "piano", "pop", "pop-film", "post-dubstep", "power-pop", "progressive-house", "psych-rock", "punk", "punk-rock", "r-n-b", "rainy-day", "reggae", "reggaeton", "road-trip", "rock", "rock-n-roll", "rockabilly", "romance", "sad", "salsa", "samba", "sertanejo", "show-tunes", "singer-songwriter", "ska", "sleep", "songwriter", "soul", "soundtracks", "spanish", "study", "summer", "swedish", "synth-pop", "tango", "techno", "trance", "trip-hop", "turkish", "work-out", "world-music"));
        getSongList();

        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);

        genreAdt = new GenreAdapter(this, genreList);
        genreView.setAdapter(genreAdt);
        genreView.setVisibility(View.INVISIBLE);


        timeBar = (SeekBar)findViewById(R.id.seekBar);
        timeBar.setMax(songList.get(0).getLength()/1000);
        timeBar.setProgress(0);

        nextBtn = (Button)findViewById(R.id.next_Btn);
        prefBtn = (Button)findViewById(R.id.pref_Btn);
        playBtn = (ImageButton)findViewById(R.id.play_Btn);
        shuffleBtn = (ImageButton)findViewById(R.id.shuffle_Btn);
        skipfBtn = (ImageButton)findViewById(R.id.skipf_Btn);
        skipbBtn = (ImageButton)findViewById(R.id.skipb_Btn);
        historyBtn=(Button) findViewById(R.id.historyBtn);
        searchBtn=(Button) findViewById(R.id.searchBtn);
        favouritBtn = (Button) findViewById(R.id.favBtn);
        offerBtn = (Button) findViewById(R.id.sugBtn);
        favBtn = (ImageButton)findViewById(R.id.fav_Btn);
        infoBtn = (ImageButton)findViewById(R.id.info_Btn);
        menuBtn =(ImageButton)findViewById(R.id.menuBtn);

        titelView = (TextView)findViewById(R.id.titel_View);
        interpretView = (TextView)findViewById(R.id.int_View);
        albumView = (TextView)findViewById(R.id.album_View);
        maxTimeView = (TextView)findViewById(R.id.max_Time);
        minTimeView = (TextView)findViewById(R.id.zero_Time);

        shuffleBtn.setImageResource(R.drawable.shuffle_not);
        repeatBtn = (ImageButton)findViewById(R.id.repeat_Btn);
        playBtn.setImageResource(R.drawable.play);
        repeatBtn.setImageResource(R.drawable.repeat_not);

        favBtn.setOnClickListener(this);
        infoBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        prefBtn.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        shuffleBtn.setOnClickListener(this);
        repeatBtn.setOnClickListener(this);
        skipfBtn.setOnClickListener(this);
        skipbBtn.setOnClickListener(this);

        titelView.setText(songList.get(0).getTitle());
        interpretView.setText(songList.get(0).getTitle());
        albumView.setText(songList.get(0).getAlbum());
        maxTimeView.setText(String.format("%02d : %02d ",
                TimeUnit.MILLISECONDS.toMinutes(songList.get(1).getLength()),
                TimeUnit.MILLISECONDS.toSeconds(songList.get(1).getLength()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songList.get(1).getLength()))
        ));

        historyBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent historyIntent = new Intent(getApplicationContext(), HistoryActivity.class);
                if(historyList!=null) {
                    historyIntent.putExtra(EXTRA_HIST, historyList);
                }
                startActivity(historyIntent);
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent searchIntent = new Intent(getApplicationContext(), SearchActivity.class);
                searchIntent.putExtra(EXTRA_SONGLIST, songList);
                startActivity(searchIntent);
            }
        });

        favouritBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent favIntent = new Intent(getApplicationContext(), FavouritesActivity.class);
                favIntent.putExtra(EXTRA_SONGLIST, favList);
                startActivity(favIntent);
            }
        });


        offerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sugIntent = new Intent(getApplicationContext(), SuggestionsActivity.class);
                sugIntent.putExtra(EXTRA_SONGLIST, songList);
                sugIntent.putExtra(EXTRA_HIST, songList);
                startActivity(sugIntent);
            }
        });

        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent infoIntent = new Intent(getApplicationContext(), InfoActivity.class);
                infoIntent.putExtra(EXTRA_SONG, songList.get(musicSrv.getSongPosn()));
                startActivity(infoIntent);
            }
        });


        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent menuIntent = new Intent(getApplicationContext(), UsermenuActivity.class);
                startActivity(menuIntent);
            }
        });





        timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                if(musicSrv != null && fromUser) {
                    seekTo(progress*1000);
                    setText();
                    if (playbackPaused)
                        play();
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        setController();

        //Make sure you update Seekbar on UI thread
        PlayerActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(musicSrv != null && !playbackPaused){
                    int mCurrentPosition = getCurrentPosition()/1000;
                    System.err.println( getCurrentPosition()/1000);
                    //currentPos = mCurrentPosition;
                    setText();
                }
                seekHandler.postDelayed(this, 1000);
            }
        });
    }

     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.next_Btn: {
                 if (pref) {
                     View songView = findViewById(R.id.song_list);
                     View genreView = findViewById(R.id.genre_list);
                     songView.setVisibility(View.VISIBLE);
                     genreView.setVisibility(View.INVISIBLE);
                     pref = !pref;
                 }
                 break;
             }

             case R.id.pref_Btn: {
                 if (!pref) {
                     View songView = findViewById(R.id.song_list);
                     View genreView = findViewById(R.id.genre_list);
                     songView.setVisibility(View.INVISIBLE);
                     genreView.setVisibility(View.VISIBLE);
                     pref = !pref;
                 }
                 break;
             }

             case R.id.play_Btn: {
                /*added*/

                 play();
                 //adding played song to history

                 if(songList.get(musicSrv.getSongPosn())!=null && !(historyList.contains(songList.get(musicSrv.getSongPosn())))) {
                     historyList.add(songList.get(musicSrv.getSongPosn()));
                 }

                 break;
             }

             case R.id.shuffle_Btn: {
                 setShuffle();
                 break;
             }

             case R.id.repeat_Btn: {
                 setLooping();
                 break;
             }

             case R.id.skipf_Btn: {
                 if (songList.get(musicSrv.getSongPosn()).getFav()) {
                     favBtn.setImageResource(R.drawable.favorite);
                 }
                 else {
                     favBtn.setImageResource(R.drawable.favorite_not);
                 }
                 if (playbackPaused)
                     skipf();
                 else
                     playNext();
                 break;
             }


             case R.id.skipb_Btn: {
                 if (songList.get(musicSrv.getSongPosn()).getFav()) {
                     favBtn.setImageResource(R.drawable.favorite);
                 }
                 else {
                     favBtn.setImageResource(R.drawable.favorite_not);
                 }
                 if (playbackPaused)
                     skipb();
                 else
                    playPrev();
                 break;
             }


             case R.id.fav_Btn:{
                 if (!songList.get(musicSrv.getSongPosn()).getFav()) {
                     favBtn.setImageResource(R.drawable.favorite);
                     songList.get(musicSrv.getSongPosn()).setFav(true);
                     favList.add(songList.get(musicSrv.getSongPosn()));
                 }
                 else {
                     favBtn.setImageResource(R.drawable.favorite_not);
                     songList.get(musicSrv.getSongPosn()).setFav(false);
                     favList.remove(songList.get(musicSrv.getSongPosn()));
                 }
             }


         }
     }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        paused=true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(paused){
            setController();
            paused=false;
        }
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }


    public void songPicked(View view){
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong(playbackPaused);
        setText();
        setTimeBar();
        playBtn.setImageResource(R.drawable.pause);
        playbackPaused = !playbackPaused;
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        //controller.show(0);
    }

     public void genrePicked(View view){
         selectedItem = view.getTag().toString();
         View songView = findViewById(R.id.song_list);
         View genreView = findViewById(R.id.genre_list);
         songView.setVisibility(View.VISIBLE);
         genreView.setVisibility(View.INVISIBLE);
         pref = !pref;
         songList.clear();
         getSongList();
         Collections.sort(songList, new Comparator<Song>(){
             public int compare(Song a, Song b){
                 return a.getTitle().compareTo(b.getTitle());
             }
         });
         songAdt.notifyDataSetChanged();
         musicSrv.setList(songList);
     }

    private void setController(){
        controller = new MusicController(this);

        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }

    public  void play(){
        if (playbackPaused) {
            seekTo(currentPos*1000);
            musicSrv.playSong(playbackPaused);
            setText();
            System.err.println("                        " + currentPos);
            playBtn.setImageResource(R.drawable.pause);
            playbackPaused = false;
        } else {
            pause();
            playBtn.setImageResource(R.drawable.play);
        }
    }

    //play next
    private void playNext(){
        currentPos = 0;
        musicSrv.playNext(playbackPaused);
        setText();
        setTimeBar();
        playBtn.setImageResource(R.drawable.pause);
        if (playbackPaused)
            playbackPaused = false;
    }

     private void skipb(){
         currentPos =0;
         musicSrv.skipb();
         setText();
         setTimeBar();
     }


     //play previous
    private void playPrev(){
        currentPos =0;
        musicSrv.playPrev(playbackPaused);
        setText();
        setTimeBar();
        playBtn.setImageResource(R.drawable.pause);
        if (playbackPaused)
            playbackPaused = false;
    }

     private void skipf(){
         currentPos =0;
         musicSrv.skipf();
         setText();
         setTimeBar();
     }

    private void setShuffle(){
        musicSrv.setShuffle();
        if (shuffle)
            shuffleBtn.setImageResource(R.drawable.shuffle_not);
        else
            shuffleBtn.setImageResource(R.drawable.shuffle);
        shuffle = !shuffle;
    }

    private void setLooping(){
        musicSrv.setLooping();
        if (loop)
            repeatBtn.setImageResource(R.drawable.repeat_not);
        else
            repeatBtn.setImageResource(R.drawable.repeat);
        loop = !loop;
    }

    public void getSongList() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        int i = 0;


        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumColum = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ALBUM);
            int lengthColum = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.DURATION);
            int yearColum =  musicCursor.getColumnIndex
            (android.provider.MediaStore.Audio.Media.YEAR);
            //add songs to list
            do{
                Random rand = new Random();
                long thisId = musicCursor.getLong(idColumn);
                int length = musicCursor.getInt(lengthColum);
                int year = musicCursor.getInt(yearColum);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColum);
                String genre = genreList.get(rand.nextInt(genreList.size()));

                if (!selectedItem.equals("")){
                    if (genre.equals(genreList.get(Integer.parseInt(selectedItem)))){
                        songList.add(new Song(thisId, thisTitle, thisArtist, thisAlbum, length, genre, year));
                        i++;
                    }
                }
                else{
                    songList.add(new Song(thisId, thisTitle, thisArtist, thisAlbum, length, genre, year));
                    i++;
                }
            }
            while (musicCursor.moveToNext() && i < 25);
        }
    }


    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public void pause() {
        playbackPaused=true;
        currentPos = getCurrentPosition();
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
        return musicSrv.getDur();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
        return musicSrv.getPosn();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound)
        return musicSrv.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }


     @Override
     public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
         // On selecting a spinner item
         String item = parent.getItemAtPosition(position).toString();

         selectedItem =item;
     }

     public void onNothingSelected(AdapterView<?> arg0) {
         //**//
     }


     public void setText(){
        titelView.setText(songList.get(musicSrv.getSongPosn()).getTitle());
        interpretView.setText(songList.get(musicSrv.getSongPosn()).getArtist());
        albumView.setText(songList.get(musicSrv.getSongPosn()).getAlbum());
        maxTimeView.setText((String.format("%02d : %02d ",
                TimeUnit.MILLISECONDS.toMinutes(songList.get(musicSrv.getSongPosn()).getLength()),
                TimeUnit.MILLISECONDS.toSeconds(songList.get(musicSrv.getSongPosn()).getLength()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songList.get(musicSrv.getSongPosn()).getLength()))
        )));
        minTimeView.setText((String.format("%02d : %02d ",
             TimeUnit.MILLISECONDS.toMinutes(getCurrentPosition()),
             TimeUnit.MILLISECONDS.toSeconds(getCurrentPosition()) -
                     TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrentPosition()))
        )));

    }

    public void setTimeBar(){
        timeBar.setMax(songList.get(musicSrv.getSongPosn()).getLength()/1000);
        timeBar.setProgress(currentPos/1000);
    }

}
