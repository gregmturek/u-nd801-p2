package com.zythem.popularmovies;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

@Database(version = MovieDatabase.VERSION)
public final class MovieDatabase {

    public static final int VERSION = 1;

    @Table(MostPopularColumns.class) public static final String MOST_POPULAR = "Most_Popular";
    @Table(TopRatedColumns.class) public static final String TOP_RATED = "Top_Rated";
    @Table(FavoriteColumns.class) public static final String FAVORITE = "Favorite";

}