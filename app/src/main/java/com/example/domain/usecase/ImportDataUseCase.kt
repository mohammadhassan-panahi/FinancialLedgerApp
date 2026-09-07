package com.example.domain.usecase

import com.example.data.repository.BackupRepository

class ImportDataUseCase(private val backupRepository: BackupRepository) {
    suspend operator fun invoke(json: String): Int = backupRepository.importFromJson(json)
}
