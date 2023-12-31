package com.example.mystorysubmission.ui.add

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.mystorysubmission.R
import com.example.mystorysubmission.databinding.ActivityAddBinding
import com.example.mystorysubmission.helper.ViewModelFactory
import com.example.mystorysubmission.helper.reduceFileImage
import com.example.mystorysubmission.helper.uriToFile
import com.example.mystorysubmission.model.StoryUploadResponse
import com.example.mystorysubmission.network.ApiConfig
import com.example.mystorysubmission.ui.camera.CameraActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

class AddActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityAddBinding
    private lateinit var factory: ViewModelFactory
    private  val addViewModel: AddViewModel by viewModels { factory }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latLng: LatLng? = null
    private var getFile: File? = null

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    resources.getString(R.string.permission_failed),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        factory = ViewModelFactory.getInstance(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        addViewModel.getUser().observe(this) {
            addViewModel.token = it.token
        }

        addViewModel.loading.observe(this) {
            showLoading(it)
        }

        binding.btnCamera.setOnClickListener(this)
        binding.btnGallery.setOnClickListener(this)
        binding.btnUpload.setOnClickListener(this)
        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getMyLastLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getMyLastLocation() {
        if (allPermissionsGranted()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latLng = LatLng(location.latitude, location.longitude)
                } else {
                    binding.switchLocation.isEnabled = false
                    Toast.makeText(
                        this@AddActivity,
                        getString(R.string.location_unknown),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                getMyLastLocation()
            }

            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                getMyLastLocation()
            }

            else -> {
                Toast.makeText(
                    this@AddActivity,
                    getString(R.string.permission_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.title = resources.getString(R.string.new_story)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_camera -> {
                val intent = Intent(this, CameraActivity::class.java)
                launcherIntentCameraX.launch(intent)
            }
            R.id.btn_gallery -> {
                val intent = Intent().also {
                    it.action = Intent.ACTION_GET_CONTENT
                    it.type = "image/*"
                }

                val chooser =
                    Intent.createChooser(intent, resources.getString(R.string.choose_image))
                launcherIntentGallery.launch(chooser)
            }
            R.id.btn_upload -> {
                uploadImage()
            }
        }
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = it.data?.getSerializableExtra("picture") as File
            val result = BitmapFactory.decodeFile(myFile.path)


            getFile = myFile

            binding.ivNewStory.setImageBitmap(result)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val selectedImg: Uri = it.data?.data as Uri
            val myFile = uriToFile(selectedImg, this@AddActivity)

            getFile = myFile

            binding.ivNewStory.setImageURI(selectedImg)
        }
    }

    private fun uploadImage() {
        if (getFile != null && binding.etDescription.text.toString() != "") {
            addViewModel.loading.value = true

            val file = reduceFileImage(getFile as File)
            val description =
                binding.etDescription.text.toString().toRequestBody("text/plain".toMediaType())
            val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part =
                MultipartBody.Part.createFormData("photo", file.name, requestImageFile)

            val service = ApiConfig.getApiService()
                .uploadStories("Bearer ${addViewModel.token}", imageMultipart, description, latLng?.latitude, latLng?.longitude)
            service.enqueue(object : Callback<StoryUploadResponse> {
                override fun onResponse(
                    call: Call<StoryUploadResponse>,
                    response: Response<StoryUploadResponse>
                ) {
                    addViewModel.loading.value = false

                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null) {
                            if (!responseBody.error) {
                                showMessage(true, responseBody.message)
                            } else {
                                showMessage(
                                    false,
                                    resources.getString(R.string.camera_failed_value) + " ${response.message()}"
                                )
                            }
                        }
                    } else {
                        showMessage(
                            false,
                            resources.getString(R.string.camera_failed_value) + " ${response.message()}"
                        )
                    }
                }

                override fun onFailure(call: Call<StoryUploadResponse>, t: Throwable) {
                    addViewModel.loading.value = false
                    showMessage(
                        false,
                        resources.getString(R.string.camera_failed_value) + " ${t.message}"
                    )
                }
            })
        } else {
            showMessage(false, resources.getString(R.string.upload_image_empty))
        }
    }

    private fun showMessage(success: Boolean, message: String) {
        val title = if (success) R.string.camera_success_title else R.string.camera_failed_title

        AlertDialog.Builder(this).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton(R.string.ok) { _, _ ->
                if (success) {
                    finish()
                }
            }
            create()
            show()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        with(binding) {
            if (isLoading) {
                pbLoading.visibility = View.VISIBLE
                bgLoading.visibility = View.VISIBLE
            } else {
                pbLoading.visibility = View.INVISIBLE
                bgLoading.visibility = View.INVISIBLE
            }
        }
    }

    companion object {
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}