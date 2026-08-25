package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
    
    private lateinit var exportLauncher: ActivityResultLauncher<String>
    private lateinit var importLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Registration MUST happen before or during onCreate
        exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
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

        importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
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

        enableEdgeToEdge()

        userPreferencesRepository = UserPreferencesRepository(applicationContext)
        val database = AppDatabase.getDatabase(applicationContext)

        val repository = PortfolioRepository(
            purchaseDao = database.assetPurchaseDao(),
            saleDao = database.assetSaleDao(),
            bankAccountDao = database.bankAccountDao(),
            marketDao = database.marketDao(),
            stockDao = database.stockDao(),
            alertDao = database.priceAlertDao(),
            debtCreditDao = database.debtCreditDao(),
            reminderDao = database.reminderDao(),
            goalDao = database.goalDao(),
            cryptoDao = database.cryptoDao(),
            apiKey = BuildConfig.BRSAPI_KEY
        )
        val cryptoRepository = CryptoRepository(
            cryptoDao = database.cryptoDao(),
            apiKey = BuildConfig.CMC_API_KEY
        )
        backupRepository = BackupRepository(
            purchaseDao = database.assetPurchaseDao(),
            saleDao = database.assetSaleDao(),
            stockDao = database.stockDao(),
            alertDao = database.priceAlertDao(),
            bankAccountDao = database.bankAccountDao(),
            database = database
        )

        val factory = PortfolioViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[PortfolioViewModel::class.java]

        val cryptoFactory = com.example.ui.viewmodel.CryptoViewModelFactory(cryptoRepository)
        val cryptoViewModel = ViewModelProvider(this, cryptoFactory)[com.example.ui.viewmodel.CryptoViewModel::class.java]

        val calculatorFactory = com.example.ui.viewmodel.CalculatorViewModelFactory(database.calculationHistoryDao())
        val calculatorViewModel = ViewModelProvider(this, calculatorFactory)[com.example.ui.viewmodel.CalculatorViewModel::class.java]

        com.example.worker.PriceAlertScheduler.schedule(applicationContext)

        val biometricAuthManager = BiometricAuthManager(this)
        val pinManager = PinManager(applicationContext)

        setContent {
            FinancialLedgerTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PortfolioApp(
                        viewModel = viewModel,
                        cryptoViewModel = cryptoViewModel,
                        calculatorViewModel = calculatorViewModel,
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
