package org.autojs.autojs.ui.ai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.autojs.autojs.R

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        try {
            val rvId = resources.getIdentifier("recyclerView", "id", packageName)
            if (rvId != 0) {
                val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(rvId)
                rv.layoutManager = LinearLayoutManager(this)
            }
        } catch (_: Throwable) {}
    }
}

package org.autojs.autojs.ui.ai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.autojs.autojs.R

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        // Minimal sanity: try to find views when present
        try {
            val rvId = resources.getIdentifier("recyclerView", "id", packageName)
            if (rvId != 0) {
                val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(rvId)
                rv.layoutManager = LinearLayoutManager(this)
            }
        } catch (_: Throwable) {}
    }
}
