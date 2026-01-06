package com.simats.eathmover.retrofit;

import com.simats.eathmover.models.NominatimResult;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NominatimApiService {
    @GET("search")
    Call<List<NominatimResult>> searchPoints(
        @Query("q") String query,
        @Query("format") String format,
        @Query("addressdetails") int addressDetails,
        @Query("limit") int limit
    );
}
