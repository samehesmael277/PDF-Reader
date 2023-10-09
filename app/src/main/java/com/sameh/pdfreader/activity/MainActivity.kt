package com.sameh.pdfreader.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sameh.pdfreader.adapter.PDFsAdapter
import com.sameh.pdfreader.databinding.ActivityMainBinding
import com.sameh.pdfreader.extensions.showToast
import com.sameh.pdfreader.extensions.toLogD
import com.sameh.pdfreader.extensions.toLogE
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val pdfsAdapter: PDFsAdapter by lazy {
        PDFsAdapter()
    }

    private var currentPDFs: ArrayList<File> = ArrayList()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleRequestPermissions()
        setupPDFsAdapter()
        handleAdapterListener()
        handleSearchView()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                // You can proceed with your action here
                "Permission granted".toLogD()
                handleFetchPDFs()
            } else {
                // Permission denied
                // Handle the case where the user denied the permission
                "Permission denied".toLogE()
                "Permission denied".showToast(this)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun handleRequestPermissions() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                "You have permission".toLogD()
                handleFetchPDFs()
            } else {
                // Request for the permission
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        } else {
            // Below Android 11
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // You have permission
                "You have permission".toLogD()
                handleFetchPDFs()
            } else {
                // Request the permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun setupPDFsAdapter() {
        binding.apply {
            rvPdfs.adapter = pdfsAdapter
            rvPdfs.layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            rvPdfs.itemAnimator = SlideInUpAnimator().apply {
                addDuration = 200
            }
        }
    }

    private fun handleAdapterListener() {
        pdfsAdapter.onPDFClickListener {
            openPDFViewerActivity(it)
        }
    }

    private fun handleSearchView() {
        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    handleFilterSearchList(query)
                } else {
                    setAllPDFsToAdapter()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    handleFilterSearchList(newText)
                } else {
                    setAllPDFsToAdapter()
                }
                return true
            }
        })
    }

    private fun setAllPDFsToAdapter() {
        pdfsAdapter.setData(currentPDFs)
        showEmptyView(currentPDFs.isEmpty())
        binding.rvPdfs.scrollToPosition(0)
    }

    private fun handleFilterSearchList(text: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val filteredList = currentPDFs.filter {
                it.name.contains(text, ignoreCase = true)
            }
            withContext(Dispatchers.Main) {
                pdfsAdapter.setData(filteredList as ArrayList<File>)
                showEmptyView(filteredList.isEmpty())
                binding.rvPdfs.scrollToPosition(0)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun handleFetchPDFs() {
        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                onLoading(true)
                binding.progressBar.visibility = View.VISIBLE
            }

            val pdfs = fetchPDFs(Environment.getExternalStorageDirectory())
            pdfs.sortByDescending {
                it.lastModified()
            }
            currentPDFs = pdfs

            withContext(Dispatchers.Main) {
                pdfsAdapter.setData(pdfs)
                showEmptyView(pdfs.isEmpty())
                binding.progressBar.visibility = View.GONE
                onLoading(false)
            }
        }
    }

    private fun fetchPDFs(dir: File): ArrayList<File> {
        val pdfFiles = ArrayList<File>()
        if (dir.listFiles() != null) {
            for (file in dir.listFiles()!!) {
                if (file.isDirectory) {
                    pdfFiles.addAll(fetchPDFs(file))
                } else {
                    if (file.name.endsWith(".pdf")) {
                        pdfFiles.add(file)
                    }
                }
            }
        }
        return pdfFiles
    }

    private fun showEmptyView(show: Boolean) {
        if (show)
            binding.tvEmpty.visibility = View.VISIBLE
        else
            binding.tvEmpty.visibility = View.GONE
    }

    private fun onLoading(loading: Boolean = false) {
        when (loading) {
            true -> closeTouche()
            false -> openTouche()
        }
    }

    private fun closeTouche() {
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun openTouche() {
        this.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun openPDFViewerActivity(pdfFile: File) {
        val intent = Intent(this, PDFViewerActivity::class.java).apply {
            putExtra("pdfFile", pdfFile)
        }
        startActivity(intent)
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }
}