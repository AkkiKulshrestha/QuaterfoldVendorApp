package com.quaterfoldvendorapp.data

import com.google.gson.annotations.SerializedName

data class JobModel(
    @SerializedName("project_id") var projectId: String = ""
)