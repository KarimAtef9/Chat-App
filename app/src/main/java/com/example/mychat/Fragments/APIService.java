package com.example.mychat.Fragments;

import com.example.mychat.Notifications.MyResponse;
import com.example.mychat.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authentication:key=AAAAqlKtQ08:APA91bFKkCveOAVy1-Kwi68ew06BCuIxNZ9UFJFo6M3n2dSqMkmRzfmOpKsBp6E3m-da1PstMP8aQn7FLx-Iive6tvUqmd9ctS2i9-T_zQPAdaudUeORrhvefmNH7Zxs7xdS4zMT8E-F"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification (@Body Sender body);


}
