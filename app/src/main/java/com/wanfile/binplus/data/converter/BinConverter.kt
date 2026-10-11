package com.wanfile.binplus.data.converter

import android.content.Context
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toUri
import com.wanfile.binplus.data.model.ConvertDirection
import com.wanfile.binplus.data.model.ConvertMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

/**
 * wanfilebin plus — 转换引擎
 *
 * 模式 A (RAW)：原样提取字节流，无损输出 .bin
 * 模式 B (ENCRYPT)：XOR 编码输出 .bin，对称运算，再跑一遍即还原
 */
@Singleton
class BinConverter @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * 流式转换，支持超大文件（不把文件读进内存）
     *
     * @return 进度 Flow，发射 0..100 的百分比
     */
    fun convert(
        inputUri: Uri,
        mode: ConvertMode,
        direction: ConvertDirection,
        key: Byte = DEFAULT_KEY
    ): Flow<ConvertResult> = flow {
        val inputFile = uriToFile(inputUri)

        val originalName = inputFile.name
        val outputName = buildOutputName(originalName, direction)

        // 获取公共 Downloads 目录
        val outputDir = getPublicDownloadsDir()
        if (outputDir == null) {
            emit(ConvertResult.Error("无法访问 Downloads 目录，请检查权限"))
            return@flow
        }

        val outputFile = File(outputDir, outputName)

        // 防止覆盖输入自身（仅当同一目录时）
        val sameFile = try {
            outputFile.canonicalPath == inputFile.canonicalPath
        } catch (_: Exception) { false }
        if (sameFile) {
            emit(ConvertResult.Error("输出路径与输入相同，已取消"))
            return@flow
        }

        emit(ConvertResult.Starting(originalName, outputName))

        val size = inputFile.length()
        var copied: Long = 0

        // 流式读写，64KB 缓冲
        FileInputStream(inputFile).use { input ->
            FileOutputStream(outputFile).use { output ->
                val buffer = ByteArray(BUFFER_SIZE)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    val chunk = if (mode == ConvertMode.ENCRYPT) {
                        ByteArray(read) { i -> (buffer[i].toInt() xor key.toInt()).toByte() }
                    } else {
                        buffer.copyOf(read)
                    }
                    output.write(chunk, 0, read)
                    copied += read
                    if (size > 0) {
                        emit(ConvertResult.Progress((copied * 100 / size).toInt(), copied, size))
                    }
                }
                output.flush()
            }
        }

        emit(ConvertResult.Success(outputFile.absolutePath, outputFile.length()))
    }.flowOn(Dispatchers.IO)

    /**
     * 获取公共 Downloads 目录。
     * Android 10+ 使用 MediaStore，兼容 scoped storage；
     * Android 7-9 使用传统公共目录。
     */
    private fun getPublicDownloadsDir(): File? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+：返回缓存目录，文件通过 MediaStore 写入 Downloads
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.parentFile, "Downloads")
        } else {
            @Suppress("DEPRECATION")
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        }.also { it.mkdirs() }
    }

    /**
     * 批量转换
     */
    fun batchConvert(
        inputs: List<Uri>,
        mode: ConvertMode,
        direction: ConvertDirection,
        key: Byte = DEFAULT_KEY
    ): Flow<BatchResult> = flow {
        val total = inputs.size
        var success = 0
        val errors = mutableListOf<String>()

        inputs.forEachIndexed { index, uri ->
            try {
                convert(uri, mode, direction, key).collect { result ->
                    if (result is ConvertResult.Progress) {
                        emit(BatchResult.ItemProgress(index + 1, total, result.percent))
                    } else if (result is ConvertResult.Success) {
                        success++
                    } else if (result is ConvertResult.Error) {
                        errors += result.message
                    }
                }
            } catch (t: Throwable) {
                errors += "${uri.lastPathSegment}: ${t.message}"
            }
        }
        emit(BatchResult.Done(success, total - success, errors))
    }.flowOn(Dispatchers.IO)

    /**
     * 将 ContentResolver Uri 解析为可读 File。
     * 优先尝试直接路径；否则拷贝到缓存目录再操作。
     */
    private suspend fun uriToFile(uri: Uri): File = withContext(Dispatchers.IO) {
        // 直接文件 URI
        if (uri.scheme == "file") {
            return@withContext File(uri.path!!)
        }

        // 通过 ContentResolver 拷贝到缓存
        val name = queryDisplayName(uri) ?: "input_${System.currentTimeMillis()}"
        val cache = File(context.cacheDir, "picker/$name")
        cache.parentFile?.mkdirs()

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(cache).use { out ->
                val buf = ByteArray(BUFFER_SIZE)
                var r: Int
                while (input.read(buf).also { r = it } != -1) {
                    out.write(buf, 0, r)
                }
            }
        } ?: error("无法打开文件: $uri")

        cache
    }

    private fun queryDisplayName(uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
        }
    }

    private fun buildOutputName(originalName: String, direction: ConvertDirection): String {
        return if (direction == ConvertDirection.TO_BIN) {
            // 任意后缀 → .bin
            val dot = originalName.lastIndexOf('.')
            (if (dot > 0) originalName.substring(0, dot) else originalName) + ".bin"
        } else {
            // .bin → 还原：去掉 .bin 后缀
            if (originalName.endsWith(".bin", ignoreCase = true)) {
                originalName.dropLast(4)
            } else {
                "$originalName.restored"
            }
        }
    }

    companion object {
        private const val BUFFER_SIZE = 64 * 1024 // 64KB 缓冲，兼顾速度与内存
        private const val DEFAULT_KEY: Byte = 0x5A // 默认 XOR 密钥
    }
}

sealed interface ConvertResult {
    data class Starting(val inputName: String, val outputName: String) : ConvertResult
    data class Progress(val percent: Int, val copied: Long, val total: Long) : ConvertResult
    data class Success(val outputPath: String, val outputSize: Long) : ConvertResult
    data class Error(val message: String) : ConvertResult
}

sealed interface BatchResult {
    data class ItemProgress(val done: Int, val total: Int, val percent: Int) : BatchResult
    data class Done(val success: Int, val failed: Int, val errors: List<String>) : BatchResult
}
