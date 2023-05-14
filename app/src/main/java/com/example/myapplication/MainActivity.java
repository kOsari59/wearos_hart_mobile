package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity  implements MessageClient.OnMessageReceivedListener,DataClient.OnDataChangedListener, CapabilityClient.OnCapabilityChangedListener{
    TextView tv;
    private String transcriptionNodeId = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.textView);
        Button bt = (Button) findViewById(R.id.button);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("테스트", "버튼이 눌림");
                
                //메시지 전송
                try{
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                getnode();
                            } catch (Exception e) {
                                Log.d("테스트 이것은 세팅 에러", e.toString());
                            }
                        }
                    }).start();
                    Log.d("테스트", "이건 성공");
                    Log.d("테스트", transcriptionNodeId.toString());
                    requestTranscription("안녕".getBytes(StandardCharsets.UTF_8));
                }catch (Exception e){
                    Log.d("테스트 이것은 스레드 에러", e.toString());
                }

            }
        });
    }

    private static final String
            VOICE_TRANSCRIPTION_CAPABILITY_NAME = "/message-item-received";



    @Override
    public void onResume() {
        super.onResume();
        Wearable.getDataClient(this).addListener(this);
        Wearable.getMessageClient(this).addListener(this);
        Wearable.getCapabilityClient(this)
                .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.getDataClient(this).removeListener(this);
        Wearable.getMessageClient(this).removeListener(this);
        Wearable.getCapabilityClient(this).removeListener(this);
    }


    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Log.d("테스트","메시지 체인지" + messageEvent.toString());
        tv.setText(new String(messageEvent.getData(), StandardCharsets.UTF_8));
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        Log.d("테스트","데이터 체인지" + dataEventBuffer.toString());
    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        Log.d("테스트","케파블리티 체인지" + capabilityInfo.toString());

    }

    //통신용 코드

    //메시지 전달 세팅


    private void getnode() throws ExecutionException, InterruptedException {
        List<Node> connectedNodes = Tasks.await(Wearable.getNodeClient(this).getConnectedNodes());

        transcriptionNodeId = pickBestNodeId(connectedNodes);
        if(transcriptionNodeId==null){
            Log.d("테스트","노드 못찾음");
        }
    }

    private String pickBestNodeId(List<Node> nodes) {
        String bestNodeId = null;
        // Find a nearby node or pick one arbitrarily.
        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }

    public static final String VOICE_TRANSCRIPTION_MESSAGE_PATH = "/message-item-received";

    //메시지 전송
    private void requestTranscription(byte[] voiceData) {
        if (transcriptionNodeId != null) {
            Task<Integer> sendTask =
                    Wearable.getMessageClient(getApplicationContext()).sendMessage(
                            transcriptionNodeId, VOICE_TRANSCRIPTION_MESSAGE_PATH, voiceData);
            // You can add success and/or failure listeners,

            // Or you can call Tasks.await() and catch ExecutionException
            Log.d("테스트", "requestTranscription:"+voiceData);

        } else {
            Log.d("테스트____", "오류");
            // Unable to retrieve node with transcription capability
        }
    }
    
    
}

