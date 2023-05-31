package com.quaterfoldvendorapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.android.volley.*
import com.android.volley.Response.*
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.quaterfoldvendorapp.data.Assignment
import com.quaterfoldvendorapp.data.AssignmentImageRequest
import com.quaterfoldvendorapp.data.Resource
import com.quaterfoldvendorapp.data.local.dao.WallDao
import com.quaterfoldvendorapp.databinding.FragmentUploadImageBinding
import com.quaterfoldvendorapp.domain.ApiViewModel
import com.quaterfoldvendorapp.utils.*
import es.dmoral.toasty.Toasty
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AddImageFragment : Fragment(), TextWatcher {

    private lateinit var binding: FragmentUploadImageBinding
    private val navArgs: AddImageFragmentArgs by navArgs()

    val PICK_IMAGE_REQUEST = 1
    val MULTIPLE_PERMISSIONS = 10 // code you want.

    private val PERMISSION_REQUEST_CODE = 200
    private lateinit var assignment: Assignment
    private val viewModel: ApiViewModel by viewModel()
    private var wall_selected: String? = "1"
    var is_set = 0
    var permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    var workDialog: Dialog? = null
    var currentPhotoPath: String? = null
    var fileName = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    var IMEI: String? = null
    var wallId: String? = null
    private var card1PicBytes: String? = null
    private var card2PicBytes: String? = null
    private var card3PicBytes: String? = null
    private var card4PicBytes: String? = null
    private var card5PicBytes: String? = null
    private var card6PicBytes: String? = null
    lateinit var progressDialog: ProgressDialog
    private var imageArray: ArrayList<AssignmentImageRequest>? = ArrayList()
    private var imageArray1: JSONArray? = null
    private lateinit var launcher: ActivityResultLauncher<Intent>
    var files: ArrayList<String> = ArrayList()
    private lateinit var pDialog: ProgressDialog
    var selectedPath1 = "NONE"
    var selectedPath2 = "NONE"
    var selectedPath3 = "NONE"
    var selectedPath4 = "NONE"
    var selectedPath5 = "NONE"
    var selectedPath6 = "NONE"
    private var bitmap1: Bitmap? = null
    private var bitmap2: Bitmap? = null
    private var bitmap3: Bitmap? = null
    private var bitmap4: Bitmap? = null
    private var bitmap5: Bitmap? = null
    private var bitmap6: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentUploadImageBinding.inflate(inflater, container, false)
        assignment = navArgs.assignment

        progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Please Wait")
        progressDialog.setCancelable(false)
        pDialog = ProgressDialog(context)

        IMEI = DeviceInfoUtils.getIMEI(requireActivity())

        attachObserver()
        initView()

        return binding.root

    }

    private fun attachObserver() {

        viewModel.uploadImageResponse.observe(requireActivity()) { response ->
            if (response != null) {
                when (response.status) {
                    Resource.Status.SUCCESS -> {
                        if (progressDialog.isShowing) progressDialog.dismiss()
                        if (response.data != null) {
                            val status = response.data.status
                            val message = response.data.message
                            if (status == true) {
                                activity?.let {
                                    if (message != null) {
                                        Toasty.success(it, message).show()
                                    }
                                }
                                val intent = Intent(context, MainActivity::class.java)
                                intent.apply {
                                    startActivity(this)
                                    activity?.finish()
                                }
                            } else {
                                //saveImageLocally()
                                activity?.let {
                                    if (message != null) {
                                        Toasty.error(it, message).show()
                                    }
                                }
                            }
                        }
                    }
                    Resource.Status.LOADING -> {
                        progressDialog.show()
                    }
                    Resource.Status.ERROR -> {
                        if (progressDialog.isShowing) progressDialog.dismiss()
                        //saveImageLocally()
                        response.message?.let { it1 -> Toasty.error(requireContext(), it1) }
                    }
                }
            }
        }
    }

    private fun saveImageLocally() {
        WallDao.insertWallImages(wallId!!, imageArray1?.toString()!!)
        Log.e("Tag", "" + WallDao.getAll().toString())
        progressDialog.dismiss()
        Toasty.error(
            requireContext(),
            "Failed to upload images.",
            Toast.LENGTH_SHORT,
            false
        ).show()
        findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
    }

    private fun initView() {
        val gps = GPSTracker(context)
        // check if GPS enabled
        if (gps.canGetLocation()) {
            latitude = gps.latitude
            longitude = gps.longitude
        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert()
        }

        val distint_wall_ids = assignment.distinct_wall_id
        val totalNoOfWalls = assignment.no_of_walls
        val list: MutableList<String> = ArrayList()
        if (totalNoOfWalls != 0) {
            for (i in 1 until totalNoOfWalls + 1) {

                val check_wall_id = assignment.assignment_code.uppercase() + "-" + i
                if (!distint_wall_ids.isNullOrEmpty()) {
                    if (!distint_wall_ids.contains(check_wall_id)) {
                        list.add("Wall $i")
                    }
                } else {
                    list.add("Wall $i")
                }
            }
        }

        val adp1: ArrayAdapter<String>? = context?.let {
            ArrayAdapter<String>(
                it,
                android.R.layout.simple_list_item_1, list
            )
        }
        adp1?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.wallCount.adapter = adp1
        binding.wallCount.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                val selectedWall = parentView?.getItemAtPosition(position).toString()
                val trimmedWallNo = selectedWall.replace("Wall ", "", ignoreCase = true)
                wall_selected = trimmedWallNo
                binding.wallIdTxt.text = "WALL ID: "+ assignment.assignment_code.uppercase() + "-" + wall_selected
                // your code here
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // your code here
            }
        }
        binding.txtWallsProgress.text =
            "/ " + assignment.no_of_walls.toString()
        binding.edtBrand.setText(assignment.brand.uppercase())
        binding.edtType.setText(assignment.work_type)

        binding.txtTotalDwpAllocatedValue.text = assignment.no_of_walls.toString()
        binding.txtTotalDwpCompletedValue.text = assignment.wall_covered.toString()

        binding.edtDimension.addTextChangedListener(this)

        binding.card1.setOnClickListener {
            is_set = 1
            openGalleryOrCamera()
        }

        binding.card2.setOnClickListener {
            is_set = 2
            openGalleryOrCamera()
        }

        binding.card3.setOnClickListener {
            is_set = 3
            openGalleryOrCamera()
        }

        binding.card4.setOnClickListener {
            is_set = 4
            openGalleryOrCamera()
        }

        binding.card5.setOnClickListener {
            is_set = 5
            openGalleryOrCamera()
        }

        binding.card6.setOnClickListener {
            is_set = 6
            openGalleryOrCamera()
        }

        binding.saveBtn.setOnClickListener {
            if (isValid()) {
                createObject()
            }
        }
    }

    private fun registerLauncher() {
        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode === RESULT_OK) {
                    val uri: Uri? = result.data?.data
                    // Use the uri to load the image
                    uri?.let { setData(it) }

                } else if (result.resultCode === ImagePicker.RESULT_ERROR) {
                    // Use to show an error
                    val error = ImagePicker.Companion.getError(result.data)
                    Toasty.error(requireContext(), "" + error).show()
                }
            }
    }

    private fun openGalleryOrCamera() {
        ImagePicker.with(requireActivity()).provider(ImageProvider.BOTH).crop()
            .createIntentFromDialog { launcher.launch(it) }
    }

    private fun validateDimension() {
        val dimension = binding.edtDimension.text?.toString()?.trim()?.toIntOrNull() ?: 0
        val dimensionCovered = assignment.sq_ft_covered
        val totalDimension = assignment.total_sq_feet.toString().trim().toInt()

        val differenceInDimen =
            totalDimension.minus(dimensionCovered)
        if (dimension > differenceInDimen) {
            binding.edtDimension.error = "Dimensions cannot exceed " + differenceInDimen
        } else {
            binding.edtDimension.error = null
        }
    }

    private fun isValid(): Boolean {
        val dimension = binding.edtDimension.text?.toString()?.trim()
        val dimensionCovered = assignment.sq_ft_covered
        val totalDimension = assignment.total_sq_feet.toString().trim().toInt()

        val differenceInDimen =
            totalDimension.minus(dimensionCovered)
        if (dimension.isNullOrEmpty()) {
            Toast.makeText(context, "Please enter a valid dimension", Toast.LENGTH_SHORT).show()
            return false
        } else {
            if (dimension.toInt() > differenceInDimen) {
                Toast.makeText(
                    context,
                    "Dimensions cannot exceed " + differenceInDimen,
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
        }

        if (card1PicBytes.isNullOrEmpty()
            && card2PicBytes.isNullOrEmpty()
            && card3PicBytes.isNullOrEmpty()
            && card4PicBytes.isNullOrEmpty()
            && card5PicBytes.isNullOrEmpty()
            && card6PicBytes.isNullOrEmpty()
        ) {
            Toast.makeText(context, "Please upload at-least 2 images", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!(!selectedPath1.equals("NONE", true) || !selectedPath2.equals("NONE", true)
                    || !selectedPath3.equals("NONE", true) || !selectedPath4.equals("NONE", true)
                    || !selectedPath5.equals("NONE", true) || !selectedPath6.equals("NONE", true)
                    )
        ) {
            Toast.makeText(
                activity,
                "Please select at-least two files to upload.",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        return true
    }

    private fun createObject() {
        imageArray?.clear()
        imageArray1 = JSONArray()
        wallId = assignment.assignment_code.uppercase() + "-" + wall_selected
        try {

            if (!card1PicBytes.isNullOrEmpty()) {
                val imageObj = AssignmentImageRequest()
                imageObj.project_id = assignment.project_code_id
                imageObj.customer_id = assignment.customer_code_id
                imageObj.assignment_id = assignment.id
                imageObj.assignment_code = assignment.assignment_code.uppercase()
                imageObj.wall_id = wallId
                imageObj.lat = latitude
                imageObj.long = longitude
                imageObj.sq_ft_covered = getStringFromEditText(binding.edtDimension)
                imageObj.imei = IMEI?.uppercase()
                imageArray?.add(imageObj)

                val imageObj1 = JSONObject()
                imageObj1.put("project_id", assignment.project_code_id)
                imageObj1.put("customer_id", assignment.customer_code_id)
                imageObj1.put("assignment_id", assignment.id)
                imageObj1.put("assignment_code", assignment.assignment_code.uppercase())
                imageObj1.put("wall_id", wallId)
                imageObj1.put("lat", latitude)
                imageObj1.put("long", longitude)
                imageObj1.put("sq_ft_covered", getStringFromEditText(binding.edtDimension))
                imageObj1.put("imei", IMEI?.uppercase())
                imageArray1!!.put(imageObj1)

            }

            if (!card2PicBytes.isNullOrEmpty()) {
                val imageObj = AssignmentImageRequest()
                imageObj.project_id = assignment.project_code_id
                imageObj.customer_id = assignment.customer_code_id
                imageObj.assignment_id = assignment.id
                imageObj.assignment_code = assignment.assignment_code.uppercase()
                imageObj.wall_id = wallId
                imageObj.lat = latitude
                imageObj.long = longitude
                imageObj.sq_ft_covered = getStringFromEditText(binding.edtDimension)
                imageObj.imei = IMEI?.uppercase()
                imageArray?.add(imageObj)

                val imageObj1 = JSONObject()
                imageObj1.put("project_id", assignment.project_code_id)
                imageObj1.put("customer_id", assignment.customer_code_id)
                imageObj1.put("assignment_id", assignment.id)
                imageObj1.put("assignment_code", assignment.assignment_code.uppercase())
                imageObj1.put("wall_id", wallId)
                imageObj1.put("lat", latitude)
                imageObj1.put("long", longitude)
                imageObj1.put("sq_ft_covered", getStringFromEditText(binding.edtDimension))
                imageObj1.put("imei", IMEI?.uppercase())
                imageArray1!!.put(imageObj1)
            }

            if (!card3PicBytes.isNullOrEmpty()) {
                val imageObj = AssignmentImageRequest()
                imageObj.project_id = assignment.project_code_id
                imageObj.customer_id = assignment.customer_code_id
                imageObj.assignment_id = assignment.id
                imageObj.assignment_code = assignment.assignment_code.uppercase()
                imageObj.wall_id = wallId
                imageObj.lat = latitude
                imageObj.long = longitude
                imageObj.sq_ft_covered = getStringFromEditText(binding.edtDimension)
                imageObj.imei = IMEI?.uppercase()
                imageArray?.add(imageObj)

                val imageObj1 = JSONObject()
                imageObj1.put("project_id", assignment.project_code_id)
                imageObj1.put("customer_id", assignment.customer_code_id)
                imageObj1.put("assignment_id", assignment.id)
                imageObj1.put("assignment_code", assignment.assignment_code.uppercase())
                imageObj1.put("wall_id", wallId)
                imageObj1.put("lat", latitude)
                imageObj1.put("long", longitude)
                imageObj1.put("sq_ft_covered", getStringFromEditText(binding.edtDimension))
                imageObj1.put("imei", IMEI?.uppercase())
                imageArray1!!.put(imageObj1)
            }

            if (!card4PicBytes.isNullOrEmpty()) {
                val imageObj = AssignmentImageRequest()
                imageObj.project_id = assignment.project_code_id
                imageObj.customer_id = assignment.customer_code_id
                imageObj.assignment_id = assignment.id
                imageObj.assignment_code = assignment.assignment_code.uppercase()
                imageObj.wall_id = wallId
                imageObj.lat = latitude
                imageObj.long = longitude
                imageObj.sq_ft_covered = getStringFromEditText(binding.edtDimension)
                imageObj.imei = IMEI?.uppercase()
                imageArray?.add(imageObj)

                val imageObj1 = JSONObject()
                imageObj1.put("project_id", assignment.project_code_id)
                imageObj1.put("customer_id", assignment.customer_code_id)
                imageObj1.put("assignment_id", assignment.id)
                imageObj1.put("assignment_code", assignment.assignment_code.uppercase())
                imageObj1.put("wall_id", wallId)
                imageObj1.put("lat", latitude)
                imageObj1.put("long", longitude)
                imageObj1.put("sq_ft_covered", getStringFromEditText(binding.edtDimension))
                imageObj1.put("imei", IMEI?.uppercase())
                imageArray1!!.put(imageObj1)
            }

            if (!card5PicBytes.isNullOrEmpty()) {
                val imageObj = AssignmentImageRequest()
                imageObj.project_id = assignment.project_code_id
                imageObj.customer_id = assignment.customer_code_id
                imageObj.assignment_id = assignment.id
                imageObj.assignment_code = assignment.assignment_code.uppercase()
                imageObj.wall_id = wallId
                imageObj.lat = latitude
                imageObj.long = longitude
                imageObj.sq_ft_covered = getStringFromEditText(binding.edtDimension)
                imageObj.imei = IMEI?.uppercase()
                imageArray?.add(imageObj)

                val imageObj1 = JSONObject()
                imageObj1.put("project_id", assignment.project_code_id)
                imageObj1.put("customer_id", assignment.customer_code_id)
                imageObj1.put("assignment_id", assignment.id)
                imageObj1.put("assignment_code", assignment.assignment_code.uppercase())
                imageObj1.put("wall_id", wallId)
                imageObj1.put("lat", latitude)
                imageObj1.put("long", longitude)
                imageObj1.put("sq_ft_covered", getStringFromEditText(binding.edtDimension))
                imageObj1.put("imei", IMEI?.uppercase())
                imageArray1!!.put(imageObj1)
            }

            if (!card6PicBytes.isNullOrEmpty()) {
                val imageObj = AssignmentImageRequest()
                imageObj.project_id = assignment.project_code_id
                imageObj.customer_id = assignment.customer_code_id
                imageObj.assignment_id = assignment.id
                imageObj.assignment_code = assignment.assignment_code.uppercase()
                imageObj.wall_id = wallId
                imageObj.lat = latitude
                imageObj.long = longitude
                imageObj.sq_ft_covered = getStringFromEditText(binding.edtDimension)
                imageObj.imei = IMEI?.uppercase()
                imageArray?.add(imageObj)

                val imageObj1 = JSONObject()
                imageObj1.put("project_id", assignment.project_code_id)
                imageObj1.put("customer_id", assignment.customer_code_id)
                imageObj1.put("assignment_id", assignment.id)
                imageObj1.put("assignment_code", assignment.assignment_code.uppercase())
                imageObj1.put("wall_id", wallId)
                imageObj1.put("lat", latitude)
                imageObj1.put("long", longitude)
                imageObj1.put("sq_ft_covered", getStringFromEditText(binding.edtDimension))
                imageObj1.put("imei", IMEI?.uppercase())
                imageArray1!!.put(imageObj1)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        //doFileUpload()
        try {
            var file1Data: MultipartBody.Part? = null
            file1Data = if (!selectedPath1.equals("NONE", true)) {
                val file1 = File(selectedPath1)
                MultipartBody.Part
                    .createFormData(
                        name = "uploadedfile1",
                        filename = file1.name,
                        body = file1.asRequestBody()
                    )
            } else {
                MultipartBody.Part.createFormData(
                    "attachment", "", ""
                        .toRequestBody("text/plain".toMediaTypeOrNull())
                )
            }

            var file2Data: MultipartBody.Part? = null
            file2Data = if (!selectedPath2.equals("NONE", true)) {
                val file2 = File(selectedPath2)
                MultipartBody.Part
                    .createFormData(
                        name = "uploadedfile2",
                        filename = file2.name,
                        body = file2.asRequestBody()
                    )
            } else {
                MultipartBody.Part.createFormData(
                    "attachment", "", ""
                        .toRequestBody("text/plain".toMediaTypeOrNull())
                )
            }

            var file3Data: MultipartBody.Part? = null
            file3Data = if (!selectedPath3.equals("NONE", true)) {
                val file3 = File(selectedPath3)
                MultipartBody.Part
                    .createFormData(
                        name = "uploadedfile3",
                        filename = file3.name,
                        body = file3.asRequestBody()
                    )
            } else {
                MultipartBody.Part.createFormData(
                    "attachment", "", ""
                        .toRequestBody("text/plain".toMediaTypeOrNull())
                )
            }

            var file4Data: MultipartBody.Part? = null
            file4Data = if (!selectedPath4.equals("NONE", true)) {
                val file4 = File(selectedPath4)
                MultipartBody.Part
                    .createFormData(
                        name = "uploadedfile4",
                        filename = file4.name,
                        body = file4.asRequestBody()
                    )
            } else {
                MultipartBody.Part.createFormData(
                    "attachment", "", ""
                        .toRequestBody("text/plain".toMediaTypeOrNull())
                )
            }

            var file5Data: MultipartBody.Part? = null
            file5Data = if (!selectedPath5.equals("NONE", true)) {
                val file5 = File(selectedPath5)
                MultipartBody.Part
                    .createFormData(
                        name = "uploadedfile5",
                        filename = file5.name,
                        body = file5.asRequestBody()
                    )
            } else {
                MultipartBody.Part.createFormData(
                    "attachment", "", ""
                        .toRequestBody("text/plain".toMediaTypeOrNull())
                )
            }

            var file6Data: MultipartBody.Part? = null
            file6Data = if (!selectedPath6.equals("NONE", true)) {
                val file6 = File(selectedPath6)
                MultipartBody.Part
                    .createFormData(
                        name = "uploadedfile6",
                        filename = file6.name,
                        body = file6.asRequestBody()
                    )
            } else {
                MultipartBody.Part.createFormData(
                    "attachment", "", ""
                        .toRequestBody("text/plain".toMediaTypeOrNull())
                )
            }

            val images = imageArray1.toString()
            Log.d("imagessss--->>", images)
            progressDialog.show()
            viewModel.uploadAssignmentImages(
                images,
                file1Data,
                file2Data,
                file3Data,
                file4Data,
                file5Data,
                file6Data
            )
        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    private fun setData(uri: Uri) {
        val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri)
        if (null != bitmap) {
            val base64Image = getEncoded64ImageStringFromBitmap(bitmap)
            when (is_set) {
                1 -> {
                    selectedPath1 = uri.path.toString()
                    bitmap1 = bitmap
                    card1PicBytes = base64Image
                    binding.imageView1.setImageBitmap(bitmap)
                    binding.imageView1.visible()
                    binding.imageView1.setOnClickListener {
                        viewImageFromURL(bitmap)
                    }
                }
                2 -> {
                    selectedPath2 = uri.path.toString()
                    bitmap2 = bitmap
                    card2PicBytes = base64Image
                    binding.imageView2.setImageBitmap(bitmap)
                    binding.imageView2.visible()
                    binding.imageView2.setOnClickListener {
                        viewImageFromURL(bitmap)
                    }
                }
                3 -> {
                    selectedPath3 = uri.path.toString()
                    bitmap3 = bitmap
                    card3PicBytes = base64Image
                    binding.imageView3.setImageBitmap(bitmap)
                    binding.imageView3.visible()
                    binding.imageView3.setOnClickListener {
                        viewImageFromURL(bitmap)
                    }
                }
                4 -> {
                    selectedPath4 = uri.path.toString()
                    bitmap4 = bitmap
                    card4PicBytes = base64Image
                    binding.imageView4.setImageBitmap(bitmap)
                    binding.imageView4.visible()
                    binding.imageView4.setOnClickListener {
                        viewImageFromURL(bitmap)
                    }
                }
                5 -> {
                    selectedPath5 = uri.path.toString()
                    bitmap5 = bitmap
                    card5PicBytes = base64Image
                    binding.imageView5.setImageBitmap(bitmap)
                    binding.imageView5.visible()
                    binding.imageView5.setOnClickListener {
                        viewImageFromURL(bitmap)
                    }
                }
                6 -> {
                    selectedPath6 = uri.path.toString()
                    bitmap6 = bitmap
                    card6PicBytes = base64Image
                    binding.imageView6.setImageBitmap(bitmap)
                    binding.imageView6.visible()
                    binding.imageView6.setOnClickListener {
                        viewImageFromURL(bitmap)
                    }
                }
            }
        }
    }


    private fun setLatLongToBitmap(myBitmap: Bitmap): Bitmap? {
        @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss")
        val captionString = sdf.format(Date())
        val text = """
                Date:$captionString
                Lat:$latitude
                Long:$longitude
                IMEI:${IMEI}
                """.trimIndent()
        val str: Array<String> = text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()

        var dest: Bitmap? = null
        try {
            dest = myBitmap.copy(myBitmap.config, true)
        } catch (e1: OutOfMemoryError) {
            Log.e("Exception", e1.message!!)
            e1.printStackTrace()
        } catch (e: Error) {
            Log.e("Exception", e.message!!)
            e.printStackTrace()
        }

        if (null != dest) {
            val cs = Canvas(dest)
            val tf = Typeface.create("Verdana", Typeface.BOLD)
            val tPaint = Paint()
            tPaint.isAntiAlias = true
            tPaint.textSize = 60f
            tPaint.textAlign = Paint.Align.LEFT
            tPaint.typeface = tf
            tPaint.color = Color.RED
            tPaint.style = Paint.Style.FILL_AND_STROKE
            cs.drawBitmap(myBitmap, 0f, 0f, null)
            val textHeight = tPaint.measureText("yY")
            str.withIndex().forEach { (index, singleLine) ->
                cs.drawText(
                    singleLine, 20f, (index + 1) * textHeight + 25f,
                    tPaint
                )
            }
            return Bitmap.createScaledBitmap(dest, 1200, 1200, true)
        }
        return null
    }

    private fun getEncoded64ImageStringFromBitmap(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream)
        val byteFormat = stream.toByteArray()
        // get the base 64 string
        return Base64.encodeToString(byteFormat, Base64.NO_WRAP)
    }

    private fun viewImageFromURL(imageBitmap: Bitmap?) {
        val settingsDialog = Dialog(requireContext())
        settingsDialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        val lp = RelativeLayout.LayoutParams(1000, 1000)
        lp.addRule(RelativeLayout.CENTER_IN_PARENT)
        lp.setMargins(5, 5, 5, 5)
        val iv = ImageView(activity)
        iv.setPadding(5, 5, 5, 5)
        iv.adjustViewBounds = true
        iv.layoutParams = lp
        iv.scaleType = ImageView.ScaleType.CENTER_INSIDE
        if (imageBitmap != null) {
            activity?.let { iv.setImageBitmap(imageBitmap) }
        } else {
            settingsDialog.dismiss()
            return
        }
        settingsDialog.addContentView(iv, lp)
        settingsDialog.show()
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        if (!p0.isNullOrEmpty()) {
            validateDimension()
        }
    }

    override fun afterTextChanged(p0: Editable?) {
    }
}