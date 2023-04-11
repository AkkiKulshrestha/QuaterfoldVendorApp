package com.quaterfoldvendorapp.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Assignment(
    @SerializedName("id") var id: String = "",
    @SerializedName("customer_code_id") var customer_code_id: String = "",
    @SerializedName("project_code_id") var project_code_id: String = "",
    @SerializedName("project_status_id") var project_status_id: String = "",
    @SerializedName("assignment_code") var assignment_code: String = "",
    @SerializedName("state") var state: String = "",
    @SerializedName("district") var district: String = "",
    @SerializedName("sub_district") var sub_district: String = "",
    @SerializedName("town") var town: String = "",
    @SerializedName("village") var village: String = "",
    @SerializedName("brand") var brand: String = "",
    @SerializedName("no_of_walls") var no_of_walls: Int = 0,
    @SerializedName("wall_size") var wall_size: String = "",
    @SerializedName("total_sq_feet") var total_sq_feet: String = "",
    @SerializedName("work_type") var work_type: String = "",
    @SerializedName("wall_covered") var wall_covered: Int = 0,
    @SerializedName("sq_ft_covered") var sq_ft_covered: Int = 0
) : Parcelable
