package com.shakespeare.new_app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PlayResourcesActivity : AppCompatActivity() {

    private lateinit var adapter: PlayResourcesAdapter
    private lateinit var viewModel: PlayResourcesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_resources)

        val playCode = intent.getStringExtra(EXTRA_PLAY_CODE) ?: return
        val playName = intent.getStringExtra(EXTRA_PLAY_NAME) ?: playCode

        supportActionBar?.title = "$playName – resources"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val recyclerView = findViewById<RecyclerView>(R.id.resourcesRecyclerView)
        adapter = PlayResourcesAdapter { resource -> onResourceClicked(resource) }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel = ViewModelProvider(this)[PlayResourcesViewModel::class.java]

        viewModel.resources.observe(this) { resources ->
            adapter.submitList(resources)
            // if empty, show a subtle "Resources coming soon" message
        }

        viewModel.loadResources(playCode)
    }

    private fun onResourceClicked(resource: PlayResource) {
        val uri = Uri.parse(resource.url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        // No need to special-case YouTube vs Amazon; system will pick app or browser
        startActivity(intent)
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
