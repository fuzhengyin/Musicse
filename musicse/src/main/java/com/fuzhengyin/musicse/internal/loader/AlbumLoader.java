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

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.loader.content.CursorLoader;

import com.fuzhengyin.musicse.internal.entity.Album;
import com.fuzhengyin.musicse.internal.entity.SelectionSpec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Load all albums (grouped by bucket_id) into a single cursor.
 */
public class AlbumLoader extends CursorLoader {

    private static final String COLUMN_BUCKET_ID = "bucket_id";
    private static final String COLUMN_BUCKET_DISPLAY_NAME = "bucket_display_name";
    public static final String COLUMN_URI = "uri";
    public static final String COLUMN_COUNT = "count";
    private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");
    private static final String[] COLUMNS = {
            MediaStore.Audio.AudioColumns.DISPLAY_NAME,
            MediaStore.Audio.AudioColumns._ID,
            COLUMN_BUCKET_ID,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.Audio.AudioColumns.MIME_TYPE,
            COLUMN_URI,
            COLUMN_COUNT};

    private static final String[] PROJECTION = {
            MediaStore.Audio.AudioColumns.DISPLAY_NAME,
            MediaStore.Audio.AudioColumns._ID,
            COLUMN_BUCKET_ID,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            "COUNT(*) AS " + COLUMN_COUNT};

    private static final String[] PROJECTION_29 = {
            MediaStore.Audio.AudioColumns.DISPLAY_NAME,
            MediaStore.Audio.AudioColumns._ID,
            COLUMN_BUCKET_ID,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE};

    // === params for showSingleMediaType: false ===
    private static final String SELECTION =
            "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + ignoreSizeNull()
                    + ") GROUP BY bucket_id";

    @NonNull
    private static String ignoreSizeNull() {
        if(SelectionSpec.getInstance().ignoreSizeNull) return " AND " + MediaStore.MediaColumns.SIZE + "> 0"; else return "";
    }

    private static final String SELECTION_29 =
            "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
                    + ignoreSizeNull();
    private static final String[] SELECTION_ARGS = {
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO),
    };
    // =============================================

    private static final String BUCKET_ORDER_BY = "datetaken DESC";

    private AlbumLoader(Context context, String selection, String[] selectionArgs) {
        super(
                context,
                QUERY_URI,
                beforeAndroidTen() ? PROJECTION : PROJECTION_29,
                selection,
                selectionArgs,
                BUCKET_ORDER_BY
        );
    }

    public static CursorLoader newInstance(Context context) {

        String selection = beforeAndroidTen() ? SELECTION : SELECTION_29;
        return new AlbumLoader(context, selection, SELECTION_ARGS);
    }

    @Override
    public Cursor loadInBackground() {
        Cursor albums = super.loadInBackground();
        MatrixCursor allAlbum = new MatrixCursor(COLUMNS);

        if (beforeAndroidTen()) {
            int totalCount = 0;
            Uri allAlbumCoverUri = null;
            MatrixCursor otherAlbums = new MatrixCursor(COLUMNS);
            if (albums != null) {
                while (albums.moveToNext()) {
                    long fileId = albums.getLong(
                            albums.getColumnIndex(MediaStore.Files.FileColumns._ID));
                    long bucketId = albums.getLong(
                            albums.getColumnIndex(COLUMN_BUCKET_ID));
                    String bucketDisplayName = albums.getString(
                            albums.getColumnIndex(COLUMN_BUCKET_DISPLAY_NAME));
                    String mimeType = albums.getString(
                            albums.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
                    Uri uri = getUri(albums);
                    int count = albums.getInt(albums.getColumnIndex(COLUMN_COUNT));

                    otherAlbums.addRow(new String[]{
                            Long.toString(fileId),
                            Long.toString(bucketId), bucketDisplayName, mimeType, uri.toString(),
                            String.valueOf(count)});
                    totalCount += count;
                }
                if (albums.moveToFirst()) {
                    allAlbumCoverUri = getUri(albums);
                }
            }

            allAlbum.addRow(new String[]{
                    Album.ALBUM_ID_ALL, Album.ALBUM_ID_ALL, Album.ALBUM_NAME_ALL, null,
                    allAlbumCoverUri == null ? null : allAlbumCoverUri.toString(),
                    String.valueOf(totalCount)});

            return new MergeCursor(new Cursor[]{allAlbum, otherAlbums});
        } else {
            int totalCount = 0;
            Uri allAlbumCoverUri = null;

            // Pseudo GROUP BY
            Map<Long, Long> countMap = new HashMap<>();
            if (albums != null) {
                while (albums.moveToNext()) {
                    long bucketId = albums.getLong(albums.getColumnIndex(COLUMN_BUCKET_ID));

                    Long count = countMap.get(bucketId);
                    if (count == null) {
                        count = 1L;
                    } else {
                        count++;
                    }
                    countMap.put(bucketId, count);
                }
            }

            MatrixCursor otherAlbums = new MatrixCursor(COLUMNS);
            if (albums != null) {
                if (albums.moveToFirst()) {
                    allAlbumCoverUri = getUri(albums);

                    Set<Long> done = new HashSet<>();

                    do {
                        long bucketId = albums.getLong(albums.getColumnIndex(COLUMN_BUCKET_ID));

                        if (done.contains(bucketId)) {
                            continue;
                        }

                        long fileId = albums.getLong(
                                albums.getColumnIndex(MediaStore.Files.FileColumns._ID));
                        String bucketDisplayName = albums.getString(
                                albums.getColumnIndex(COLUMN_BUCKET_DISPLAY_NAME));
                        String mimeType = albums.getString(
                                albums.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
                        Uri uri = getUri(albums);
                        String name = albums.getString(albums.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME));
                        long count = countMap.get(bucketId);

                        otherAlbums.addRow(new String[]{
                                name,
                                Long.toString(fileId),
                                Long.toString(bucketId),
                                bucketDisplayName,
                                mimeType,
                                uri.toString(),
                                String.valueOf(count)});
                        done.add(bucketId);

                        totalCount += count;
                    } while (albums.moveToNext());
                }
            }

            allAlbum.addRow(new String[]{
                    "All",
                    Album.ALBUM_ID_ALL,
                    Album.ALBUM_ID_ALL, Album.ALBUM_NAME_ALL, null,
                    allAlbumCoverUri == null ? null : allAlbumCoverUri.toString(),
                    String.valueOf(totalCount)});

            return new MergeCursor(new Cursor[]{allAlbum, otherAlbums});
        }
    }

    private static Uri getUri(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
        Uri contentUri = MediaStore.Files.getContentUri("external");
        return ContentUris.withAppendedId(contentUri, id);
    }

    @Override
    public void onContentChanged() {
        // FIXME a dirty way to fix loading multiple times
    }

    /**
     * @return 是否是 Android 10 （Q） 之前的版本
     */
    private static boolean beforeAndroidTen() {
        return android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q;
    }
}