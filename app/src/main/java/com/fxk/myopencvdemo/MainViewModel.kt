package com.fxk.myopencvdemo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.ToastUtils
import com.fxk.bsdiff.utils.FxkBsDiffUtil
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.system.measureTimeMillis

/**
 * @author fenxi
 * @date 2023/3/10
 * @time 13:47
 */
class MainViewModel: ViewModel() {

    /*异常处理*/
    private val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        LogUtils.e("异常了:${throwable.localizedMessage}")
        throwable.printStackTrace()
    }

    /*文件后缀名*/
    private val suffix = "apk"

    /*旧文件*/
    private val oldFile = File(PathUtils.getInternalAppFilesPath(), "old.${suffix}")

    /*新文件*/
    private val newFile = File(PathUtils.getInternalAppFilesPath(), "new.${suffix}")

    /*补丁文件*/
    private val patchFile = File(PathUtils.getInternalAppFilesPath(), "patch.${suffix}")

    /*合并后的文件*/
    private val combineFile = File(PathUtils.getInternalAppFilesPath(), "combine.${suffix}")

    /*生成补丁文件*/
    fun fileDiff() {
        viewModelScope.launch(exceptionHandler) {

            val measureTimeMillis = measureTimeMillis {
                withContext(Dispatchers.IO) {
                    if (!oldFile.exists() || !newFile.exists()) {
                        ToastUtils.showShort("对比包缺失")
                        return@withContext
                    }

                    /*生成补丁包，耗时操作，记得放在子线程  返回值 0表示成功*/
                    val result = FxkBsDiffUtil().diff(
                        newFile.absolutePath,//新文件path
                        oldFile.absolutePath,//旧文件path
                        patchFile.absolutePath//补丁文件path
                    )

                }
            }

            LogUtils.i("生成补丁文件耗时:${measureTimeMillis}")
            LogUtils.i("oldFileSize:${FileUtils.getSize(oldFile)}")
            LogUtils.i("newFileSize:${FileUtils.getSize(newFile)}")
            LogUtils.i("patchFileSize:${FileUtils.getSize(patchFile)}")

        }

    }

    /*合并补丁文件*/
    fun filePatch() {
        viewModelScope.launch(exceptionHandler) {
            val measureTimeMillis = measureTimeMillis {
                withContext(Dispatchers.IO) {
                    LogUtils.e(PathUtils.getExternalAppFilesPath())
                    if (!oldFile.exists() || !patchFile.exists()) {
                        ToastUtils.showShort("补丁文件或旧文件缺失")
                        return@withContext
                    }
                    /*合并补丁包，耗时操作，记得放在子线程  返回值 0表示成功*/
                    val result = FxkBsDiffUtil().patch(
                        oldFile.absolutePath,
                        patchFile.absolutePath,
                        combineFile.absolutePath
                    )
                }
            }
            LogUtils.i("合并补丁文件耗时:${measureTimeMillis}")
            LogUtils.i("newFile MD5:${FileUtils.getFileMD5ToString(newFile)}")
            LogUtils.i("combineFile MD5:${FileUtils.getFileMD5ToString(combineFile)}")

        }

    }
}