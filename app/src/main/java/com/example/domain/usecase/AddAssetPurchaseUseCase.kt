package com.example.domain.usecase

import com.example.data.local.AssetPurchaseEntity
import com.example.data.local.PortfolioAssetType
import com.example.data.repository.PortfolioRepository
import java.math.BigDecimal

class AddAssetPurchaseUseCase(private val repository: PortfolioRepository) {
    suspend operator fun invoke(
        assetType: PortfolioAssetType,
        assetCode: String,
        assetName: String,
        quantity: BigDecimal,
        unitPriceRial: BigDecimal,
        purchaseDate: Long
    ) {
        repository.addPurchase(
            AssetPurchaseEntity(
                assetType = assetType,
                assetCode = assetCode,
                assetName = assetName,
                quantity = quantity,
                unitPriceRial = unitPriceRial,
                totalPaidRial = quantity.multiply(unitPriceRial),
                purchaseDate = purchaseDate
            )
        )
    }
}
