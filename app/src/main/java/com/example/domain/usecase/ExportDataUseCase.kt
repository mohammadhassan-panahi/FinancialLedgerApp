package com.example.domain.usecase

import com.example.data.repository.BackupRepository

class ExportDataUseCase(private val backupRepository: BackupRepository) {
    suspend operator fun invoke(): String = backupRepository.exportToJson()
}
