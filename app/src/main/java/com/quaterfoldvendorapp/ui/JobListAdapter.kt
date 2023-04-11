package com.quaterfoldvendorapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quaterfoldvendorapp.R
import com.quaterfoldvendorapp.data.Assignment
import com.quaterfoldvendorapp.data.JobModel
import com.quaterfoldvendorapp.databinding.AssignmentListItemBinding

class JobListAdapter : RecyclerView.Adapter<JobListAdapter.Holder>() {

    private val assignments = mutableListOf<Assignment>()
    private var hasWarning = false
    private lateinit var listener : Listener
    /**
     * updates the adapter data
     *
     * @param data data of the adapter
     */
    fun updateData(data: List<Assignment?>?) {
        this.assignments.clear()
        val safeList = data?.mapNotNull { it }
        if (!safeList.isNullOrEmpty()) this.assignments.addAll(safeList)
        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            AssignmentListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(position)


    override fun getItemCount(): Int {
        return assignments.size
    }


    inner class Holder(
        private val binding: AssignmentListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {

            binding.apply {
                val assignment =  assignments[position]
                brand.text = "Brand: " +assignment.brand?.uppercase()
                assignmentId.text = "Assignment : " + assignment.assignment_code?.uppercase()

                val locationBuilder = StringBuilder()
                val village = assignment.village
                if (village.isNotEmpty() && !village.equals("-")) {
                    locationBuilder.append(village.uppercase()).append(", ")
                }

                val town = assignment.town
                if (town.isNotEmpty() && !town.equals("-")) {
                    locationBuilder.append(town.uppercase()).append(", ")
                }

                val subDistrict = assignment.sub_district
                if (subDistrict.isNotEmpty() && !subDistrict.equals("-")) {
                    locationBuilder.append(subDistrict.uppercase()).append(", ")
                }

                val district = assignment.district
                if (district.isNotEmpty() && !district.equals("-")) {
                    locationBuilder.append(district.uppercase()).append(", ")
                }

                val state = assignment.state
                if (state.isNotEmpty() && !state.equals("-")) {
                    locationBuilder.append(state.uppercase())
                }

                assignmentLocation.text = "Location: " + locationBuilder?.toString()
                noOfWalls.text = "No Of Walls: " + assignment.no_of_walls + "\nWalls Covered: " + assignment.wall_covered

                if (assignment.wall_covered >= assignment.no_of_walls) {
                    row.setBackgroundColor(root.context.resources.getColor(R.color.success_almost))
                } else {
                    row.setBackgroundColor(root.context.resources.getColor(R.color.white))
                }

                executeCta.setOnClickListener {
                    listener.onClickListener(assignment)
                }

            }

        }

    }

    interface Listener {
        fun onClickListener(assignment: Assignment)
    }

}

