package com.example.yelimhan.dangshin;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

public class AnswerActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private QuestionInfo questionInfo;
    private ImageView imageView;
    private TextView questionText;
    private EditText editText;
    private StorageReference storageReference = FirebaseStorage.getInstance("gs://dangshin-fa136.appspot.com").getReference();
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private boolean isPlaying = false;
    private boolean isPrepared = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);
        Intent intent = getIntent();
        questionInfo = (QuestionInfo)intent.getSerializableExtra("question");
        imageView = findViewById(R.id.answerimage);
        GlideApp.with(this)
                .load(storageReference.child(questionInfo.q_pic))
                .override(500,300)
                .into(imageView);
        questionText = findViewById(R.id.questiontext);

        // 음성 파일 재생
        Task<Uri> uri = storageReference.child(questionInfo.q_voice).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    mediaPlayer.setDataSource(getApplicationContext(), uri);
                    mediaPlayer.prepare();
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            isPrepared = true;
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        final Button playbutton = findViewById(R.id.playbutton);
        playbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPrepared) {
                    if (isPlaying) {
                        mediaPlayer.pause();
                        isPlaying = false;
                        playbutton.setText("재생");
                    } else {
                        mediaPlayer.start();
                        isPlaying = true;
                        playbutton.setText("일시정지");
                    }
                }
            }
        });
        findViewById(R.id.stopbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPrepared = false;
                mediaPlayer.stop();
                isPlaying = false;
                playbutton.setText("재생");
                try {
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // STT
        questionText.setText("음성녹음 STT한 질문글");

        // 확인버튼 누르면 답변 디비에 저장
        editText = findViewById(R.id.answertext);
        findViewById(R.id.checkbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!editText.getText().toString().equals("")) {
                    mDatabase = FirebaseDatabase.getInstance().getReference("AnswerInfo");
                    String newAnswer = mDatabase.push().getKey();
                    AnswerInfo answerInfo = new AnswerInfo(newAnswer,questionInfo.q_id,"a",editText.getText().toString());
                    mDatabase.child(newAnswer).setValue(answerInfo);
                    mDatabase = FirebaseDatabase.getInstance().getReference("QuestionInfo");
                    mDatabase.child(questionInfo.q_id).child("checkAnswer").setValue(true);
                    //startActivity(new Intent(AnswerActivity.this,QuestionListActivity.class));

                    AnswerActivity.this.finish();
                }
            }
        });
    }

    public void stt() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        SpeechRecognizer mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {
                String stt = SpeechRecognizer.RESULTS_RECOGNITION;
                ArrayList<String> mResult = results.getStringArrayList(stt);
                String[] rs = new String[mResult.size()];
                mResult.toArray(rs);
                questionText.setText(""+rs[0]);
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });
        mRecognizer.startListening(i);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}