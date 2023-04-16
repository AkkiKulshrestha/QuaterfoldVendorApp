package com.quaterfoldvendorapp.utils;

import com.quaterfoldvendorapp.data.AssignmentImageRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Convertor {

    public static ArrayList<AssignmentImageRequest> convertJsonToArray(final String images) {
        final ArrayList<AssignmentImageRequest> arrayList = new ArrayList<>();
        try {
            final JSONArray jsonArray = new JSONArray(images);
            for (int k = 0; k < jsonArray.length(); k++) {
                final JSONObject jsonObject = jsonArray.getJSONObject(k);
                final AssignmentImageRequest imageObj = new AssignmentImageRequest();
                imageObj.setProject_id(jsonObject.getString("project_id"));
                imageObj.setCustomer_id(jsonObject.getString("customer_id"));
                imageObj.setAssignment_id(jsonObject.getString("assignment_id"));
                imageObj.setAssignment_code(jsonObject.getString("assignment_code"));
                imageObj.setWall_id(jsonObject.getString("wall_id"));
                imageObj.setLat(jsonObject.getDouble("lat"));
                imageObj.setLong(jsonObject.getDouble("long"));
                imageObj.setSq_ft_covered(jsonObject.getString("sq_ft_covered"));
                imageObj.setImei(jsonObject.getString("imei"));
                imageObj.setImage_bitmap(jsonObject.getString("image_bitmap"));
                arrayList.add(imageObj);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return arrayList;
    }
}
