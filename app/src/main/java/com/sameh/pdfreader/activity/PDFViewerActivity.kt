package com.sameh.pdfreader.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.sameh.pdfreader.databinding.ActivityPdfviewerBinding
import com.sameh.pdfreader.extensions.serializable
import java.io.File

class PDFViewerActivity : AppCompatActivity() {

    private var _binding: ActivityPdfviewerBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPdfviewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleIntentViewer()
        handleGetIntentFromHome()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun handleIntentViewer() {
        val intent = intent
        if (intent.action == Intent.ACTION_VIEW) {
            val pdfUri = intent.data
            if (pdfUri != null)
                loadPDFViewer(pdfUri)
        }
    }

    private fun handleGetIntentFromHome() {
        val intent = intent
        val pdfFile = intent.serializable<File>("pdfFile")
        if (pdfFile != null)
            loadPDFViewer(pdfFile)
    }

    private fun loadPDFViewer(pdfFile: File) {
        binding.pdfView.fromFile(pdfFile)
            .scrollHandle(DefaultScrollHandle(this, false))
            .enableSwipe(true) // allows to block changing pages using swipe
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .defaultPage(0)
            .pageSnap(true)
            .autoSpacing(true)
            .pageFling(true)
            .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
            .password(null)
            .enableAntialiasing(true) // improve rendering a little bit on low-res screens
            // spacing between pages in dp. To define spacing color, set view background
            .spacing(1)
            .autoSpacing(false) // add dynamic spacing to fit each page on its own on the screen
            .pageFitPolicy(FitPolicy.WIDTH) // mode to fit pages in the view
            .fitEachPage(false) // fit each page to the view, else smaller pages are scaled relative to largest page.
            .pageSnap(false) // snap pages to screen boundaries
            .pageFling(false) // make a fling change only a single page like ViewPager
            .nightMode(false) // toggle night mode
            .load()
    }

    private fun loadPDFViewer(pdfUri: Uri) {
        binding.pdfView.fromUri(pdfUri)
            .scrollHandle(DefaultScrollHandle(this, false))
            .enableSwipe(true) // allows to block changing pages using swipe
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .defaultPage(0)
            .pageSnap(true)
            .autoSpacing(true)
            .pageFling(true)
            .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
            .password(null)
            .enableAntialiasing(true) // improve rendering a little bit on low-res screens
            // spacing between pages in dp. To define spacing color, set view background
            .spacing(1)
            .autoSpacing(false) // add dynamic spacing to fit each page on its own on the screen
            .pageFitPolicy(FitPolicy.WIDTH) // mode to fit pages in the view
            .fitEachPage(false) // fit each page to the view, else smaller pages are scaled relative to largest page.
            .pageSnap(false) // snap pages to screen boundaries
            .pageFling(false) // make a fling change only a single page like ViewPager
            .nightMode(false) // toggle night mode
            .load()
    }
}