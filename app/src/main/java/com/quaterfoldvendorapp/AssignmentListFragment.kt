package com.quaterfoldvendorapp

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quaterfoldvendorapp.data.*
import com.quaterfoldvendorapp.data.local.dao.WallDao
import com.quaterfoldvendorapp.databinding.FragmentAssignmentListBinding
import com.quaterfoldvendorapp.domain.ApiViewModel
import com.quaterfoldvendorapp.sharedpreference.SharedPrefManager
import com.quaterfoldvendorapp.ui.JobListAdapter
import com.quaterfoldvendorapp.utils.Convertor
import es.dmoral.toasty.Toasty
import org.koin.androidx.viewmodel.ext.android.viewModel


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class AssignmentListFragment : Fragment(), JobListAdapter.Listener {

    private lateinit var binding: FragmentAssignmentListBinding
    private lateinit var progressDialog: ProgressDialog

    // This property is only valid between onCreateView and
    // onDestroyView.
    private var jobListAdapter: JobListAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var agentinfo: Agentinfo? = null
    private var posId: String? = null
    private var pendingCallCount = 0
    private val viewModel: ApiViewModel by viewModel()
    private val assignmentList = mutableListOf<Assignment>()
    private var wallId: String? = null
    private var assignmentModelArrayList: ArrayList<Assignment>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssignmentListBinding.inflate(inflater, container, false)

        progressDialog = ProgressDialog(requireActivity())
        progressDialog.setMessage("Please Wait")
        progressDialog.setCancelable(false)

        attachObserver()
        initView()

        return binding.root
    }

    private fun attachObserver() {

        viewModel.assignmentResponse.observe(requireActivity()) { response ->
            if (response != null) {
                when (response.status) {
                    Resource.Status.SUCCESS -> {
                        progressDialog.dismiss()
                        assignmentList.clear()
                        assignmentModelArrayList = ArrayList()
                        if (response.data?.data != null && response.data.status == "valid") {
                            val responseData = response.data.data
                            assignmentModelArrayList?.addAll(responseData)
                            assignmentList.addAll(responseData)
                            if (jobListAdapter == null) {
                                jobListAdapter = JobListAdapter()
                            }
                            jobListAdapter?.updateData(assignmentList)
                            jobListAdapter?.setListener(this)
                        } else {
                            progressDialog.dismiss()
                            assignmentList.clear()
                            Toasty.error(
                                requireContext(),
                                "Failed to get data. Please try again later.",
                                Toast.LENGTH_SHORT,
                                false
                            ).show()
                        }
                    }
                    Resource.Status.LOADING -> {
                        progressDialog.show()
                    }
                    Resource.Status.ERROR -> {
                        progressDialog.dismiss()
                        assignmentList.clear()
                        response.message?.let { it1 -> Toasty.error(requireContext(), it1) }
                    }
                }
            }
        }

        viewModel.assignmentSaveResponse.observe(requireActivity()) { response ->
            if (response != null) {
                when (response.status) {
                    Resource.Status.SUCCESS -> {
                        if (response.data != null) {
                            val status = response.data.status
                            if (status == "valid") {
                                if (!wallId.isNullOrEmpty()) {
                                    WallDao.deleteWallImages(wallId!!)
                                }
                                handlePendingCallResponse()
                            } else {
                                handlePendingCallResponse()
                            }

                        }
                    }
                    Resource.Status.LOADING -> {
                        //progressDialog.show()
                    }
                    Resource.Status.ERROR -> {
                        //progressDialog.dismiss()
                        handlePendingCallResponse()
                        response.message?.let { it1 -> Toasty.error(requireContext(), it1) }
                    }
                }
            }
        }
    }

    private fun initView() {

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

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        })

        uploadPendingWallImages()
    }

    private fun filter(text: String?) {
        // creating a new array list to filter our data.
        val filteredlist = ArrayList<Assignment>()

        // running a for loop to compare elements.
        if (!assignmentModelArrayList.isNullOrEmpty()) {
            for (item in assignmentModelArrayList!!) {
                0
                // checking if the entered string matched with any item of our recycler view.
                if (!text.isNullOrEmpty()) {
                    if (item.assignment_code.lowercase().contains(text.lowercase())
                        || item.dealer_name.lowercase().contains(text.lowercase())
                        || item.state.lowercase().contains(text.lowercase())
                        || item.district.lowercase().contains(text.lowercase())
                        || item.village.lowercase().contains(text.lowercase())
                        || item.sub_district.lowercase().contains(text.lowercase())
                        || item.town.lowercase().contains(text.lowercase())
                        || item.brand.lowercase().contains(text.lowercase())
                    ) {
                        // if the item is matched we are
                        // adding it to our filtered list.
                        filteredlist.add(item)
                    }
                } else {
                    filteredlist.addAll(assignmentModelArrayList!!)
                }
            }
        }
        if (filteredlist.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
            //Toast.makeText(activity, "No Data Found..", Toast.LENGTH_SHORT).show()
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            jobListAdapter?.filterList(filteredlist)
            jobListAdapter?.setListener(this)
        }
    }

    private fun getAssignmentData() {
        agentinfo = SharedPrefManager.getInstance(context).agent
        posId = agentinfo?.id

        val assignmentData = AssignmentRequest()
        assignmentData.vendor_id = posId
        viewModel.getAssignmentList(assignmentData)
    }

    private fun uploadPendingWallImages() {
        val pendingWallImages = WallDao.getAll()
        if (pendingWallImages.isNullOrEmpty()) {
            getAssignmentData()
            return
        }
        pendingCallCount = pendingWallImages.size
        pendingWallImages.forEach {
            val images = it.imageArray
            val imgArrayList = Convertor.convertJsonToArray(images)

            val assignmentData = AssignmentSaveRequest()
            wallId = it._id
            assignmentData.images = imgArrayList
            viewModel.saveAssignmentImages(assignmentData)
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