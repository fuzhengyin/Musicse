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
package com.fuzhengyin.musicse.internal.entity;

import android.content.pm.ActivityInfo;
import android.provider.MediaStore;

import androidx.annotation.StyleRes;

import com.fuzhengyin.musicse.MimeType;
import com.fuzhengyin.musicse.filter.Filter;
import com.fuzhengyin.musicse.listener.OnCheckedListener;
import com.fuzhengyin.musicse.listener.OnSelectedListener;
import com.fuzhengyin.musicse.R;
import com.fuzhengyin.musicse.internal.model.SelectedItemCollection;

import java.util.List;
import java.util.Set;

public final class SelectionSpec {

    public Set<MimeType> mimeTypeSet;
    public boolean mediaTypeExclusive;
    public boolean showSingleMediaType;
    @StyleRes
    public int themeId;
    public int orientation;
    public boolean countable;
    public int maxSelectable;
    public boolean autoapplyIfMaxIsOne;
    public int maxImageSelectable;
    public int maxVideoSelectable;
    public List<Filter> filters;
    public boolean capture;
    public int spanCount;
    public int gridExpectedSize;
    public float thumbnailScale;
    public boolean hasInited;
    public OnSelectedListener onSelectedListener;
    public boolean originalable;
    public boolean autoHideToobar;
    public int originalMaxSize;
    public OnCheckedListener onCheckedListener;
    public boolean showPreview;
    public String orderBy;
    public String sortOrder;
    public boolean refresh;
    public boolean ignoreSizeNull;

    private SelectionSpec() {
    }

    public static SelectionSpec getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public static SelectionSpec getCleanInstance() {
        SelectionSpec selectionSpec = getInstance();
        selectionSpec.reset();
        return selectionSpec;
    }

    private void reset() {
        mimeTypeSet = null;
        mediaTypeExclusive = true;
        showSingleMediaType = false;
        themeId = R.style.Matisse_Zhihu;
        orientation = 0;
        countable = false;
        maxSelectable = 1;
        autoapplyIfMaxIsOne = false;
        maxImageSelectable = 0;
        maxVideoSelectable = 0;
        filters = null;
        capture = false;
        spanCount = 3;
        gridExpectedSize = 0;
        thumbnailScale = 0.5f;
        hasInited = true;
        originalable = false;
        autoHideToobar = false;
        originalMaxSize = Integer.MAX_VALUE;
        showPreview = true;
        orderBy = MediaStore.MediaColumns.DATE_ADDED;
        sortOrder = "DESC";
        refresh = false;
        ignoreSizeNull = false;
    }

    public boolean isSingleSelection(int collectionType) {
        if (collectionType == SelectedItemCollection.COLLECTION_MIXED) {
            return maxSelectable == 1;
        } else if (collectionType == SelectedItemCollection.COLLECTION_VIDEO) {
            return maxVideoSelectable == 1;
        } else if (collectionType == SelectedItemCollection.COLLECTION_IMAGE) {
            return maxImageSelectable == 1;
        } else {
            return false;
        }
    }

    public boolean singleSelectionModeEnabled(int collectionType) {
        return !countable && isSingleSelection(collectionType);
    }

    public boolean autoapplyModeEnabled(int collectionType) {
        return isSingleSelection(collectionType) && autoapplyIfMaxIsOne;
    }

    public boolean needOrientationRestriction() {
        return orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    private static final class InstanceHolder {
        private static final SelectionSpec INSTANCE = new SelectionSpec();
    }
}
