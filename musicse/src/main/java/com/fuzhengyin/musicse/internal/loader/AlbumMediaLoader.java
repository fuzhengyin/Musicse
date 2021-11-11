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
package com.fuzhengyin.musicse.internal.loader;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.loader.content.CursorLoader;

import com.fuzhengyin.musicse.internal.entity.Album;
import com.fuzhengyin.musicse.internal.entity.SelectionSpec;

/**
 * Load images and videos into a single cursor.
 */
public class AlbumMediaLoader extends CursorLoader {
    private static final Uri QUERY_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private static final String[] PROJECTION = {
            MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.ALBUM_ID,
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.SIZE,
            "duration"};

    @NonNull
    private static String ignoreSizeNull(boolean removeAnd) {
        if(SelectionSpec.getInstance().ignoreSizeNull) return  (removeAnd?"":" AND ") + MediaStore.MediaColumns.SIZE + "> 0"; else return "";
    }

    private static final String[] SELECTION_ALL_ARGS = {
    };
    // ===========================================================

    // === params for album ALL && showSingleMediaType: true ===
    private static final String SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE =
                    ignoreSizeNull(true);


    // === params for ordinary album && showSingleMediaType: false ===
    private static final String SELECTION_ALBUM =
                    " bucket_id=?"
                    + ignoreSizeNull(false);

    private static String[] getSelectionAlbumArgs(String albumId) {
        return new String[]{
                albumId
        };
    }

    private AlbumMediaLoader(Context context, String selection, String[] selectionArgs) {
        super(context, QUERY_URI, PROJECTION, selection, selectionArgs, SelectionSpec.getInstance().orderBy+" "+SelectionSpec.getInstance().sortOrder);
    }

    public static CursorLoader newInstance(Context context, Album album) {
        String selection;
        String[] selectionArgs;

        if (album.isAll()) {
            selection = SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE;
            selectionArgs = SELECTION_ALL_ARGS;
        } else {
            selection = SELECTION_ALBUM;
            selectionArgs = getSelectionAlbumArgs(album.getId());
        }
        return new AlbumMediaLoader(context, selection, selectionArgs);
    }

}
