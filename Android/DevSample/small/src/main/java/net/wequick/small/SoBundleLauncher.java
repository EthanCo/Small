/*
 * Copyright 2015-present wequick.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package net.wequick.small;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import net.wequick.small.util.SignUtils;

import java.io.File;

/**
 * This class resolve the bundle file with the extension of ".so".
 * 解析后缀为".so"的Bundle file
 *
 * <p>All the bundle files are built in at application lib path ({@code bundle.getBuiltinFile()}).
 *
 * <p>All the bundles can be upgraded after you download the patch file to
 * {@code bundle.getPatchFile()} and call {@code bundle.upgrade()}.
 *
 * There are two primary implements of this class:
 * <ul>
 *     <li>{@link ApkBundleLauncher} resolve the apk bundle</li>
 *     <li>{@link WebBundleLauncher} resolve the native web bundle</li>
 * </ul>
 */
public abstract class SoBundleLauncher extends BundleLauncher {

    /** Types that support */
    protected abstract String[] getSupportingTypes();

    @Override
    public boolean preloadBundle(Bundle bundle) {
        String packageName = bundle.getPackageName();
        if (packageName == null) return false;

        // Check if supporting
        // 检查是否是支持的类型
        String[] types = getSupportingTypes();
        if (types == null) return false;

        boolean supporting = false;
        for (String type : types) {
            if (packageName.contains("." + type + ".")) {
                supporting = true;
                break;
            }
        }
        if (!supporting) return false;

        // Check if has a patch
        //检查是否有patch
        File plugin = bundle.getPatchFile();
        PackageInfo pluginInfo = getPluginInfo(plugin);
        if (pluginInfo == null) {
            if (bundle.isPatching()) return false;

            plugin = bundle.getBuiltinFile();
            pluginInfo = getPluginInfo(plugin);
            if (pluginInfo == null) return false;
        }

        // Verify signatures
        //验证签名
        if (!SignUtils.verifyPlugin(pluginInfo)) {
            bundle.setEnabled(false);
            return true; // Got it, but disabled
        }

        // Record version code for upgrade
        //为了升级 记录版本
        bundle.setVersionCode(pluginInfo.versionCode);
        //存储到Sp
        Small.setBundleVersionCode(packageName, pluginInfo.versionCode);

        return true;
    }

    protected PackageInfo getPluginInfo(File plugin) {
        if (plugin == null || !plugin.exists()) return null;

        PackageManager pm = Small.getContext().getPackageManager();
        PackageInfo pluginInfo = pm.getPackageArchiveInfo(plugin.getPath(),
                PackageManager.GET_SIGNATURES);
        return pluginInfo;
    }
}
