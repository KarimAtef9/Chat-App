package com.example.mychat.Notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.mychat.MessageActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        if (firebaseUser != null) {
            updateToken(refreshToken);
        }
    }

    private void updateToken(String refreshToken) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(refreshToken);
        reference.child(firebaseUser.getUid()).setValue(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String sent = remoteMessage.getData().get("sent");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && sent.equals(firebaseUser.getUid())) {
            sendNotification(remoteMessage);
        }

    }

    private void sendNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String body = remoteMessage.getData().get("body");
        String title = remoteMessage.getData().get("title");
        String icon = remoteMessage.getData().get("icon");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userId", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Flag indicating that this PendingIntent can be used only once (flag one shot)
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // building notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i = ((j>0) ? j:0);

        notificationManager.notify(i, builder.build());

    }
}
