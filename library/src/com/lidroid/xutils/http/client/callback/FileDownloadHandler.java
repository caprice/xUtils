/*
 * Copyright (c) 2013. wyouflf (wyouflf@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lidroid.xutils.http.client.callback;

import android.text.TextUtils;

import com.lidroid.xutils.util.IOUtils;

import org.apache.http.HttpEntity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileDownloadHandler {

    public File handleEntity(HttpEntity entity,
                             RequestCallBackHandler callBackHandler,
                             String target,
                             boolean isResume,
                             String responseFileName) throws IOException {
        if (entity == null || TextUtils.isEmpty(target)) {
            return null;
        }

        File targetFile = new File(target);

        if (!targetFile.exists()) {
            targetFile.createNewFile();
        }

        long current = 0;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;

        try {

            if (isResume) {
                current = targetFile.length();
                fileOutputStream = new FileOutputStream(target, true);
            } else {
                fileOutputStream = new FileOutputStream(target);
            }

            long total = entity.getContentLength() + current;

            if (callBackHandler != null && !callBackHandler.updateProgress(total, current, true)) {
                return null;
            }


            inputStream = entity.getContent();
            BufferedInputStream bis = new BufferedInputStream(inputStream);

            byte[] tmp = new byte[4096];
            int len;
            while ((len = bis.read(tmp)) != -1) {
                fileOutputStream.write(tmp, 0, len);
                current += len;
                if (callBackHandler != null) {
                    if (!callBackHandler.updateProgress(total, current, false)) {
                        return targetFile;
                    }
                }
            }
            fileOutputStream.flush();
            if (callBackHandler != null) {
                callBackHandler.updateProgress(total, current, true);
            }
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(fileOutputStream);
        }

        if (targetFile.exists() && !TextUtils.isEmpty(responseFileName)) {
            File newFile = new File(targetFile.getParent(), responseFileName);
            targetFile.renameTo(newFile);
            return newFile;
        } else {
            return targetFile;
        }
    }

}
