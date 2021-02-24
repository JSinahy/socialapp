package com.laraguzman.tribalproofactivity.ui.main.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.laraguzman.tribalproofactivity.DetailPhotoActivity
import com.laraguzman.tribalproofactivity.ProfileActivity
import com.laraguzman.tribalproofactivity.data.api.UnsplashApinstance
import com.laraguzman.tribalproofactivity.data.api.UnsplashService
import com.laraguzman.tribalproofactivity.data.models.SearchPhotos
import com.laraguzman.tribalproofactivity.data.models.UnsplahPhotos
import com.laraguzman.tribalproofactivity.data.persistence.Preferences
import com.laraguzman.tribalproofactivity.ui.base.BaseApplication
import com.laraguzman.tribalproofactivity.ui.main.adapters.HomePhotoListener
import com.laraguzman.tribalproofactivity.ui.main.adapters.HomePhotosAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable

class HomeFragmentViewModel : ViewModel(), HomePhotoListener {
    var unsplashphotos : MutableLiveData<ArrayList<UnsplahPhotos>>
    var unsplashAdapter : HomePhotosAdapter
    var unsplashFavorites: ArrayList<UnsplahPhotos>? = null

    companion object {
        lateinit var prefs: Preferences
    }

    var searchText = MutableLiveData<String>()

    init{
        unsplashphotos = MutableLiveData()
        unsplashAdapter = HomePhotosAdapter(this)
        unsplashFavorites = ArrayList<UnsplahPhotos>()

        prefs = Preferences(BaseApplication.instance)
    }

    fun GetAdapter() : HomePhotosAdapter{
        return unsplashAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    fun SetAdapter(data: ArrayList<UnsplahPhotos>){
        unsplashAdapter.setDataList(data)
        unsplashAdapter.notifyDataSetChanged()
    }

    fun GetListUnsplashPhotos() : MutableLiveData<ArrayList<UnsplahPhotos>>{
        return unsplashphotos
    }

    fun GetPhotosFromApi(){
        val retrofitInstance = UnsplashApinstance().GetInstance().create(UnsplashService::class.java)
        val call = retrofitInstance.searchPhotos("<YOUR API KEY>", 1, 20)
        call.enqueue(object : Callback<ArrayList<UnsplahPhotos>> {
            override fun onResponse(
                call: Call<ArrayList<UnsplahPhotos>>,
                response: Response<ArrayList<UnsplahPhotos>>
            ) {
                if (response.isSuccessful) {
                    unsplashphotos.postValue(response.body())
                } else {
                    unsplashphotos.postValue(null)
                }
            }

            override fun onFailure(call: Call<ArrayList<UnsplahPhotos>>, t: Throwable) {
                //Log.wtf("DEVOLUCION", t.message)
                unsplashphotos.postValue(null)
            }

        })
    }

    fun GetPhotosFromApiSearch(search: String){
        val retrofitInstance = UnsplashApinstance().GetInstance().create(UnsplashService::class.java)
        val call = retrofitInstance.searchPhotos("<YOUR API KEY>", search, 1, 20)
        call.enqueue(object : Callback<SearchPhotos> {
            override fun onResponse(
                call: Call<SearchPhotos>,
                response: Response<SearchPhotos>
            ) {
                if (response.isSuccessful) {
                    unsplashphotos.postValue(response.body()?.results)
                } else {
                    unsplashphotos.postValue(null)
                }
            }

            override fun onFailure(call: Call<SearchPhotos>, t: Throwable) {
                //Log.wtf("DEVOLUCION", t.message)
                unsplashphotos.postValue(null)
            }

        })
    }

    override fun onClickPhoto(data: UnsplahPhotos, action: Int) {
        when(action) {
            2-> {
                val intent = Intent(BaseApplication.instance, ProfileActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("SALUDO", "")
                intent.putExtra("PROFILE", data as Serializable)
                BaseApplication.instance.startActivity(intent)
                //Log.wtf("PHOTO", data.user?.username)
            }
            1-> {
                Toast.makeText(BaseApplication.instance, data.user?.username, Toast.LENGTH_LONG).show()

                val intent = Intent(BaseApplication.instance, DetailPhotoActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("SALUDO", "")
                intent.putExtra("PROFILE", data as Serializable)
                BaseApplication.instance.startActivity(intent)
                //Log.wtf("PHOTO", data.user?.username)
            }
            3-> {

                if(prefs.name != ""){
                    val modelo : ArrayList<UnsplahPhotos>? = prefs.name?.let { GetDataFavorites(it) }
                    modelo?.add(data)
                    val datos : String = Gson().toJson(modelo)
                    prefs.name = datos
                    //Log.wtf("GUARDADO", prefs.name)
                    Toast.makeText(BaseApplication.instance, "Esta imagen se ha guardado en mis favoritos", Toast.LENGTH_LONG).show()
                }else{
                    unsplashFavorites?.add(data)
                    val datos : String = Gson().toJson(unsplashFavorites)
                    prefs.name = datos
                    //Log.wtf("GUARDADO", prefs.name)
                    Toast.makeText(BaseApplication.instance, "Esta imagen se ha guardado en mis favoritos", Toast.LENGTH_LONG).show()
                }

            }
        }
    }

    fun GetDataFavorites(data: String ) : ArrayList<UnsplahPhotos>?{
        //Log.wtf("CADENA", FavoritesFragmentViewModel.prefs.name)
        val listType = object : TypeToken<ArrayList<UnsplahPhotos?>?>() {}.type
        val modelo : ArrayList<UnsplahPhotos>? = Gson().fromJson(prefs.name, listType)
        return modelo
    }
}
