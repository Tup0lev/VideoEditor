package com.raghav.gfgffmpeg;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.florescu.android.rangeseekbar.RangeSeekBar;
import org.w3c.dom.ls.LSOutput;

import java.io.File;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS= 7;
    private ImageButton reverse,slow,fast;
    private Button cancel;

    private ProgressDialog progressDialog;
    private String video_url;
    private VideoView videoView;
    private Runnable r;

    private static final String root= Environment.getExternalStorageDirectory().toString();
    private static final String app_folder=root+"/Movies";

    @Override

    protected void onCreate(Bundle savedInstanceState) {


//        RxPermissions rxPermissions = new RxPermissions(this);
//        rxPermissions
//                .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                // ask single or multiple permission once
//                .subscribe(granted -> {
//                    if (granted) {
//                        // All requested permissions are granted
//                    } else {
//                        Toast.makeText(MainActivity.this, "请允许储存空间权限！！！", Toast.LENGTH_SHORT).show();
//                    }
//                });







        setTitle("审核快乐App By图波列夫的设计图");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        slow = (ImageButton) findViewById(R.id.slow);
        reverse = (ImageButton) findViewById(R.id.reverse);
        fast = (ImageButton) findViewById(R.id.fast);
        cancel = (Button) findViewById(R.id.cancel_button);
        fast = (ImageButton) findViewById(R.id.fast);
        videoView=(VideoView) findViewById(R.id.layout_movie_wrapper);

        //creating the progress dialog
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("请耐心等待");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        //set up the onClickListeners
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create an intent to retrieve the video file from the device storage
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                intent.setType("video/*");
                startActivityForResult(intent, 123);
            }
        });

        slow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                    check if the user has selected any video or not
                    In case a user hasen't selected any video and press the button,
                    we will show an warning, stating "Please upload the video"
                 */
                if (video_url != null) {
                    //a try-catch block to handle all necessary exceptions like File not found, IOException
                    try {
                        slowmotion(1,1);
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else
                    Toast.makeText(MainActivity.this, "你没选择视频！", Toast.LENGTH_SHORT).show();
            }
        });



        fast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (video_url != null) {

                    try {
                        fastforward(0,0);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                } else
                    Toast.makeText(MainActivity.this, "你没选择视频！", Toast.LENGTH_SHORT).show();
            }
        });



        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (video_url != null) {
                    try {
                        reverse(0,0);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                } else
                    Toast.makeText(MainActivity.this, "你没选择视频！", Toast.LENGTH_SHORT).show();
            }
        });

        /*
            set up the VideoView.
            We will be using VideoView to view our video.
         */
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                //get the durtion of the video
                int duration = mp.getDuration() / 1000;
                //initially set the left TextView to "00:00:00"

                //initially set the right Text-View to the video length
                //the getTime() method returns a formatted string in hh:mm:ss

                //this will run he ideo in loop i.e. the video won't stop
                //when it reaches its duration
                mp.setLooping(true);

                //set up the initial values of rangeSeekbar




                //this method changes the right TextView every 1 second as the video is being played
                //It works same as a time counter we see in any Video Player
                final Handler handler = new Handler();
                handler.postDelayed(r = new Runnable() {
                    @Override
                    public void run() {


                    }
                }, 1000);

            }
        });
    }



    /**
     * Method for creating fast motion video
     */
    private void fastforward(int startMs, int endMs) throws Exception {

        progressDialog.show();

        final String filePath;
        String filePrefix = "23hr";
        String fileExtn = ".mp4";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            ContentValues valuesvideos = new ContentValues();
            valuesvideos.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/" );
            valuesvideos.put(MediaStore.Video.Media.TITLE, filePrefix+System.currentTimeMillis());
            valuesvideos.put(MediaStore.Video.Media.DISPLAY_NAME, filePrefix+System.currentTimeMillis()+fileExtn);
            valuesvideos.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            valuesvideos.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
            valuesvideos.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
            Uri uri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, valuesvideos);
            File file=FileUtils.getFileFromUri(this,uri);
            filePath=file.getAbsolutePath();
            System.out.println(filePath);

        }else {

            File dest = new File(new File(app_folder), filePrefix + fileExtn);
            int fileNo = 0;
            while (dest.exists()) {
                fileNo++;
                dest = new File(new File(app_folder), filePrefix + fileNo + fileExtn);
            }
            filePath = dest.getAbsolutePath();
            System.out.println(filePath);
        }

        String exe;
        String exe2;

        String vf;
        vf = "scale=256:144 ";
        exe=" -i " +video_url+" -crf 20 -preset ultrafast -pix_fmt yuv420p -crf 24 -vf scale=256:144" + "/storage/emulated/0/Movies/cache.mkv";

        exe2=" -stream_loop -1 -i " + "/storage/emulated/0/Movies/cache.mkv" +" -c copy -t 82800 "+filePath;


        File file = new File("/storage/emulated/0/Movies/cache.mkv");
        file.delete();

        long executionId = FFmpeg.executeAsync(exe, new ExecuteCallback() {

            @Override
            public void apply(final long executionId, final int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {



                } else if (returnCode == RETURN_CODE_CANCEL) {
                    Log.i(Config.TAG, "Async command execution cancelled by user.");
                } else {
                    Log.i(Config.TAG, String.format("Async command execution failed with returnCode=%d.", returnCode));
                }
            }
        });

        sleep(10000);

        System.out.println("asdf");
        Toast.makeText(MainActivity.this, "asdfghjkl;'", Toast.LENGTH_SHORT).show();

        long executionId2 = FFmpeg.executeAsync(exe2, new ExecuteCallback() {

            @Override
            public void apply(final long executionId2, final int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {

                    videoView.setVideoURI(Uri.parse(filePath));
                    video_url = filePath;
                    videoView.start();


                    File file = new File("/storage/emulated/0/Movies/cache.mkv");
                    file.delete();

                    progressDialog.dismiss();

                } else if (returnCode == RETURN_CODE_CANCEL) {
                    Log.i(Config.TAG, "Async command execution cancelled by user.");
                } else {
                    Log.i(Config.TAG, String.format("Async command execution failed with returnCode=%d.", returnCode));
                }
            }
        });
    }



    private void reverse(int startMs, int endMs) throws Exception {

        progressDialog.show();

        final String filePath;
        String filePrefix = "10hr";
        String fileExtn = ".mp4";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            ContentValues valuesvideos = new ContentValues();
            valuesvideos.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/" );
            valuesvideos.put(MediaStore.Video.Media.TITLE, filePrefix+System.currentTimeMillis());
            valuesvideos.put(MediaStore.Video.Media.DISPLAY_NAME, filePrefix+System.currentTimeMillis()+fileExtn);
            valuesvideos.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            valuesvideos.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
            valuesvideos.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
            Uri uri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, valuesvideos);
            File file=FileUtils.getFileFromUri(this,uri);
            filePath=file.getAbsolutePath();
            System.out.println(filePath);

        }else {

            File dest = new File(new File(app_folder), filePrefix + fileExtn);
            int fileNo = 0;
            while (dest.exists()) {
                fileNo++;
                dest = new File(new File(app_folder), filePrefix + fileNo + fileExtn);
            }
            filePath = dest.getAbsolutePath();
            System.out.println(filePath);
        }

        String exe;
        String exe2;

        String vf;
        vf = "scale=480:360";
        exe=" -i " +video_url+" -crf 20 -preset ultrafast -vf "+ vf + " /storage/emulated/0/Movies/cache.mkv";


        exe2=" -stream_loop -1 -i " + "/storage/emulated/0/Movies/cache.mkv" +" -c copy -t 35999 "+filePath;


        File file = new File("/storage/emulated/0/Movies/cache.mkv");
        file.delete();

        long executionId = FFmpeg.executeAsync(exe, new ExecuteCallback() {

            @Override
            public void apply(final long executionId, final int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {



                } else if (returnCode == RETURN_CODE_CANCEL) {
                    Log.i(Config.TAG, "Async command execution cancelled by user.");
                } else {
                    Log.i(Config.TAG, String.format("Async command execution failed with returnCode=%d.", returnCode));
                }
            }
        });


        sleep(10000);

        long executionId2 = FFmpeg.executeAsync(exe2, new ExecuteCallback() {

            @Override
            public void apply(final long executionId2, final int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {

                    videoView.setVideoURI(Uri.parse(filePath));
                    video_url = filePath;
                    videoView.start();


                    File file = new File("/storage/emulated/0/Movies/cache.mkv");
                    file.delete();

                    progressDialog.dismiss();

                } else if (returnCode == RETURN_CODE_CANCEL) {
                    Log.i(Config.TAG, "Async command execution cancelled by user.");
                } else {
                    Log.i(Config.TAG, String.format("Async command execution failed with returnCode=%d.", returnCode));
                }
            }
        });



    }

    /**
      Method for creating slow motion video for specific part of the video
      The below code is same as above only the command in string "exe" is changed.
    */
    private void slowmotion(int startMs, int endMs) throws Exception {

        progressDialog.show();

        final String filePath;
        String filePrefix = "1hr";
        String fileExtn = ".mp4";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            ContentValues valuesvideos = new ContentValues();
            valuesvideos.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/" );
            valuesvideos.put(MediaStore.Video.Media.TITLE, filePrefix+System.currentTimeMillis());
            valuesvideos.put(MediaStore.Video.Media.DISPLAY_NAME, filePrefix+System.currentTimeMillis()+fileExtn);
            valuesvideos.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            valuesvideos.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
            valuesvideos.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
            Uri uri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, valuesvideos);
            File file=FileUtils.getFileFromUri(this,uri);
            filePath=file.getAbsolutePath();
            System.out.println(filePath);

        }else {

            File dest = new File(new File(app_folder), filePrefix + fileExtn);
            int fileNo = 0;
            while (dest.exists()) {
                fileNo++;
                dest = new File(new File(app_folder), filePrefix + fileNo + fileExtn);
            }
            filePath = dest.getAbsolutePath();
            System.out.println(filePath);
        }
        System.out.println("delete");
        File file = new File("/storage/emulated/0/Movies/cache.mkv");
        file.delete();

        String exe;
        String exe2;

        String vf;
        vf = "scale=640:360";
        exe=" -i " +video_url+" -crf 20 -preset ultrafast -vf "+ vf + " /storage/emulated/0/Movies/cache.mkv";

        exe2=" -stream_loop -1 -i " + "/storage/emulated/0/Movies/cache.mkv" +" -c copy -t 3600 " + filePath;




        long executionId = FFmpeg.executeAsync(exe, new ExecuteCallback() {

            @Override
            public void apply(final long executionId, final int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {



                } else if (returnCode == RETURN_CODE_CANCEL) {
                    Log.i(Config.TAG, "Async command execution cancelled by user.");
                } else {
                    Log.i(Config.TAG, String.format("Async command execution failed with returnCode=%d.", returnCode));
                }
            }
        });

        System.out.println("asdfasdf");

        sleep(10000);

        long executionId2 = FFmpeg.executeAsync(exe2, new ExecuteCallback() {

            @Override
            public void apply(final long executionId2, final int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {

                    videoView.setVideoURI(Uri.parse(filePath));
                    video_url = filePath;
                    videoView.start();


                    File file = new File("/storage/emulated/0/Movies/cache.mkv");
                    file.delete();

                    progressDialog.dismiss();

                } else if (returnCode == RETURN_CODE_CANCEL) {
                    Log.i(Config.TAG, "Async command execution cancelled by user.");
                } else {
                    Log.i(Config.TAG, String.format("Async command execution failed with returnCode=%d.", returnCode));
                }
            }
        });



    }











    //Overriding the method onActivityResult() to get the video Uri form intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == 123) {

                if (data != null) {
                    //get the video Uri
                    Uri uri = data.getData();
                    try {
                        //get the file from the Uri using getFileFromUri() methid present in FileUils.java
                        File video_file = FileUtils.getFileFromUri(this, uri);
                        //now set the video uri in the VideoView
                        videoView.setVideoURI(uri);
                        //after successful retrieval of the video and properly setting up the retried video uri in
                        //VideoView, Start the VideoView to play that video
                        videoView.start();
                        //get the absolute path of the video file. We will require this as an input argument in
                        //the ffmpeg command.
                        video_url=video_file.getAbsolutePath();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }


                }
            }
        }
    }

    //This method returns the seconds in hh:mm:ss time format
    private String getTime(int seconds) {
        int hr = seconds / 3600;
        int rem = seconds % 3600;
        int mn = rem / 60;
        int sec = rem % 60;
        return String.format("%02d", hr) + ":" + String.format("%02d", mn) + ":" + String.format("%02d", sec);
    }
}
