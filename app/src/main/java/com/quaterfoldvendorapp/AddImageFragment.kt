package com.quaterfoldvendorapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.quaterfoldvendorapp.data.Assignment
import com.quaterfoldvendorapp.data.AssignmentImageRequest
import com.quaterfoldvendorapp.data.AssignmentSaveRequest
import com.quaterfoldvendorapp.data.Resource
import com.quaterfoldvendorapp.data.local.dao.WallDao
import com.quaterfoldvendorapp.databinding.FragmentUploadImageBinding
import com.quaterfoldvendorapp.domain.ApiViewModel
import com.quaterfoldvendorapp.utils.*
import es.dmoral.toasty.Toasty
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

        IMEI = DeviceInfoUtils.getIMEI(requireActivity())

        attachObserver()
        initView()

        return binding.root

    }

    private fun attachObserver() {

        viewModel.assignmentSaveResponse.observe(requireActivity()) { response ->
            if (response != null) {
                when (response.status) {
                    Resource.Status.SUCCESS -> {
                        if (response.data != null) {
                            val status = response.data.status
                            if (status == "valid") {
                                progressDialog.dismiss()
                                findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                            } else {
                                saveImageLocally()
                            }
                        }
                    }
                    Resource.Status.LOADING -> {
                        progressDialog.show()
                    }
                    Resource.Status.ERROR -> {
                        saveImageLocally()
                        //response.message?.let { it1 -> Toasty.error(requireContext(), it1) }
                    }
                }
            }
        }
    }

    private fun saveImageLocally() {
        imageArray1?.toString()
            ?.let { WallDao.insertWallImages(wallId!!, it) }
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

        val wall_starting = assignment.wall_covered + 1
        binding.txtWallsProgress.text =
            wall_starting.toString() + "/ " + assignment.no_of_walls.toString()
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
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.getResultCode() === RESULT_OK) {
                    val uri: Uri? = result.data?.data
                    // Use the uri to load the image
                    uri?.let { setData(it) }

                } else if (result.getResultCode() === ImagePicker.RESULT_ERROR) {
                    // Use to show an error
                    val error = ImagePicker.Companion.getError(result.data)
                    Toasty.error(requireContext(), "" +error, ).show()
                }
            }
    }

    private fun openGalleryOrCamera() {
        ImagePicker.with(requireActivity()).provider(ImageProvider.BOTH).crop().createIntentFromDialog { launcher.launch(it) }
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

        return true
    }

    private fun createObject() {
        imageArray?.clear()
        imageArray1 = JSONArray()
        val wall_starting = assignment.wall_covered + 1
        wallId = assignment.assignment_code.uppercase() + "-" + wall_starting
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
                imageObj.image_bitmap = card1PicBytes
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
                imageObj1.put("image_bitmap", card1PicBytes)
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
                imageObj.image_bitmap = card2PicBytes
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
                imageObj1.put("image_bitmap", card2PicBytes)
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
                imageObj.image_bitmap = card3PicBytes
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
                imageObj1.put("image_bitmap", card3PicBytes)
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
                imageObj.image_bitmap = card4PicBytes
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
                imageObj1.put("image_bitmap", card4PicBytes)
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
                imageObj.image_bitmap = card5PicBytes
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
                imageObj1.put("image_bitmap", card5PicBytes)
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
                imageObj.image_bitmap = card6PicBytes
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
                imageObj1.put("image_bitmap", card6PicBytes)
                imageArray1!!.put(imageObj1)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        //progressDialog.show()
        val assignmentData = AssignmentSaveRequest()
        assignmentData.images = imageArray
        try {
            viewModel.saveAssignmentImages(assignmentData)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setData(uri: Uri) {
        //val imgFile = File(filePath)
        val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri)
        if (null != bitmap) {
                //myBitmap = setLatLongToBitmap(myBitmap)
                val base64Image = getEncoded64ImageStringFromBitmap(bitmap)
                when (is_set) {
                    1 -> {
                        card1PicBytes = base64Image
                        binding.imageView1.setImageBitmap(bitmap)
                        binding.imageView1.visible()
                        binding.imageView1.setOnClickListener {
                            viewImageFromURL(bitmap)
                        }
                    }
                    2 -> {
                        card2PicBytes = base64Image
                        binding.imageView2.setImageBitmap(bitmap)
                        binding.imageView2.visible()
                        binding.imageView2.setOnClickListener {
                            viewImageFromURL(bitmap)
                        }
                    }
                    3 -> {
                        card3PicBytes = base64Image
                        binding.imageView3.setImageBitmap(bitmap)
                        binding.imageView3.visible()
                        binding.imageView3.setOnClickListener {
                            viewImageFromURL(bitmap)
                        }
                    }
                    4 -> {
                        card4PicBytes = base64Image
                        binding.imageView4.setImageBitmap(bitmap)
                        binding.imageView4.visible()
                        binding.imageView4.setOnClickListener {
                            viewImageFromURL(bitmap)
                        }
                    }
                    5 -> {
                        card5PicBytes = base64Image
                        binding.imageView5.setImageBitmap(bitmap)
                        binding.imageView5.visible()
                        binding.imageView5.setOnClickListener {
                            viewImageFromURL(bitmap)
                        }
                    }
                    6 -> {
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
        //use in your case iv.setImageBitmap(convertToBitmap(data.getImage()));
        settingsDialog.addContentView(iv, lp)
        settingsDialog.show()
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .toString() + "/QuaterfoldImages"
        )
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.absolutePath
        return image
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