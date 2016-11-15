package com.zythem.popularmovies;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

public interface MostPopularColumns {
    @DataType(DataType.Type.INTEGER)
    @PrimaryKey
    @AutoIncrement
    String _ID = "_id";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String MOVIE_TITLE = "Title";

    @DataType(DataType.Type.TEXT)
    public static final String MOVIE_IMAGEPATH = "Imagepath";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String MOVIE_DATE = "Date";

    @DataType(DataType.Type.REAL)
    @NotNull
    public static final String MOVIE_RATING = "Rating";

    @DataType(DataType.Type.INTEGER)
    @NotNull
    public static final String MOVIE_ID = "Id";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String MOVIE_OVERVIEW = "Overview";

    @DataType(DataType.Type.TEXT)
    public static final String MOVIE_IMAGEPATH_2 = "Imagepath2";
}
