/*
 * Copyright (C) 2014 nohana, Inc.
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fuzhengyin.musicse.internal.utils;

import android.content.ContentResolver;
import android.content.Context;

import com.fuzhengyin.musicse.MimeType;
import com.fuzhengyin.musicse.filter.Filter;
import com.fuzhengyin.musicse.internal.entity.IncapableCause;
import com.fuzhengyin.musicse.internal.entity.Item;
import com.fuzhengyin.musicse.internal.entity.SelectionSpec;
import com.fuzhengyin.musicse.R;

public final class PhotoMetadataUtils {
    private PhotoMetadataUtils() {
        throw new AssertionError("oops! the utility class is about to be instantiated...");
    }


    public static IncapableCause isAcceptable(Context context, Item item) {
        if (!isSelectableType(context, item)) {
            return new IncapableCause(context.getString(R.string.error_file_type));
        }

        if (SelectionSpec.getInstance().filters != null) {
            for (Filter filter : SelectionSpec.getInstance().filters) {
                IncapableCause incapableCause = filter.filter(context, item);
                if (incapableCause != null) {
                    return incapableCause;
                }
            }
        }
        return null;
    }

    private static boolean isSelectableType(Context context, Item item) {
        if (context == null) {
            return false;
        }

        ContentResolver resolver = context.getContentResolver();
        for (MimeType type : SelectionSpec.getInstance().mimeTypeSet) {
            if (type.checkType(resolver, item)) {
                return true;
            }
        }
        return false;
    }
}
