package com.example.stockapp.data.repository

import com.example.stockapp.data.local.StockDatabase
import com.example.stockapp.data.mapper.toCompanyListing
import com.example.stockapp.data.remote.StockApi
import com.example.stockapp.domain.model.CompanyListing
import com.example.stockapp.domain.repository.StockRepository
import com.example.stockapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

            emit(Resource.Loading(false))
        }
    }
}