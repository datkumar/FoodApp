package com.phoenix.tech.foodapp.data.sample_data

import com.phoenix.tech.foodapp.data.model.LoginResponse
import retrofit2.Response

val SampleLoginSuccessResponse: Response<LoginResponse> = Response.success(
    LoginResponse(
        statusCode = 200,
        statusMessage = "Success",
        data = SampleUserData
    )
)