package com.ifs21004.lostandfound.presentation.lostandfound

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.ifs21004.lostandfound.R
import com.ifs21004.lostandfound.data.model.DelcomLostandFound
import com.ifs21004.lostandfound.data.remote.MyResult
import com.ifs21004.lostandfound.helper.getImageUri
import com.ifs21004.lostandfound.helper.reduceFileImage
import com.ifs21004.lostandfound.helper.uriToFile
import com.ifs21004.lostandfound.presentation.ViewModelFactory
import com.ifs21004.lostandfound.databinding.ActivityLostandFoundManageBinding
import com.ifs21004.lostandfound.helper.Utils.Companion.observeOnce
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class LostandFoundManageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLostandFoundManageBinding
    private var currentImageUri: Uri? = null

    private val viewModel by viewModels<LostandFoundViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLostandFoundManageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAtion()
    }

    private fun setupView() {
        showLoading(false)
    }

    private fun setupAtion() {
        val isAddLostandFound = intent.getBooleanExtra(KEY_IS_ADD, true)
        if (isAddLostandFound) {
            manageAddLostandFound()
        } else {

            val delcomTodo = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    intent.getParcelableExtra(KEY_TODO, DelcomLostandFound::class.java)
                }

                else -> {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<DelcomLostandFound>(KEY_TODO)
                }
            }
            if (delcomTodo == null) {
                finishAfterTransition()
                return
            }
            manageEditLostandFound(delcomTodo)
        }

        binding.appbarTodoManage.setNavigationOnClickListener {
            finishAfterTransition()
        }
    }

    private fun manageAddLostandFound() {
        binding.apply {
            appbarTodoManage.title = "Tambah Todo"
            btnLostandFoundManageSave.setOnClickListener {
                val title = etLostandFoundManageTitle.text.toString()
                val description = etLostandFoundManageDesc.text.toString()
                val status = etLostandFoundManageStatus.selectedItem.toString()

                if (title.isEmpty() || description.isEmpty()) {
                    AlertDialog.Builder(this@LostandFoundManageActivity).apply {
                        setTitle("Oh No!")
                        setMessage("Tidak boleh ada data yang kosong!")
                        setPositiveButton("Oke") { _, _ -> }
                        create()
                        show()
                    }
                    return@setOnClickListener
                }
                observePostLostandFound(title, description, status)
            }
        }
    }

    private fun observePostLostandFound(title: String, description: String, status : String,) {
        viewModel.postLostandFound(title, description,status,).observeOnce { result ->
            when (result) {
                is MyResult.Loading -> {
                    showLoading(true)
                }
//                is MyResult.Success -> {
////                    showLoading(false)
////                    val resultIntent = Intent()
////                    setResult(RESULT_CODE, resultIntent)
////                    finishAfterTransition()
////                }
                is MyResult.Success -> {
                    if (currentImageUri != null) {
                        observeAddCoverLostandFound(result.data.lostFoundId)
                    } else {
                        showLoading(false)
                        val resultIntent = Intent()
                        setResult(RESULT_CODE, resultIntent)
                        finishAfterTransition()           }
                }
                is MyResult.Error -> {
                    AlertDialog.Builder(this@LostandFoundManageActivity).apply {
                        setTitle("Oh No!")
                        setMessage(result.error)
                        setPositiveButton("Oke") { _, _ -> }
                        create()
                        show()
                    }
                    showLoading(false)
                }
            }
        }
    }

    private fun manageEditLostandFound(lostandFound: DelcomLostandFound) {
        binding.apply {
            appbarTodoManage.title = "Ubah Barang"

            etLostandFoundManageTitle.setText(lostandFound.title)
            etLostandFoundManageDesc.setText(lostandFound.description)

            val statusArray = resources.getStringArray(R.array.status)
            val statusIndex = statusArray.indexOf(lostandFound.status)
            etLostandFoundManageStatus.setSelection(statusIndex)

            if (lostandFound.cover != null) {
                Glide.with(this@LostandFoundManageActivity)
                    .load(lostandFound.cover)
                    .placeholder(R.drawable.ic_image_24)
                    .into(ivTodoManageCover)
            }

            btnLostandFoundManageSave.setOnClickListener {
                val title = etLostandFoundManageTitle.text.toString()
                val description = etLostandFoundManageDesc.text.toString()
                val status = etLostandFoundManageStatus.selectedItem.toString()

                if (title.isEmpty() || description.isEmpty()) {
                    AlertDialog.Builder(this@LostandFoundManageActivity).apply {
                        setTitle("Oh No!")
                        setMessage("Tidak boleh ada data yang kosong!")
                        setPositiveButton("Oke") { _, _ -> }
                        create()
                        show()
                    }
                    return@setOnClickListener
                }

                observePutLostandFound(lostandFound.id, title, description, status, lostandFound.isCompleted)

            }
            btnTodoManageCamera.setOnClickListener {
                startCamera()
            }
            btnTodoManageGallery.setOnClickListener {
                startGallery()
            }
        }
    }
    private fun startGallery() {
        launcherGallery.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Toast.makeText(
                applicationContext,
                "Tidak ada media yang dipilih!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.ivTodoManageCover.setImageURI(it)
        }
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri)
    }
    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        }
    }

    private fun observePutLostandFound(
        lostandfoundId: Int,
        title: String,
        description: String,
        status: String,
        isCompleted: Boolean,
    ) {
        viewModel.putLostandFound(
            lostandfoundId,
            title,
            description,
            status,
            isCompleted,
        ).observeOnce { result ->
            when (result) {
                is MyResult.Loading -> {
                    showLoading(true)
                }
//                is MyResult.Success -> {
//                    showLoading(false)
//                    val resultIntent = Intent()
//                    setResult(RESULT_CODE, resultIntent)
//                    finishAfterTransition()
//                }
                is MyResult.Success -> {
                    if (currentImageUri != null) {
                        observeAddCoverLostandFound(lostandfoundId)
                    } else {
                        showLoading(false)
                        val resultIntent = Intent()
                        setResult(RESULT_CODE, resultIntent)
                        finishAfterTransition()
                    }
                }
                is MyResult.Error -> {
                    AlertDialog.Builder(this@LostandFoundManageActivity).apply {
                        setTitle("Oh No!")
                        setMessage(result.error)
                        setPositiveButton("Oke") { _, _ -> }
                        create()
                        show()
                    }
                    showLoading(false)
                }
            }
        }
    }

    private fun observeAddCoverLostandFound(
        lostandfoundId: Int
    ) {
        val imageFile =
            uriToFile(currentImageUri!!, this).reduceFileImage()
        val requestImageFile =
            imageFile.asRequestBody("image/jpeg".toMediaType())
        val reqPhoto =
            MultipartBody.Part.createFormData(
                "cover",
                imageFile.name,
                requestImageFile
            )
        viewModel.addCoverLostandFound(
            lostandfoundId,
            reqPhoto
        ).observeOnce { result ->
            when (result) {
                is MyResult.Loading -> {
                    showLoading(true)
                }
                is MyResult.Success -> { showLoading(false)
                    val resultIntent = Intent()
                    setResult(RESULT_CODE, resultIntent)
                    finishAfterTransition()
                }
                is MyResult.Error -> {
                    showLoading(false)
                    AlertDialog.Builder(this@LostandFoundManageActivity).apply {
                        setTitle("Oh No!")
                        setMessage(result.error)
                        setPositiveButton("Oke") { _, _ ->
                            val resultIntent = Intent()
                            setResult(RESULT_CODE, resultIntent)
                            finishAfterTransition()
                        }
                        setCancelable(false)
                        create()
                        show()
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbLostandFoundManage.visibility =
            if (isLoading) View.VISIBLE else View.GONE
        binding.btnLostandFoundManageSave.isActivated = !isLoading
        binding.btnLostandFoundManageSave.text =
            if (isLoading) "" else "Simpan"
    }

    companion object {
        const val KEY_IS_ADD = "is_add"
        const val KEY_TODO = "todo"
        const val RESULT_CODE = 1002
    }
}
