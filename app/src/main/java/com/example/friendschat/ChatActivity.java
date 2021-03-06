package com.example.friendschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.services.language.v1.CloudNaturalLanguage;
import com.google.api.services.language.v1.CloudNaturalLanguageRequestInitializer;
import com.google.api.services.language.v1.model.AnnotateTextRequest;
import com.google.api.services.language.v1.model.AnnotateTextResponse;
import com.google.api.services.language.v1.model.Document;
import com.google.api.services.language.v1.model.Features;
import com.google.api.services.language.v1.model.Sentiment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity
{
    //FOR SENTIMENT CALCULATION
    public static final String API_KEY = "AIzaSyAIPC5LPPwK_f99ytvvki_YhLWH_HbYfxU";
    private CloudNaturalLanguage naturalLanguageService;
    private Document document;
    private Features features;
    //SENTIMENT COMPONENTS OVER

    private String receiveUID, receiveUNAME, receiveUIMAGE, senderUID;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private TextView username, lastseen;
    private CircleImageView userimage;
    private ImageButton psendmessagebutton, psendfilebutton;
    private EditText pedittext;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessageList;
    private String saveCurrentTime, saveCurrentDate;
    private String checker = "", myUrl="";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth=FirebaseAuth.getInstance();
        senderUID=mAuth.getCurrentUser().getUid();
        rootRef= FirebaseDatabase.getInstance().getReference();

        receiveUID = getIntent().getExtras().get("Visit_UID").toString();
        receiveUNAME=getIntent().getExtras().get("Visit_UNAME").toString();
        receiveUIMAGE=getIntent().getExtras().get("Visit_UIMAGE").toString();

        initializeControllers();

        username.setText(receiveUNAME);
        Picasso.get().load(receiveUIMAGE).placeholder(R.drawable.profile_image).into(userimage);
        DisplayLastSeen();



        //ANALYZE SENTIMENT

        naturalLanguageService = new CloudNaturalLanguage.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(),
                null
        ).setCloudNaturalLanguageRequestInitializer(
                new CloudNaturalLanguageRequestInitializer(API_KEY)
        ).build();


        document = new Document();
        document.setType("PLAIN_TEXT");
        document.setLanguage("en-US");

        features = new Features();
        features.setExtractEntities(true);
        features.setExtractSyntax(true);
        features.setExtractDocumentSentiment(true);

        final AnnotateTextRequest request = new AnnotateTextRequest();
        request.setDocument(document);
        request.setFeatures(features);




        //ANALYZE SENTIMENT PART OVER



        psendmessagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });
        psendfilebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "PDF",
                                "Docx"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select File");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        if (i==0)
                        {
                            checker="image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select Image"),123);

                        }
                        else if (i==1)
                        {
                            checker="pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent,"Select PDF File"),123);
                        }
                        else if (i==2)
                        {
                            checker="docx";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent,"Select DOCX File"),123);
                        }

                    }
                });
                builder.show();
            }
        });
    }

    private void initializeControllers()
    {
        mToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userimage = findViewById(R.id.custom_profile_pic);
        username = findViewById(R.id.custom_profile_name);
        lastseen = findViewById(R.id.custom_profile_lastseen);
        psendmessagebutton = findViewById(R.id.send_msg_btn);
        pedittext = findViewById(R.id.input_msg);
        psendfilebutton = findViewById(R.id.attach_file_btn);
        progressDialog= new ProgressDialog(this);
        messageAdapter = new MessageAdapter(messagesList);
        userMessageList = findViewById(R.id.private_message_list_of_user);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messageAdapter);


        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentdate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentdate.format(calendar.getTime());

        SimpleDateFormat currenttime = new SimpleDateFormat("K:mm");
        saveCurrentTime = currenttime.format(calendar.getTime());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==123 && resultCode==RESULT_OK && data!=null && data.getData()!=null )
        {
            progressDialog.setTitle("Sending File");
            progressDialog.setMessage("Please wait while we send your file.");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            fileUri = data.getData();
            if (!checker.equals("image"))
            {

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");
                final String messageSenderref = "Messages/"+senderUID+"/"+receiveUID;
                final String messageReceiverref = "Messages/"+receiveUID+"/"+senderUID;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                        .child(senderUID).child(receiveUID).push();

                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filepath = storageReference.child(messagePushID+"."+checker);
                filepath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            Map messageTextBody = new HashMap<>();

                            messageTextBody.put("Message",task.getResult().getMetadata().getReference().getDownloadUrl().toString());
                            messageTextBody.put("Name",fileUri.getLastPathSegment());
                            messageTextBody.put("Type",checker);
                            messageTextBody.put("From",senderUID);
                            messageTextBody.put("To",receiveUID);
                            messageTextBody.put("MessageID",messagePushID);
                            messageTextBody.put("Time",saveCurrentTime);
                            messageTextBody.put("Date",saveCurrentDate);

                            Map messageBodyDetail = new HashMap<>();
                            messageBodyDetail.put(messageSenderref+"/"+messagePushID,messageTextBody);
                            messageBodyDetail.put(messageReceiverref+"/"+messagePushID,messageTextBody);

                            rootRef.updateChildren(messageBodyDetail);
                            progressDialog.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        progressDialog.dismiss();
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        double p = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        progressDialog.setMessage((int)p + "% Uploading...");
                    }
                });

            }
            else if (checker.equals("image"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                final String messageSenderref = "Messages/"+senderUID+"/"+receiveUID;
                final String messageReceiverref = "Messages/"+receiveUID+"/"+senderUID;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                        .child(senderUID).child(receiveUID).push();

                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filepath = storageReference.child(messagePushID+"."+"jpg");

                uploadTask= filepath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filepath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful())
                        {
                            Uri downloadUrl = task.getResult();
                            myUrl= downloadUrl.toString();

                            Map messageTextBody = new HashMap<>();

                            messageTextBody.put("Message",myUrl);
                            messageTextBody.put("Name",fileUri.getLastPathSegment());
                            messageTextBody.put("Type",checker);
                            messageTextBody.put("From",senderUID);
                            messageTextBody.put("To",receiveUID);
                            messageTextBody.put("MessageID",messagePushID);
                            messageTextBody.put("Time",saveCurrentTime);
                            messageTextBody.put("Date",saveCurrentDate);

                            Map messageBodyDetail = new HashMap<>();
                            messageBodyDetail.put(messageSenderref+"/"+messagePushID,messageTextBody);
                            messageBodyDetail.put(messageReceiverref+"/"+messagePushID,messageTextBody);

                            rootRef.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful())
                                    {
                                        progressDialog.dismiss();
                                        Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        progressDialog.dismiss();
                                        Toast.makeText(ChatActivity.this, "Some Error Occured...", Toast.LENGTH_SHORT).show();
                                    }
                                    pedittext.setText("");
                                }
                            });
                        }
                    }
                });
            }
            else
            {
                progressDialog.dismiss();
                Toast.makeText(this, "Some Error Occured ! Kindly Retry", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void DisplayLastSeen()
    {
        rootRef.child("Users").child(receiveUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.child("userState").hasChild("state"))
                        {
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("Online"))
                            {
                                lastseen.setText("Online");
                            }
                            else if (state.equals("Offline"))
                            {
                                lastseen.setText("Last Seen: "+date+" "+time);
                            }
                        }
                        else
                        {
                            lastseen.setText("Offline");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        rootRef.child("Messages").child(senderUID).child(receiveUID)
        .addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                Messages messages =dataSnapshot.getValue(Messages.class);

                messagesList.add(messages);

                messageAdapter.notifyDataSetChanged();

                userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendMessage()
    {
       String messagetext = pedittext.getText().toString();
       if (TextUtils.isEmpty(messagetext))
       {
           Toast.makeText(this, "Write your Message First...", Toast.LENGTH_SHORT).show();
       }
       else
       {
           String messageSenderref = "Messages/"+senderUID+"/"+receiveUID;
           String messageReceiverref = "Messages/"+receiveUID+"/"+senderUID;


           DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                   .child(senderUID).child(receiveUID).push();

           String messagePushID = userMessageKeyRef.getKey();


           //SENTIMENT ANALYSIS PART

               document.setContent(messagetext);

               final AnnotateTextRequest request = new AnnotateTextRequest();
               request.setDocument(document);
               request.setFeatures(features);

               new AsyncTask<Object, Void, AnnotateTextResponse>() {
                   @Override
                   protected AnnotateTextResponse doInBackground(Object... params) {
                       AnnotateTextResponse response = null;
                       try {
                           response = naturalLanguageService.documents().annotateText(request).execute();

                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                       return response;
                   }

                   @Override
                   protected void onPostExecute(AnnotateTextResponse response) {
                       super.onPostExecute(response);
                       if (response != null) {
                           Sentiment sent = response.getDocumentSentiment();
                           String sentimentScore = sent.getScore().toString();
                           //entityList.addAll(response.getEntities());
                           //entityListAdapter.notifyDataSetChanged();
                           //sentiment.setText("Score : " + sent.getScore() + " Magnitude : " + sent.getMagnitude());
                           Toast.makeText(ChatActivity.this, "Score : " + sentimentScore , Toast.LENGTH_SHORT).show();
                           //Log.i("SENTIMENT SCORE", sent.getScore().toString() );
                       }
                   }
               }.execute();

           //SENTIMENT ANALYSIS OVER




           Map messageTextBody = new HashMap<>();

           messageTextBody.put("Message",messagetext);
           messageTextBody.put("Type","text");
           messageTextBody.put("From",senderUID);
           messageTextBody.put("To",receiveUID);
           messageTextBody.put("MessageID",messagePushID);
           messageTextBody.put("Time",saveCurrentTime);
           messageTextBody.put("Date",saveCurrentDate);

           Map messageBodyDetail = new HashMap<>();
           messageBodyDetail.put(messageSenderref+"/"+messagePushID,messageTextBody);
           messageBodyDetail.put(messageReceiverref+"/"+messagePushID,messageTextBody);

           rootRef.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener() {
               @Override
               public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "Some Error Occured...", Toast.LENGTH_SHORT).show();
                    }
                    pedittext.setText("");
               }
           });


       }
    }
}
