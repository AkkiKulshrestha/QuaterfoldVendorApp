package com.quaterfoldvendorapp

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.quaterfoldvendorapp.data.Agentinfo
import com.quaterfoldvendorapp.data.Assignment
import com.quaterfoldvendorapp.data.local.dao.WallDao
import com.quaterfoldvendorapp.databinding.FragmentAssignmentListBinding
import com.quaterfoldvendorapp.interfaces.UploadImagesCallback
import com.quaterfoldvendorapp.sharedpreference.SharedPrefManager
import com.quaterfoldvendorapp.ui.JobListAdapter
import com.quaterfoldvendorapp.utils.SharedPrefConstant
import com.quaterfoldvendorapp.utils.VendorServices
import org.json.JSONObject

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class AssignmentListFragment : Fragment(), JobListAdapter.Listener {

    private lateinit var binding: FragmentAssignmentListBinding

    // This property is only valid between onCreateView and
    // onDestroyView.
    private lateinit var jobListAdapter: JobListAdapter
    var assignmentList: ArrayList<Assignment> = ArrayList<Assignment>()
    var layoutManager: RecyclerView.LayoutManager? = null
    lateinit var assignment: Assignment
    var agentinfo: Agentinfo? = null
    var posId: String? = null
    var progressDialog: ProgressDialog? = null
    var pendingCallCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssignmentListBinding.inflate(inflater, container, false)
        initView()

        return binding.root
    }

    private fun initView() {
        //getting agent info
        agentinfo = SharedPrefManager.getInstance(context).agent
        posId = agentinfo?.id

        progressDialog = ProgressDialog(context)
        progressDialog?.setMessage("Please Wait")
        progressDialog?.setCancelable(false)

        binding.assignmentList.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context)
        binding.assignmentList.layoutManager = layoutManager

        jobListAdapter = JobListAdapter()
        binding.assignmentList.adapter = jobListAdapter

        binding.refreshLayout.setOnRefreshListener {
            Handler().postDelayed({
                assignmentList.clear()
                uploadPendingWallImages()
                binding.refreshLayout.isRefreshing = false
            }, 3000)
            binding.assignmentList.itemAnimator = DefaultItemAnimator()
        }

        uploadPendingWallImages()
    }

    private fun getAssignmentData() {
        val assignmentUrl = SharedPrefConstant.ASSIGNMENT_LIST_URL + "?vendor_id=" + posId
        Log.d("AssignmentListUrl", "" + assignmentUrl)
        val stringRequest = StringRequest(
            Request.Method.GET, assignmentUrl,
            { response ->
                try {
                    Log.d("assignmentListResponse", "" + response)
                    val responseObj = JSONObject(response)
                    val status = responseObj.getString("status")
                    if ("valid".equals(status, ignoreCase = true)) {
                        val dataArray = responseObj.getJSONArray("data")

                        val assignmentList = mutableListOf<Assignment>()
                        for (i in 0 until dataArray.length()) {
                            val dataObj: JSONObject =
                                dataArray.getJSONObject(i)
                            //Customer vehicle info
                            val id = dataObj.getString("id")
                            val customer_code_id = dataObj.getString("customer_code_id")
                            val project_code_id = dataObj.getString("project_code_id")
                            val assignment_code =
                                dataObj.getString("assignment_code")
                            val state = dataObj.getString("state")
                            val district = dataObj.getString("district")
                            val sub_district = dataObj.getString("sub_district")
                            val town = dataObj.getString("town")
                            val village = dataObj.getString("village")
                            val brand = dataObj.getString("brand")
                            val no_of_walls = dataObj.getInt("no_of_walls")
                            val wall_size = dataObj.getString("wall_size")
                            val total_sq_feet = dataObj.getString("total_sq_feet")
                            val work_type = dataObj.getString("work_type")
                            val wall_covered = dataObj.getInt("wall_covered")
                            val sq_ft_covered = dataObj.getInt("sq_ft_covered")

                            assignment = Assignment()
                            assignment.id = id
                            assignment.customer_code_id = customer_code_id
                            assignment.project_code_id = project_code_id
                            assignment.assignment_code = assignment_code
                            assignment.state = state
                            assignment.district = district
                            assignment.sub_district = sub_district
                            assignment.town = town
                            assignment.village = village
                            assignment.brand = brand
                            assignment.no_of_walls = no_of_walls
                            assignment.wall_size = wall_size
                            assignment.total_sq_feet = total_sq_feet
                            assignment.work_type = work_type
                            assignment.wall_covered = wall_covered
                            assignment.sq_ft_covered = sq_ft_covered

                            assignmentList.add(assignment)
                        }
                        jobListAdapter.updateData(assignmentList)
                        jobListAdapter.setListener(this)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        ) { error ->
            error.printStackTrace()
            Toast.makeText(
                requireActivity(),
                "App under maintenance. Please try again later.",
                Toast.LENGTH_LONG
            ).show()
        }

        stringRequest.retryPolicy =
            DefaultRetryPolicy(20 * 1000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(stringRequest)
    }

    private fun uploadPendingWallImages() {
        val pendingWallImages = WallDao.getAll()
        if (pendingWallImages.isNullOrEmpty()) {
            getAssignmentData()
            return
        }
        pendingCallCount = pendingWallImages.size
        pendingWallImages.forEach {
            VendorServices.uploadImages(context,
                it.imageArray,
                object : UploadImagesCallback {
                    override fun onSuccess() {
                        WallDao.deleteWallImages(it._id)
                        handlePendingCallResponse()
                    }

                    override fun onFailure(msg: String?) {
                        handlePendingCallResponse()
                    }
                })
        }
    }

    override fun onClickListener(assignment: Assignment) {
        if (assignment.no_of_walls > assignment.wall_covered) {
            val bundle = Bundle()
            bundle.putParcelable("assignment", assignment)
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment, bundle)
        } else {
            Toast.makeText(context, "Images for all the walls are covered", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun handlePendingCallResponse() {
        --pendingCallCount
        if (pendingCallCount == 0) {
            getAssignmentData()
        }
    }
}