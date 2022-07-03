package com.example.stockapp.data.repository

import com.example.stockapp.data.local.StockDatabase
import com.example.stockapp.data.mapper.toCompanyListing
import com.example.stockapp.data.remote.StockApi
import com.example.stockapp.domain.model.CompanyListing
import com.example.stockapp.domain.repository.StockRepository
import com.example.stockapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    val api: StockApi,
    val db: StockDatabase
) : StockRepository {

    private val dao = db.dao

    override suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>> {
        return flow {
            //Start with loading as usual
            emit(Resource.Loading(true))
            //Get the listings from db
            val localListings = dao.searchCompanyListing(query)
            //Emit success
            emit(Resource.Success(
                //Map it to CompanyListings
                data = localListings.map { it.toCompanyListing() }
            ))
            //Need to check if the initial list is empty or the query is nonsense
            val isDbEmpty = localListings.isEmpty() && query.isBlank()
            //if it is not populated then we just want to load if from the cache
            val shouldLoadFromCache = !isDbEmpty && !fetchFromRemote
            if (shouldLoadFromCache) {
                emit(Resource.Loading(false))
                return@flow
            }
            val remoteListings = try {
                val response = api.getListings()
                response.byteStream()

            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error("Data cannot be loaded"))
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error("Check your internet connection"))

            }

        }
    }
}