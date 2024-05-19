package com.ifs21004.lostandfound.presentation.lostandfound

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifs21004.lostandfound.R
import com.ifs21004.lostandfound.adapter.LostandFoundsAdapter
import com.ifs21004.lostandfound.data.local.entity.DelcomLostandFoundEntity
import com.ifs21004.lostandfound.data.remote.MyResult
import com.ifs21004.lostandfound.helper.Utils.Companion.entitiesToResponses
import com.ifs21004.lostandfound.presentation.ViewModelFactory
import com.ifs21004.lostandfound.data.remote.response.LostFoundsItemResponse
import com.ifs21004.lostandfound.databinding.ActivityLostandFoundFavoriteBinding
import com.ifs21004.lostandfound.helper.Utils.Companion.observeOnce

class LostandFoundFavoriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLostandFoundFavoriteBinding
    private val viewModel by viewModels<LostandFoundViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == LostandFoundDetailActivity.RESULT_CODE) {
            result.data?.let {
                val isChanged = it.getBooleanExtra(
                    LostandFoundDetailActivity.KEY_IS_CHANGED,
                    false
                )
                if (isChanged) {
                    recreate()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLostandFoundFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        setupAction()
    }

    private fun setupAction() {
        binding.appbarLostFoundFavorite.setNavigationOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra(LostandFoundDetailActivity.KEY_IS_CHANGED, true)
            setResult(LostandFoundDetailActivity.RESULT_CODE, resultIntent)
            finishAfterTransition()
        }
    }

    private fun setupView() {
        showComponentNotEmpty(false)
        showEmptyError(false)
        showLoading(true)
        binding.appbarLostFoundFavorite.overflowIcon =
            ContextCompat
                .getDrawable(this, R.drawable.ic_more_vert_24)
        observeGetLostFounds()
    }

    private fun observeGetLostFounds() {
        viewModel.getLocalLostFounds().observe(this) { lostfounds ->
            loadLostFoundsToLayout(lostfounds)
        }
    }

    private fun loadLostFoundsToLayout(lostfounds: List<DelcomLostandFoundEntity>?) {
        showLoading(false)
        val layoutManager = LinearLayoutManager(this)
        binding.rvLostFoundFavoriteLostFounds.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(
            this,
            layoutManager.orientation
        )
        binding.rvLostFoundFavoriteLostFounds.addItemDecoration(itemDecoration)
        if (lostfounds.isNullOrEmpty()) {
            showEmptyError(true)
            binding.rvLostFoundFavoriteLostFounds.adapter = null
        } else {
            showComponentNotEmpty(true)
            showEmptyError(false)
            val adapter = LostandFoundsAdapter()
            adapter.submitOriginalList(entitiesToResponses(lostfounds))
            binding.rvLostFoundFavoriteLostFounds.adapter = adapter
            adapter.setOnItemClickCallback(
                object : LostandFoundsAdapter.OnItemClickCallback {
                    override fun onCheckedChangeListener(
                        lostfound: LostFoundsItemResponse,
                        isChecked: Boolean
                    ) {
                        adapter.filter(binding.svLostFoundFavorite.query.toString())
                        val newLostFound = DelcomLostandFoundEntity(
                            id = lostfound.id,
                            title = lostfound.title,
                            description = lostfound.description,
                            isCompleted = lostfound.isCompleted, // Sesuaikan dengan isCompleted
                            cover = lostfound.cover,
                            createdAt = lostfound.createdAt,
                            updatedAt = lostfound.updatedAt,
                            status = "", // Sesuaikan dengan nilai default untuk status
                            userId = 0 // Sesuaikan dengan nilai default untuk userId
                        )

                        viewModel.putLostandFound(
                            lostfound.id,
                            lostfound.title,
                            lostfound.description,
                            lostfound.status,
                            isChecked
                        ).observeOnce {
                            when (it) {
                                is MyResult.Error -> {
                                    if (isChecked) {
                                        Toast.makeText(
                                            this@LostandFoundFavoriteActivity,
                                            "Gagal menyelesaikan LostFound: " + lostfound.title,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            this@LostandFoundFavoriteActivity,
                                            "Gagal batal menyelesaikan LostFound: " + lostfound.title,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                is MyResult.Success -> {
                                    if (isChecked) {
                                        Toast.makeText(
                                            this@LostandFoundFavoriteActivity,
                                            "Berhasil menyelesaikan LostFound: " + lostfound.title,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            this@LostandFoundFavoriteActivity,
                                            "Berhasil batal menyelesaikan lostfound: " + lostfound.title,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    viewModel.insertLocalLostFound(newLostFound)
                                }

                                else -> {}
                            }
                        }
                    }

                    override fun onClickDetailListener(lostfoundId: Int) {
                        val intent = Intent(
                            this@LostandFoundFavoriteActivity,
                            LostandFoundDetailActivity::class.java
                        )
                        intent.putExtra(LostandFoundDetailActivity.KEY_TODO_ID, lostfoundId)
                        launcher.launch(intent)
                    }
                })
            binding.svLostFoundFavorite.setOnQueryTextListener(
                object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        adapter.filter(newText)
                        binding.rvLostFoundFavoriteLostFounds
                            .layoutManager?.scrollToPosition(0)

                        return true
                    }
                })
        }
    }

    private fun showComponentNotEmpty(status: Boolean) {
        binding.svLostFoundFavorite.visibility =
            if (status) View.VISIBLE else View.GONE
        binding.rvLostFoundFavoriteLostFounds.visibility =
            if (status) View.VISIBLE else View.GONE
    }

    private fun showEmptyError(isError: Boolean) {
        binding.tvLostFoundFavoriteEmptyError.visibility =
            if (isError) View.VISIBLE else View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbLostFoundFavorite.visibility =
            if (isLoading) View.VISIBLE else View.GONE
    }
}

