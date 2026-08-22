package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores a saved result from any calculator screen (simple/compound interest, loan,
 * inflation, gold/fx, comparison, deposit) so the user can revisit past calculations.
 * Added as part of merging the Calculators module into the Credify Financial Ledger app.
 */
@Entity(tableName = "calculation_history")
data class CalculationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sectionKey: String, // "simple_interest", "compound", "loan", "deposit", "comparison", "inflation", "gold_fx"
    val title: String,
    val summary: String,
    val paramsJson: String, // stored JSON or pipe-separated params to restore form state
    val timestamp: Long = System.currentTimeMillis()
)
