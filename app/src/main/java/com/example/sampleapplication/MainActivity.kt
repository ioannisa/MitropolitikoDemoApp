package com.example.sampleapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.sampleapplication.network.MoviesService
import com.example.sampleapplication.ui.theme.SampleApplicationTheme
import com.example.sampleapplication.utils.Movie
import com.example.sampleapplication.utils.MoviesResponse
import com.example.sampleapplication.utils.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class MainActivity : ComponentActivity() {

    private val viewModel: MoviesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SampleApplicationTheme {
                    Navigation(viewModel = viewModel)

                    // A surface container using the 'background' color from the theme
//                    Surface(
//                        modifier = Modifier.fillMaxSize(),
//                        color = MaterialTheme.colorScheme.background
//                    ) {
//                        MoviesListScreen(paddingValues, viewModel = viewModel)
//                    }

                }

        }
    }
}

@Composable
fun Navigation(viewModel: MoviesViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MoviesListScreen(viewModel = viewModel, navController = navController) }

        composable("detail/{movieId}") { navBackStackEntry ->
            val movieId = navBackStackEntry.arguments?.getString("movieId")?.toInt()

//            movieId?.let {
//                MovieDetailsScreen(viewModel = viewModel, movieId = it, navController = navController)
//            }

            movieId?.let {
                MovieDetailsScreen(viewModel = viewModel, movieId = it, onNavigateUp = { navController.navigateUp() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar(title: String? = null, onBackPress: (() -> Unit)? = null) {
    title?.let { barTitle ->
        TopAppBar(
            title = { Text(barTitle, color = MaterialTheme.colorScheme.onPrimaryContainer) },

            navigationIcon = {
                 onBackPress?.let {
                     IconButton(onClick = it) {
                         Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                     }
                 }
            },

            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
fun MoviesListScreen(viewModel: MoviesViewModel, navController: NavHostController) {

    //val context = LocalContext.current

    Scaffold(
        topBar = {
            MyTopAppBar(title = "Popular Movies")
        }
    ) { paddingValues ->
        // Collecting the StateFlow from the ViewModel
        val movies = viewModel.movies.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()

        Box {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        start = paddingValues.calculateStartPadding(layoutDirection = LocalLayoutDirection.current),
                        end = paddingValues.calculateEndPadding(layoutDirection = LocalLayoutDirection.current),
                        bottom = paddingValues.calculateBottomPadding()
                    )
            ) {

                items(
                    items = movies.value,
                    key = { it.id }
                ) { movie ->
                    ListItem({
                        RowItem(movie = movie, modifier = Modifier
                            .clickable {
                                navController.navigate("detail/${movie.id}")

//                                Toast
//                                    .makeText(context, "Click ${movie.title}", Toast.LENGTH_LONG)
//                                    .show()
                            }
                            .background(MaterialTheme.colorScheme.surface)
                            .fillMaxWidth())

                        //.padding(horizontal = 16.dp, vertical = 8.dp))
                    })
                }
            }
            
            LoadingIndicator(isLoading = isLoading)
        }
        
    }
}

@Composable
//fun MovieDetailsScreen(viewModel: MoviesViewModel, movieId: Int, navController: NavHostController) {
fun MovieDetailsScreen(viewModel: MoviesViewModel, movieId: Int, onNavigateUp: () -> Unit) {
    val movie by viewModel.selectedMovie.collectAsState()

    Scaffold(
        topBar = {
            //MyTopAppBar(title = movie?.title, onBackPress = { navController.navigateUp() })
            MyTopAppBar(title = movie?.title, onBackPress = { onNavigateUp.invoke() })
        }
    ) { paddingValues ->

        // obtain the movie from view model when movieId changes
        LaunchedEffect(movieId) {
            viewModel.selectMovie(movieId)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = paddingValues.calculateStartPadding(layoutDirection = LocalLayoutDirection.current),
                    end = paddingValues.calculateEndPadding(layoutDirection = LocalLayoutDirection.current),
                    bottom = paddingValues.calculateBottomPadding()
                )
                .verticalScroll(rememberScrollState())
        ) {
            // simply show the obtained movieId from the arguments
            //Text("Hello Movie Detail $movieId")
            movie?.let { selectedMovie ->
                ImageLoader(
                    imagePath = selectedMovie.poster_path,
                    size = NetworkUtils.ImageSize.LARGE
                )
                Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(selectedMovie.title, fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(" ${selectedMovie.overview}")
                }

            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    val viewModel: MoviesViewModel(Application())
//
//    SampleApplicationTheme {
//        MoviesListScreen(viewModel = viewModel)
//    }
//}

@Composable
fun RowItem(movie: Movie, modifier: Modifier) {
    Card(modifier = modifier) {
        Row {
            ImageLoader(imagePath = movie.poster_path, size = NetworkUtils.ImageSize.SMALL)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier
                .align(Alignment.Top)
                .heightIn(min = 120.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(8.dp),
                    maxLines = 3

                )

                Text(
                    text = movie.release_date.substring(0, 4),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}


@Composable
fun ImageLoader(imagePath: String?, size: NetworkUtils.ImageSize, scale: ContentScale = ContentScale.Crop) {
    val imagePainter = if (!imagePath.isNullOrEmpty()) {
        val imageUrl = NetworkUtils.getImageUrl(imagePath, size)
        val filterQuality = if (size == NetworkUtils.ImageSize.LARGE) { FilterQuality.High } else { FilterQuality.Low }
        rememberAsyncImagePainter( imageUrl, filterQuality = filterQuality)

    } else {
        painterResource(R.drawable.ic_launcher_foreground)
    }

    val imageModifier: Modifier =
        if (size == NetworkUtils.ImageSize.LARGE) {
            Modifier
                .fillMaxWidth() // Make the image fill the width
                .heightIn(max = 400.dp) // Set the maximum height to 400dp
        }
        else {
            Modifier.size(120.dp)
        }

    Image(
        painter = imagePainter,
        contentDescription = null,
        contentScale = scale,
        modifier = imageModifier

    )
}

@Composable
fun LoadingIndicator(isLoading: Boolean) {
    if (isLoading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f))
        ) {
            CircularProgressIndicator()
        }
    }
}

class MoviesViewModel(): ViewModel() {

    private val repository = MoviesRepository()


    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _selectedMovie = MutableStateFlow<Movie?>(null)
    val selectedMovie: StateFlow<Movie?> = _selectedMovie

    init {
        //loadMoviesFromFile()
        loadMoviesFromNetwork()
    }

    private fun loadMoviesFromNetwork() {
        viewModelScope.launch {
            _isLoading.value = true
            _movies.value = repository.getMoviesFromNetwork()
            _isLoading.value = false
        }
    }

    fun selectMovie(movieId: Int) {
        _selectedMovie.value = getMovieById(movieId)
    }

    private fun getMovieById(movieId: Int): Movie? {
        return _movies.value.firstOrNull { it.id == movieId }
    }
}

class MoviesRepository() {

    suspend fun getMoviesFromNetwork(): List<Movie> {
        val response: Response<MoviesResponse> = try {
            MoviesService.api.getMoviesOnline()
        } catch (e: IOException) {
            Log.e("", "IOException: Check your internet connection: $e")
            return emptyList()
        }
        catch (e: HttpException) {
            Log.e("", "HttpException: Unexpected Result: $e")
            return emptyList()
        }

        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.results
        }
        return emptyList()
    }
}


