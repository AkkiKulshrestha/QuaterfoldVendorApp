package com.quaterfoldvendorapp.utils

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.quaterfoldvendorapp.interfaces.UploadImagesCallback
import org.json.JSONException
import org.json.JSONObject

object VendorServices {

    fun uploadImages(
        context: Context?,
        imageArray: String,
        callback: UploadImagesCallback
    ) {
        val request: StringRequest = object : StringRequest(
            Method.POST, SharedPrefConstant.UPLOAD_IMAGE_URL,
            Response.Listener { response ->
                try {
                    Log.d("response", response!!)
                    val jObject = JSONObject(response)
                    val status = jObject.getString("status")
                    if ("valid".equals(status, ignoreCase = true)) {
                        callback.onSuccess()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    callback.onFailure(null)
                }
            },
            Response.ErrorListener { volleyError ->
                volleyError.printStackTrace()
                callback.onFailure("Some error occurred. Please try again later.")
            }) {
            override fun getParams(): MutableMap<String, String> {
                val map: MutableMap<String, String> = HashMap()
                map["images"] = imageArray

                Log.d("", ""+map)
                return map
            }
        }

        request.retryPolicy =
            DefaultRetryPolicy(20 * 1000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        val rQueue = Volley.newRequestQueue(context)
        rQueue.add(request)
    }

}