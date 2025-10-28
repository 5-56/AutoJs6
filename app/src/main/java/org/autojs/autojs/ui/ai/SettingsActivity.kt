package org.autojs.autojs.ui.ai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.autojs.autojs.R
import org.autojs.autojs.ai.llm.ModelFetcher
import org.autojs.autojs.ai.llm.Providers
import androidx.preference.ListPreference
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, XiheSettingsFragment())
            .commit()
    }
}

class XiheSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.xihe_preferences, rootKey)
        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val modelPref = findPreference<ListPreference>("xihe_llm_model")
        val providerPref = findPreference<ListPreference>("xihe_llm_provider")

        val fetcher = ModelFetcher()
        val scope = CoroutineScope(Dispatchers.IO)

        fun refreshModels() {
            val cfg = Providers.read(requireContext())
            if (cfg.apiKey.isNullOrBlank() || cfg.baseUrl.isNullOrBlank()) return
            scope.launch {
                val models = fetcher.fetch(cfg.provider, cfg.baseUrl, cfg.apiKey)
                if (models.isNotEmpty()) {
                    requireActivity().runOnUiThread {
                        modelPref?.entries = models.toTypedArray()
                        modelPref?.entryValues = models.toTypedArray()
                    }
                }
            }
        }

        providerPref?.setOnPreferenceChangeListener { _, _ ->
            refreshModels()
            true
        }

        listOf(
            "xihe_llm_api_key_google",
            "xihe_llm_api_key_deepseek",
            "xihe_llm_api_key_kimi",
            "xihe_llm_api_key_openrouter",
            "xihe_llm_api_key_zhipu",
        ).forEach { key ->
            findPreference<Preference>(key)?.setOnPreferenceChangeListener { _, _ ->
                refreshModels(); true
            }
        }

        // Initial fetch if available
        refreshModels()
    }
}
