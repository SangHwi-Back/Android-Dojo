package com.example.moviceapp

import android.os.Bundle
import android.view.Menu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.moviceapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.main_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.navigationView.setupWithNavController(navController)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3009/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val movieService = retrofit.create(RetrofitAPIService::class.java)
        movieService.getMovies()
            .enqueue(object : Callback<Array<Movie>> {
                override fun onResponse(
                    call: Call<Array<Movie>?>,
                    response: Response<Array<Movie>?>
                ) {
                    if (response.isSuccessful && response.body().isNullOrEmpty().not()) {
                        print("Whoa!! ${response.body()!!.size}")
                    } else {
                        print("Hmmmm.....")
                    }
                }

                override fun onFailure(
                    p0: Call<Array<Movie>?>,
                    p1: Throwable
                ) {
                    print("What??? ${p1.toString()}")
                }
            })

//        navController.addOnDestinationChangedListener { _, destination, _ ->
//            when (destination.id) {
//
//            }
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar, menu)
        return true
    }
}