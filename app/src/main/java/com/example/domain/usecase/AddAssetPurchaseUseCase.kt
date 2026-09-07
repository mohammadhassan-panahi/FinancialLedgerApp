package com.example.domain.usecase

import com.example.data.local.AssetPurchaseEntity
import com.example.data.local.PortfolioAssetType
import com.example.data.repository.PortfolioRepository

class AddAssetPurchaseUseCase(private val repository: PortfolioRepository) {
    suspend operator fun invoke(
        assetType: PortfolioAssetType,
        assetCode: String,
        assetName: String,
        quantity: Double,
        unitPriceRial: Double,
        purchaseDate: Long
    ) {
        repository.addPurchase(
            AssetPurchaseEntity(
                assetType = assetType,
                assetCode = assetCode,
                assetName = assetName,
                quantity = quantity,
                unitPriceRial = unitPriceRial,
                totalPaidRial = quantity * unitPriceRial,
                purchaseDate = purchaseDate
            )
        )
    }
}
