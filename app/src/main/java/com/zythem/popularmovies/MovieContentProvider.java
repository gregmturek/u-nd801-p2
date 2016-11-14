package com.zythem.popularmovies;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

@ContentProvider(authority = MovieContentProvider.AUTHORITY, database = MovieDatabase.class)
public final class MovieContentProvider {

    public static final String AUTHORITY = "com.zythem.MovieContentProvider";

    @TableEndpoint(table = MovieDatabase.MOVIES)
    public static class Movies {

        @ContentUri(
                path = "movies",
                type = "vnd.android.cursor.dir/movie",
                defaultSort = MovieColumns.MOVIE_TITLE + " ASC")
        public static final Uri MOVIES = Uri.parse("content://" + AUTHORITY + "/movies");

        @InexactContentUri(
                path = "movies" + "/#",
                name = "MOVIE_ID",
                type = "vnd.android.cursor.item/movie",
                whereColumn = MovieColumns._ID,
                pathSegment = 1)
        public static Uri withId(long id) {
            return Uri.parse("content://" + AUTHORITY + "/movies/" + id);
        }
    }
}
