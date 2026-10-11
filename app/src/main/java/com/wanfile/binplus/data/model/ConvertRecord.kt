package com.wanfile.binplus.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

enum class ConvertMode { RAW, ENCRYPT }

class Converters {
    @TypeConverter
    fun fromMode(mode: ConvertMode): String = mode.name

    @TypeConverter
    fun toMode(value: String): ConvertMode = ConvertMode.valueOf(value)

    @TypeConverter
    fun fromDirection(direction: ConvertDirection): String = direction.name

    @TypeConverter
    fun toDirection(value: String): ConvertDirection = ConvertDirection.valueOf(value)
}

enum class ConvertDirection { TO_BIN, FROM_BIN }

@Entity(tableName = "convert_records")
@TypeConverters(Converters::class)
data class ConvertRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originalName: String,
    val outputName: String,
    val originalPath: String,
    val outputPath: String,
    val sizeBytes: Long,
    val mode: ConvertMode,
    val direction: ConvertDirection,
    val timestamp: Long = System.currentTimeMillis()
) {
    val sizeText: String
        get() = when {
            sizeBytes < 1024 -> "${sizeBytes} B"
            sizeBytes < 1024 * 1024 -> String.format("%.1f KB", sizeBytes / 1024f)
            sizeBytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", sizeBytes / (1024f * 1024f))
            else -> String.format("%.2f GB", sizeBytes / (1024f * 1024f * 1024f))
        }
}
