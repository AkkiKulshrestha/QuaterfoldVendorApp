package com.quaterfoldvendorapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.TelephonyManager
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
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage
import com.quaterfoldvendorapp.data.Assignment
import com.quaterfoldvendorapp.data.local.dao.WallDao
import com.quaterfoldvendorapp.databinding.FragmentUploadImageBinding
import com.quaterfoldvendorapp.interfaces.UploadImagesCallback
import com.quaterfoldvendorapp.utils.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AddImageFragment : Fragment(), PermissionUtil.PermissionsCallBack, TextWatcher {

    private lateinit var binding: FragmentUploadImageBinding
    private val navArgs: AddImageFragmentArgs by navArgs()

    val PICK_IMAGE_REQUEST = 1
    val MULTIPLE_PERMISSIONS = 10 // code you want.

    private val PERMISSION_REQUEST_CODE = 200
    private lateinit var assignment: Assignment

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
    private var card1PicBytes: String? = null
    private var card2PicBytes: String? = null
    private var card3PicBytes: String? = null
    private var card4PicBytes: String? = null
    private var card5PicBytes: String? = null
    private var card6PicBytes: String? = null
    var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentUploadImageBinding.inflate(inflater, container, false)
        assignment = navArgs.assignment
        IMEI = getDeviceIMEI()
        Log.d("IMEI", " -->$IMEI")
        initView()

        return binding.root

    }

    /**
     * Returns the unique identifier for the device
     *
     * @return unique identifier for the device
     */
    private fun getDeviceIMEI(): String? {
        var deviceUniqueIdentifier: String? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deviceUniqueIdentifier = Settings.Secure.getString(
                activity?.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        } else {
            val mTelephony =
                activity?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (mTelephony.deviceId != null) {
                deviceUniqueIdentifier = mTelephony.deviceId
            } else {
                deviceUniqueIdentifier = Settings.Secure.getString(
                    activity?.contentResolver,
                    Settings.Secure.ANDROID_ID
                )
            }
        }
        return deviceUniqueIdentifier
    }

    private fun initView() {

        progressDialog = ProgressDialog(context)
        progressDialog?.setMessage("Please Wait")
        progressDialog?.setCancelable(false)

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
            requestPermissions()
        }

        binding.card2.setOnClickListener {
            is_set = 2
            requestPermissions()
        }

        binding.card3.setOnClickListener {
            is_set = 3
            requestPermissions()
        }

        binding.card4.setOnClickListener {
            is_set = 4
            requestPermissions()
        }

        binding.card5.setOnClickListener {
            is_set = 5
            requestPermissions()
        }

        binding.card6.setOnClickListener {
            is_set = 6
            requestPermissions()
        }

        binding.saveBtn.setOnClickListener {
            if (isValid()) {
                createObject()
            }
        }


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
        val imageArray = JSONArray()
        val wall_starting = assignment.wall_covered + 1
        val wallId = assignment.assignment_code.uppercase() + "-" + wall_starting
        try {

            if (!card1PicBytes.isNullOrEmpty()) {
                val imageObj = JSONObject()
                imageObj.put("project_id", assignment.project_code_id)
                imageObj.put("customer_id", assignment.customer_code_id)
                imageObj.put("assignment_id", assignment.id)
                imageObj.put("assignment_code", assignment.assignment_code.uppercase())
                imageObj.put("wall_id", wallId)
                imageObj.put("lat", latitude)
                imageObj.put("long", longitude)
                imageObj.put("sq_ft_covered", getStringFromEditText(binding.edtDimension))
                imageObj.put("imei", IMEI?.uppercase())
                imageObj.put("image_bitmap", card1PicBytes)
                imageArray.put(imageObj)
            }

            if (!card2PicBytes.isNullOrEmpty()) {
                val imageObj = JSONObject()
                imageObj.put("project_id", assignment.project_code_id)
                imageObj.put("customer_id", assignment.customer_code_id)
                imageObj.put("assignment_id", assignment.id)
                imageObj.put("assignment_code", assignment.assignment_code.uppercase())
                imageObj.put("wall_id", wallId)
                imageObj.put("lat", latitude)
                imageObj.put("long", longitude)
                imageObj.put("sq_ft_covered", getStringFromEditText(binding.edtDimension))
                imageObj.put("imei", IMEI?.uppercase())
                imageObj.put("image_bitmap", card2PicBytes)
                imageArray.put(imageObj)
            }

            if (!card3PicBytes.isNullOrEmpty()) {
                val imageObj = JSONObject()
                imageObj.put("project_id", assignment.project_code_id)
                imageObj.put("customer_id", assignment.customer_code_id)
                imageObj.put("assignment_id", assignment.id)
                imageObj.put("assignment_code", assignment.assignment_code.uppercase())
                imageObj.put("wall_id", wallId)
                imageObj.put("lat", latitude)
                imageObj.put("long", longitude)
                imageObj.put("sq_ft_covered", getStringFromEditText(binding.edtDimension))
                imageObj.put("imei", IMEI?.uppercase())
                imageObj.put("image_bitmap", card3PicBytes)
                imageArray.put(imageObj)
            }

            if (!card4PicBytes.isNullOrEmpty()) {
                val imageObj = JSONObject()
                imageObj.put("project_id", assignment.project_code_id)
                imageObj.put("customer_id", assignment.customer_code_id)
                imageObj.put("assignment_id", assignment.id)
                imageObj.put("assignment_code", assignment.assignment_code.uppercase())
                imageObj.put("wall_id", wallId)
                imageObj.put("lat", latitude)
                imageObj.put("long", longitude)
                imageObj.put("sq_ft_covered", getStringFromEditText(binding.edtDimension))
                imageObj.put("imei", IMEI?.uppercase())
                imageObj.put("image_bitmap", card4PicBytes)

                imageArray.put(imageObj)
            }

            if (!card5PicBytes.isNullOrEmpty()) {
                val imageObj = JSONObject()
                imageObj.put("project_id", assignment.project_code_id)
                imageObj.put("customer_id", assignment.customer_code_id)
                imageObj.put("assignment_id", assignment.id)
                imageObj.put("assignment_code", assignment.assignment_code.uppercase())
                imageObj.put("wall_id", wallId)
                imageObj.put("lat", latitude)
                imageObj.put("long", longitude)
                imageObj.put("sq_ft_covered", getStringFromEditText(binding.edtDimension))
                imageObj.put("imei", IMEI?.uppercase())
                imageObj.put("image_bitmap", card5PicBytes)

                imageArray.put(imageObj)
            }

            if (!card6PicBytes.isNullOrEmpty()) {
                val imageObj = JSONObject()
                imageObj.put("project_id", assignment.project_code_id)
                imageObj.put("customer_id", assignment.customer_code_id)
                imageObj.put("assignment_id", assignment.id)
                imageObj.put("assignment_code", assignment.assignment_code.uppercase())
                imageObj.put("wall_id", wallId)
                imageObj.put("lat", latitude)
                imageObj.put("long", longitude)
                imageObj.put("sq_ft_covered", getStringFromEditText(binding.edtDimension))
                imageObj.put("imei", IMEI?.uppercase())
                imageObj.put("image_bitmap", card6PicBytes)
                imageArray.put(imageObj)
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        progressDialog?.show()
        Log.d("SubmitImages", SharedPrefConstant.UPLOAD_IMAGE_URL)
        VendorServices.uploadImages(context,
            imageArray.toString(),
            object : UploadImagesCallback {
                override fun onSuccess() {
                    WallDao.deleteWallImages(wallId)
                    if (progressDialog?.isShowing!!) {
                        progressDialog?.dismiss()
                    }
                    findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                }

                override fun onFailure(msg: String?) {
                    WallDao.insertWallImages(wallId, imageArray.toString())
                    if (progressDialog?.isShowing!!) {
                        progressDialog?.dismiss()
                    }
                    if (!msg.isNullOrEmpty()) {
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    private fun requestPermissions() {
        if (PermissionUtil.checkAndRequestPermissions(
                activity,
                Manifest.permission.CAMERA
                //Manifest.permission.READ_EXTERNAL_STORAGE,
                // Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        ) {
            showFileChooser()
        }
    }

    private fun showFileChooser() {
        fileName = System.currentTimeMillis().toString() + ".jpg"
        workDialog = context?.let { Dialog(it) }
        workDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        workDialog?.setCanceledOnTouchOutside(true)
        workDialog?.setCancelable(true)
        workDialog?.setContentView(R.layout.dialog_upload_image_from)
        workDialog?.window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )
        workDialog?.show()
        val card_camera: CardView? = workDialog?.findViewById(R.id.card_camera)
        card_camera?.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.d(ContentValues.TAG, "onClick: IN IF")
                val photoURI = FileProvider.getUriForFile(
                    requireContext(),
                    context?.packageName + ".fileprovider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(
                    takePictureIntent,
                    PICK_IMAGE_REQUEST
                )
            }
            workDialog?.dismiss()
        }
        val card_gallery: CardView? = workDialog?.findViewById(R.id.card_gallery)
        card_gallery?.setOnClickListener {
            val chooser =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(
                chooser,
                PICK_IMAGE_REQUEST
            )
            workDialog?.dismiss()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtil.onRequestPermissionsResult(
            activity,
            requestCode,
            permissions,
            grantResults,
            this
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                onFileReceivedFromSys(data)
            } else {
                Log.d("onFileRecievedFromSys", "" + currentPhotoPath)
                if (!currentPhotoPath.isNullOrEmpty()) {
                    setData(currentPhotoPath!!)
                } else {
                    Toast.makeText(context, "Please try again", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private fun onFileReceivedFromSys(data: Intent?) {
        var filePath: String? = null
        filePath = ImagePicker.getImageFromResult(context, data, fileName)
        setData(filePath)
        Log.d(ContentValues.TAG, "onFileRecievedFromSys: $filePath   --$fileName")
    }

    private fun setData(filePath: String) {
        val imgFile = File(filePath)
        if (imgFile.exists()) {
            val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
            //val rotatedBitmap = rotateFileAsPerOrientation(filePath, myBitmap)
            if (null != myBitmap) {
                //myBitmap = setLatLongToBitmap(myBitmap)
                val base64Image = getEncoded64ImageStringFromBitmap(myBitmap)
                when (is_set) {
                    1 -> {
                        val card1PhotoPath = filePath
                        card1PicBytes = base64Image
                        binding.imageView1.setImageBitmap(myBitmap)
                        binding.imageView1.visible()
                        binding.imageView1.setOnClickListener {
                            viewImageFromURL(myBitmap)
                        }
                    }
                    2 -> {
                        val card21PhotoPath = filePath
                        card2PicBytes = base64Image
                        binding.imageView2.setImageBitmap(myBitmap)
                        binding.imageView2.visible()
                        binding.imageView2.setOnClickListener {
                            viewImageFromURL(myBitmap)
                        }
                    }
                    3 -> {
                        val card3PhotoPath = filePath
                        card3PicBytes = base64Image
                        binding.imageView3.setImageBitmap(myBitmap)
                        binding.imageView3.visible()
                        binding.imageView3.setOnClickListener {
                            viewImageFromURL(myBitmap)
                        }
                    }
                    4 -> {
                        val card4PhotoPath = filePath
                        card4PicBytes = base64Image
                        binding.imageView4.setImageBitmap(myBitmap)
                        binding.imageView4.visible()
                        binding.imageView4.setOnClickListener {
                            viewImageFromURL(myBitmap)
                        }
                    }
                    5 -> {
                        val card5PhotoPath = filePath
                        card5PicBytes = base64Image
                        binding.imageView5.setImageBitmap(myBitmap)
                        binding.imageView5.visible()
                        binding.imageView5.setOnClickListener {
                            viewImageFromURL(myBitmap)
                        }
                    }
                    6 -> {
                        val card6PhotoPath = filePath
                        card6PicBytes = base64Image
                        binding.imageView6.setImageBitmap(myBitmap)
                        binding.imageView6.visible()
                        binding.imageView6.setOnClickListener {
                            viewImageFromURL(myBitmap)
                        }
                    }
                }
            }
        }
    }

    private fun rotateFileAsPerOrientation(filePath: String, myBitmap: Bitmap): Bitmap {
        val ei = ExifInterface(filePath)
        val orientation: Int = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        val rotatedBitmap: Bitmap? = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(myBitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(myBitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(myBitmap, 270)
            ExifInterface.ORIENTATION_NORMAL -> myBitmap
            else -> myBitmap
        }

        return rotatedBitmap!!

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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
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

    override fun permissionsGranted() {
        showFileChooser()
    }

    override fun permissionsDenied() {
        Toast.makeText(context, "Permissions Denied!", Toast.LENGTH_SHORT).show()
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