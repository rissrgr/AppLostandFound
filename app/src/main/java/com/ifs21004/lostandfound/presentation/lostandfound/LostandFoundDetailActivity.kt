package com.ifs21004.lostandfound.presentation.lostandfound

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.ifs21004.lostandfound.R
import com.ifs21004.lostandfound.data.local.entity.DelcomLostandFoundEntity
import com.ifs21004.lostandfound.data.remote.MyResult
import com.ifs21004.lostandfound.presentation.ViewModelFactory
import com.ifs21004.lostandfound.data.model.DelcomLostandFound
import com.ifs21004.lostandfound.data.remote.response.LostandFoundResponse
import com.ifs21004.lostandfound.databinding.ActivityLostandFoundDetailBinding
import com.ifs21004.lostandfound.helper.Utils.Companion.observeOnce

class LostandFoundDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLostandFoundDetailBinding
    private val viewModel by viewModels<LostandFoundViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private var isChanged: Boolean = false
    private var isFavorite: Boolean = false
    private var delcomLostandFound: DelcomLostandFoundEntity? = null

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == LostandFoundManageActivity.RESULT_CODE) {
            recreate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLostandFoundDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()
    }

    private fun setupView() {
        showComponent(false)
        showLoading(false)
    }

    private fun setupAction() {
        val lostandFoundId = intent.getIntExtra(KEY_TODO_ID, 0)
        if (lostandFoundId == 0) {
            finish()
            return
        }

        observeGetLostandFound(lostandFoundId)

        binding.appbarLostandFoundDetail.setNavigationOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra(KEY_IS_CHANGED, isChanged)
            setResult(RESULT_CODE, resultIntent)
            finishAfterTransition()
        }
    }

    private fun observeGetLostandFound(lostandfoundId: Int) {
        viewModel.getLostandFound(lostandfoundId).observeOnce { result ->
            when (result) {
                is MyResult.Loading -> {
                    showLoading(true)
                }
                is MyResult.Success -> {
                    showLoading(false)
                    loadLostandFound(result.data.data.lostFound)
                }
                is MyResult.Error -> {
                    Toast.makeText(
                        this@LostandFoundDetailActivity,
                        result.error,
                        Toast.LENGTH_SHORT
                    ).show()
                    showLoading(false)
                    finishAfterTransition()
                }
            }
        }
    }

    private fun loadLostandFound(lostandfound: LostandFoundResponse?) {
        if (lostandfound != null) {
            showComponent(true)

            binding.apply {
                tvLostandFoundDetailTitle.text = lostandfound.title
                tvLostandFoundDetailDate.text = "Dibuat pada: ${lostandfound.createdAt}"
                tvLostandFoundDetailDesc.text = lostandfound.description

                val status = if (lostandfound.status.equals("found", ignoreCase = true)) {
                    highlight("Found", Color.BLUE)
                } else {
                    highlight("LOST", Color.RED)
                }

                tvLostandFoundDetailStatus.text = status

                if(lostandfound.cover != null){
                    ivTodoDetailCover.visibility = View.VISIBLE

                    Glide.with(this@LostandFoundDetailActivity)
                        .load(lostandfound.cover)
                        .placeholder(R.drawable.ic_image_24)
                        .into(ivTodoDetailCover)
                }else{
                    ivTodoDetailCover.visibility = View.VISIBLE
                }

                viewModel.getLocalLostFound(lostandfound.id).observeOnce {
                    if(it != null){
                        delcomLostandFound = it
                        setFavorite(true)
                    }else{
                        setFavorite(false)
                    }
                }

                ivLostandFoundDetailActionFavorite.setOnClickListener {
                    if(isFavorite){
                        setFavorite(false)
                        if(delcomLostandFound != null){
                            viewModel.deleteLocalLostFound(delcomLostandFound!!)
                        }
                        Toast.makeText(
                            this@LostandFoundDetailActivity,
                            "LostFound berhasil dihapus dari daftar favorite",
                            Toast.LENGTH_SHORT
                        ).show()
                    }else{
                        delcomLostandFound = DelcomLostandFoundEntity(
                            id = lostandfound.id,
                            title = lostandfound.title,
                            description = lostandfound.description,
                            isCompleted = lostandfound.isCompleted,
                            cover = lostandfound.cover,
                            createdAt = lostandfound.createdAt,
                            updatedAt = lostandfound.updatedAt,
                            status = "", // Anda perlu memberikan nilai default untuk status
                            userId = 0 // Anda perlu memberikan nilai default untuk userId
                        )

                        setFavorite(true)
                        viewModel.insertLocalLostFound(delcomLostandFound!!)
                        Toast.makeText(
                            this@LostandFoundDetailActivity,
                            "LostFound berhasil ditambahkan ke daftar favorite",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                cbLostandFoundDetailIsCompleted.isChecked = lostandfound.isCompleted == 1

                cbLostandFoundDetailIsCompleted.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.putLostandFound(
                        lostandfound.id,
                        lostandfound.title,
                        lostandfound.description,
                        lostandfound.status,
                        isChecked
                    ).observeOnce {
                        when (it) {
                            is MyResult.Error -> {
                                if (isChecked) {
                                    Toast.makeText(
                                        this@LostandFoundDetailActivity,
                                        "Gagal menyelesaikan data lost and found:  + ${lostandfound.title}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@LostandFoundDetailActivity,
                                        "Gagal batal menyelesaikan data lost and found: " + lostandfound.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            is MyResult.Success -> {
                                if (isChecked) {
                                    Toast.makeText(
                                        this@LostandFoundDetailActivity,
                                        "Berhasil menyelesaikan data lost and found: " + lostandfound.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@LostandFoundDetailActivity,
                                        "Berhasil batal menyelesaikan data lost and found: " + lostandfound.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                if ((lostandfound.isCompleted == 1) != isChecked) {
                                    isChanged = true
                                }
                            }

                            else -> {}
                        }
                    }
                }

                ivLostandFoundDetailActionDelete.setOnClickListener {
                    val builder = AlertDialog.Builder(this@LostandFoundDetailActivity)

                    // Menambahkan judul dan pesan pada dialog
                    builder.setTitle("Konfirmasi Hapus data lost and found")
                        .setMessage("Anda yakin ingin menghapus data lost and found ini?")

                    // Menambahkan tombol "Ya" dengan warna hijau
                    builder.setPositiveButton("Ya") { _, _ ->
                        observeDeleteLostandFound(lostandfound.id)
                    }

                    // Menambahkan tombol "Tidak" dengan warna merah
                    builder.setNegativeButton("Tidak") { dialog, _ ->
                        dialog.dismiss() // Menutup dialog
                    }

                    // Membuat dan menampilkan dialog
                    val dialog = builder.create()
                    dialog.show()

                    // Membuat kustomisasi warna teks pada tombol
                    val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    positiveButton.setTextColor(resources.getColor(R.color.green))
                    negativeButton.setTextColor(resources.getColor(R.color.red))
                }

                ivLostandFoundDetailActionEdit.setOnClickListener {
                    val delcomLostandFound = DelcomLostandFound(
                        lostandfound.id,
                        lostandfound.title,
                        lostandfound.description,
                        lostandfound.status,
                        lostandfound.isCompleted == 1,
                        lostandfound.cover
                    )

                    val intent = Intent(
                        this@LostandFoundDetailActivity,
                        LostandFoundManageActivity::class.java
                    )
                    intent.putExtra(LostandFoundManageActivity.KEY_IS_ADD, false)
                    intent.putExtra(LostandFoundManageActivity.KEY_TODO, delcomLostandFound)
                    launcher.launch(intent)
                }
            }
        }else {
            Toast.makeText(
                this@LostandFoundDetailActivity,
                "Tidak ditemukan item yang dicari",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun highlight(text: String, color: Int): SpannableString {
        val spannableString = SpannableString(text)
        spannableString.setSpan(ForegroundColorSpan(color), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }

    private fun setFavorite(status: Boolean){
        isFavorite = status
        if(status){
            binding.ivLostandFoundDetailActionFavorite
                .setImageResource(R.drawable.ic_favorite_24)
        }else{
            binding.ivLostandFoundDetailActionFavorite
                .setImageResource(R.drawable.ic_favorite_border_24)
        }
    }

    private fun observeDeleteLostandFound(lostandfoundId: Int) {
        showComponent(false)
        showLoading(true)

        viewModel.deleteLostandFound(lostandfoundId).observeOnce {
            when (it) {
                is MyResult.Error -> {
                    showComponent(true)
                    showLoading(false)
                    Toast.makeText(
                        this@LostandFoundDetailActivity,
                        "Gagal menghapus data lost and found: ${it.error}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is MyResult.Success -> {
                    showLoading(false)
                    Toast.makeText(
                        this@LostandFoundDetailActivity,
                        "Berhasil menghapus data lost and found",
                        Toast.LENGTH_SHORT
                    ).show()

                    val resultIntent = Intent()
                    resultIntent.putExtra(KEY_IS_CHANGED, true)
                    setResult(RESULT_CODE, resultIntent)
                    finishAfterTransition()
                }
                else -> {}
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbLostandFoundDetail.visibility =
            if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showComponent(status: Boolean) {
        binding.llLostandFoundDetail.visibility =
            if (status) View.VISIBLE else View.GONE
    }

    companion object {
        const val KEY_TODO_ID = "todo_id"
        const val KEY_IS_CHANGED = "is_changed"
        const val RESULT_CODE = 1001
    }
}
