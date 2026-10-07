package com.example.intellipaat

import DashboardViewModelFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.intellipaat.data.AppDatabase
import com.example.intellipaat.domain.CourseEntity
import com.example.intellipaat.repository.CourseRepository
import com.example.intellipaat.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Database and Repository
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "learning-db").build()
        val repository = CourseRepository(db.courseDao(), db.lessonDao())

        setContent {
            MaterialTheme {
                val navController = rememberNavController()

                // Using a simple factory to pass repository to ViewModel
                val dashboardViewModel: DashboardViewModel = viewModel(
                    factory = DashboardViewModelFactory(repository)
                )

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(onLoginSuccess = {
                            navController.navigate("dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        })
                    }

                    composable("dashboard") {
                        DashboardScreen(
                            viewModel = dashboardViewModel,
                            onCourseClick = { courseId -> navController.navigate("details/$courseId") }
                        )
                    }

                    composable(
                        "details/{courseId}",
                        arguments = listOf(navArgument("courseId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val courseId = backStackEntry.arguments?.getInt("courseId") ?: 0
                        CourseDetailsScreen(
                            courseId = courseId,
                            viewModel = dashboardViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val viewModel: LoginViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) onLoginSuccess()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Learning App Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(16.dp))

        if (uiState is LoginUiState.Loading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = { viewModel.login(email, password) }) {
                Text("Login")
            }
        }

        if (uiState is LoginUiState.Error) {
            Text((uiState as LoginUiState.Error).message, color = Color.Red)
        }
    }
}

@Composable
fun DashboardScreen(viewModel: DashboardViewModel, onCourseClick: (Int) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { Text("My Courses", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge) }) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is DashboardUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is DashboardUiState.Success -> {
                    LazyColumn {
                        items(state.courses) { course ->
                            CourseItem(course, onCourseClick)
                        }
                    }
                }
                is DashboardUiState.Empty -> Text("No courses found", Modifier.align(Alignment.Center))
                is DashboardUiState.Error -> Text(state.message, Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun CourseItem(course: CourseEntity, onClick: (Int) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp), onClick = { onClick(course.id) }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(course.title, style = MaterialTheme.typography.titleMedium)
            Text("Instructor: ${course.instructor}")
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = course.progress / 100f, modifier = Modifier.fillMaxWidth())
            Text("${course.progress}% Completed")
        }
    }
}

@Composable
fun CourseDetailsScreen(courseId: Int, viewModel: DashboardViewModel, onBack: () -> Unit) {
    val lessons by viewModel.getLessonsForCourse(courseId).collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Course Lessons", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        LazyColumn {
            items(lessons) { lesson ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Checkbox(
                        checked = lesson.isCompleted,
                        onCheckedChange = { viewModel.toggleLesson(courseId, lesson.id, it) }
                    )
                    Text(lesson.title, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
        Button(onClick = onBack) { Text("Back") }
    }
}
