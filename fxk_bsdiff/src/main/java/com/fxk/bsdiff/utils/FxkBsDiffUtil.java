package com.fxk.bsdiff.utils;

/**
 * @author fenxi
 * @date 2023/3/10
 * @time 9:52
 */
public class FxkBsDiffUtil {

    static {
        System.loadLibrary("bsdiff");
    }

    /**
     * 生成补丁包
     * @param newFilePath String 新文件的地址
     * @param oldFilePath String  旧文件的地址
     * @param patchFilePath String  生成的补丁文件地址
     * @return Int
     */
    public native int diff(String newFilePath,String oldFilePath,String patchFilePath);

    /**
     * 合并差分包
     * @param oldFilePath String 旧文件地址
     * @param patchFilePath String 补丁文件地址
     * @param combineFilePath String 合并后的新文件地址
     * @return Int
     */
    public native int patch(String oldFilePath,String patchFilePath,String combineFilePath);
}
