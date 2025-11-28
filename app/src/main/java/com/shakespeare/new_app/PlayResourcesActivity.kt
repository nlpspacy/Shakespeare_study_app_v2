package com.shakespeare.new_app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shakespeare.new_app.data.PlayResourcesRepository
import com.shakespeare.new_app.models.PlayResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayResourcesActivity : AppCompatActivity() {

    private lateinit var adapter: PlayResourcesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_resources)

        // Read inputs
        val playCode = intent.getStringExtra(EXTRA_PLAY_CODE) ?: return
        val playName = intent.getStringExtra(EXTRA_PLAY_NAME) ?: playCode

        // Toolbar title
        supportActionBar?.title = "$playName – resources"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // RecyclerView & adapter
        val recyclerView = findViewById<RecyclerView>(R.id.resourcesRecyclerView)
        adapter = PlayResourcesAdapter { resource -> onResourceClicked(resource) }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load data
        loadResources(playCode)
    }

    private fun loadResources(playCode: String) {
        lifecycleScope.launch {
            val resources = withContext(Dispatchers.IO) {
                PlayResourcesRepository.getResourcesForPlay(playCode)
            }

            // Convert PlayResource list into ResourceRow list
            val rows = buildRows(resources)
            adapter.submitList(rows)
        }
    }

    private fun buildRows(resources: List<PlayResource>): List<ResourceRow> {
        val rows = mutableListOf<ResourceRow>()

        val austi = resources.filter { it.isAusti }
        val others = resources.filter { !it.isAusti }

        if (austi.isNotEmpty()) {
            rows += ResourceRow.Header("From Austi Classics")
            rows += austi.map { ResourceRow.Item(it) }
        }

        if (others.isNotEmpty()) {
            rows += ResourceRow.Header("Recommended videos and editions")
            rows += others.map { ResourceRow.Item(it) }
        }

        return rows
    }

    private fun onResourceClicked(resource: PlayResource) {
        val uri = Uri.parse(resource.url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent) // System handles browser / YouTube / Amazon
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_PLAY_CODE = "extra_play_code"
        const val EXTRA_PLAY_NAME = "extra_play_name"
    }
}
