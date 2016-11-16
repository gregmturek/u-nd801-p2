package com.zythem.popularmovies;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

@ContentProvider(authority = MovieContentProvider.AUTHORITY, database = MovieDatabase.class)
public final class MovieContentProvider {

    public static final String AUTHORITY = "com.zythem.popularmovies.MovieContentProvider";

    interface Path{
        String MOST_POPULAR = "most_popular";
        String TOP_RATED = "top_rated";
        String FAVORITE = "favorite";
    }

    @TableEndpoint(table = MovieDatabase.MOST_POPULAR)
    public static class MostPopular {

        @ContentUri(
                path = Path.MOST_POPULAR,
                type = "vnd.android.cursor.dir/" + Path.MOST_POPULAR,
                defaultSort = MostPopularColumns._ID + " ASC")
        public static final Uri MOVIES = Uri.parse("content://" + AUTHORITY + "/" + Path.MOST_POPULAR);

        @InexactContentUri(
                path = Path.MOST_POPULAR + "/#",
                name = "MOVIE_ID",
                type = "vnd.android.cursor.item/" + Path.MOST_POPULAR,
                whereColumn = MostPopularColumns._ID,
                pathSegment = 1)
        public static Uri withId(long id) {
            return Uri.parse("content://" + AUTHORITY + "/" + Path.MOST_POPULAR + "/" + id);
        }
    }

    @TableEndpoint(table = MovieDatabase.TOP_RATED)
    public static class TopRated {

        @ContentUri(
                path = Path.TOP_RATED,
                type = "vnd.android.cursor.dir/" + Path.TOP_RATED,
                defaultSort = MostPopularColumns._ID + " ASC")
        public static final Uri MOVIES = Uri.parse("content://" + AUTHORITY + "/" + Path.TOP_RATED);

        @InexactContentUri(
                path = Path.TOP_RATED + "/#",
                name = "MOVIE_ID",
                type = "vnd.android.cursor.item/" + Path.TOP_RATED,
                whereColumn = MostPopularColumns._ID,
                pathSegment = 1)
        public static Uri withId(long id) {
            return Uri.parse("content://" + AUTHORITY + "/" + Path.TOP_RATED + "/" + id);
        }
    }

    @TableEndpoint(table = MovieDatabase.FAVORITE)
    public static class Favorite {

        @ContentUri(
                path = Path.FAVORITE,
                type = "vnd.android.cursor.dir/" + Path.FAVORITE,
                defaultSort = MostPopularColumns._ID + " ASC")
        public static final Uri MOVIES = Uri.parse("content://" + AUTHORITY + "/" + Path.FAVORITE);

        @InexactContentUri(
                path = Path.FAVORITE + "/#",
                name = "MOVIE_ID",
                type = "vnd.android.cursor.item/" + Path.FAVORITE,
                whereColumn = MostPopularColumns._ID,
                pathSegment = 1)
        public static Uri withId(long id) {
            return Uri.parse("content://" + AUTHORITY + "/" + Path.FAVORITE + "/" + id);
        }
    }


}
