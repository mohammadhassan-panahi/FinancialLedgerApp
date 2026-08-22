package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.data.repository.BackupRepository
import com.example.data.repository.CryptoRepository
import com.example.data.repository.PortfolioRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.security.BiometricAuthManager
import com.example.security.PinManager
import com.example.ui.PortfolioApp
import com.example.ui.theme.FinancialLedgerTheme
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.ui.viewmodel.PortfolioViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : FragmentActivity() {

    private lateinit var viewModel: PortfolioViewModel
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var backupRepository: BackupRepository

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@registerForActivityResult
        lifecycleScope.launch {
            try {
                val json = backupRepository.exportToJson()
                contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                Toast.makeText(this@MainActivity, "پشتیبان با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "خطا در ذخیره‌ی پشتیبان: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        lifecycleScope.launch {
            try {
                val json = contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.readText()
                    ?: throw IllegalStateException("فایل قابل خواندن نیست")
                val count = backupRepository.importFromJson(json)
                Toast.makeText(this@MainActivity, "$count مورد با موفقیت بازیابی شد", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "خطا در بازیابی: فایل معتبر نیست", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        userPreferencesRepository = UserPreferencesRepository(applicationContext)

        // Encrypted Room DB (SQLCipher) — see data/local/AppDatabase.kt + security/DatabasePassphraseProvider.kt
        val database = AppDatabase.getDatabase(applicationContext)

        val repository = PortfolioRepository(
            purchaseDao = database.assetPurchaseDao(),
            saleDao = database.assetSaleDao(),
            marketDao = database.marketDao(),
            stockDao = database.stockDao(),
            alertDao = database.priceAlertDao(),
            apiKey = BuildConfig.BRSAPI_KEY
        )
        // Phase 1 of the crypto analysis feature (see project plan): market data only,
        // sourced from CoinMarketCap's free tier. Not yet wired to a ViewModel/UI — that's
        // the next step once this compiles and the data layer is confirmed working.
        @Suppress("UNUSED_VARIABLE")
        val cryptoRepository = CryptoRepository(
            cryptoDao = database.cryptoDao(),
            apiKey = BuildConfig.CMC_API_KEY
        )
        backupRepository = BackupRepository(
            purchaseDao = database.assetPurchaseDao(),
            saleDao = database.assetSaleDao(),
            stockDao = database.stockDao(),
            alertDao = database.priceAlertDao(),
            database = database
        )

        val factory = PortfolioViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[PortfolioViewModel::class.java]

        com.example.worker.PriceAlertScheduler.schedule(applicationContext)

        val biometricAuthManager = BiometricAuthManager(this)
        val pinManager = PinManager(applicationContext)

        setContent {
            FinancialLedgerTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PortfolioApp(
                        viewModel = viewModel,
                        userPreferencesRepository = userPreferencesRepository,
                        biometricAuthManager = biometricAuthManager,
                        pinManager = pinManager,
                        onExportRequested = {
                            val timestamp = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.US).format(Date())
                            exportLauncher.launch("portfolio-backup-$timestamp.json")
                        },
                        onImportRequested = { importLauncher.launch(arrayOf("application/json")) }
                    )
                }
            }
        }
    }
}
