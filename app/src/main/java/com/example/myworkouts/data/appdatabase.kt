package com.example.myworkouts.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myworkouts.data.dao.ExerciseDao
import com.example.myworkouts.data.dao.SetDao
import com.example.myworkouts.data.dao.WorkoutDao
import com.example.myworkouts.data.dao.WorkoutExerciseDao
import com.example.myworkouts.data.entity.ExerciseEntity
import com.example.myworkouts.data.entity.SetEntity
import com.example.myworkouts.data.entity.WorkoutEntity
import com.example.myworkouts.data.entity.WorkoutExerciseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ExerciseEntity::class, WorkoutEntity::class, WorkoutExerciseEntity::class, SetEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao
    abstract fun setDao(): SetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "myworkouts_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(PrepopulateCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class PrepopulateCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                checkAndPrepopulate()
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                checkAndPrepopulate()
            }

            private fun checkAndPrepopulate() {
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { instance ->
                        val dao = instance.exerciseDao()
                        dao.deleteDuplicates()
                        if (dao.getExerciseCount() == 0) {
                            populateInitialExercises(dao)
                        }
                    }
                }
            }

            private suspend fun populateInitialExercises(dao: ExerciseDao) {
                val defaultExercises = listOf(
                    // Abs
                    ExerciseEntity(name = "Crunch", category = "Abs", exerciseType = "REPS_ONLY", isCustom = false),
                    ExerciseEntity(name = "Sit-Up", category = "Abs", exerciseType = "REPS_ONLY", isCustom = false),
                    ExerciseEntity(name = "Hanging Leg Raise", category = "Abs", exerciseType = "REPS_ONLY", isCustom = false),
                    ExerciseEntity(name = "Leg Raise", category = "Abs", exerciseType = "REPS_ONLY", isCustom = false),
                    ExerciseEntity(name = "Cable Crunch", category = "Abs", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Plank", category = "Abs", exerciseType = "DURATION", isCustom = false),

                    // Back
                    ExerciseEntity(name = "Deadlift", category = "Back", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Barbell Row", category = "Back", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Dumbbell Row", category = "Back", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Lat Pulldown", category = "Back", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Pull-Up", category = "Back", exerciseType = "BODYWEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Seated Cable Row", category = "Back", exerciseType = "WEIGHT_REPS", isCustom = false),

                    // Biceps
                    ExerciseEntity(name = "Barbell Curl", category = "Biceps", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Dumbbell Curl", category = "Biceps", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Hammer Curl", category = "Biceps", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Preacher Curl", category = "Biceps", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Cable Curl", category = "Biceps", exerciseType = "WEIGHT_REPS", isCustom = false),

                    // Cardio
                    ExerciseEntity(name = "Running", category = "Cardio", exerciseType = "DISTANCE_DURATION", isCustom = false),
                    ExerciseEntity(name = "Treadmill", category = "Cardio", exerciseType = "DISTANCE_DURATION", isCustom = false),
                    ExerciseEntity(name = "Cycling", category = "Cardio", exerciseType = "DISTANCE_DURATION", isCustom = false),
                    ExerciseEntity(name = "Rowing", category = "Cardio", exerciseType = "DISTANCE_DURATION", isCustom = false),
                    ExerciseEntity(name = "Elliptical", category = "Cardio", exerciseType = "DURATION", isCustom = false),
                    ExerciseEntity(name = "Walking", category = "Cardio", exerciseType = "DISTANCE_DURATION", isCustom = false),

                    // Chest
                    ExerciseEntity(name = "Bench Press", category = "Chest", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Incline Bench Press", category = "Chest", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Decline Bench Press", category = "Chest", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Dumbbell Bench Press", category = "Chest", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Incline Dumbbell Press", category = "Chest", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Chest Fly", category = "Chest", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Cable Crossover", category = "Chest", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Push-Up", category = "Chest", exerciseType = "BODYWEIGHT_REPS", isCustom = false),

                    // Legs
                    ExerciseEntity(name = "Squat", category = "Legs", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Front Squat", category = "Legs", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Leg Press", category = "Legs", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Leg Extension", category = "Legs", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Leg Curl", category = "Legs", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Romanian Deadlift", category = "Legs", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Lunges", category = "Legs", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Calf Raise", category = "Legs", exerciseType = "WEIGHT_REPS", isCustom = false),

                    // Shoulders
                    ExerciseEntity(name = "Overhead Press", category = "Shoulders", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Dumbbell Shoulder Press", category = "Shoulders", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Lateral Raise", category = "Shoulders", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Front Raise", category = "Shoulders", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Rear Delt Fly", category = "Shoulders", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Face Pull", category = "Shoulders", exerciseType = "WEIGHT_REPS", isCustom = false),

                    // Triceps
                    ExerciseEntity(name = "Tricep Pushdown", category = "Triceps", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Skull Crusher", category = "Triceps", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Overhead Tricep Extension", category = "Triceps", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Close-Grip Bench Press", category = "Triceps", exerciseType = "WEIGHT_REPS", isCustom = false),
                    ExerciseEntity(name = "Dumbbell Tricep Extension", category = "Triceps", exerciseType = "WEIGHT_REPS", isCustom = false)
                )
                dao.insertAll(defaultExercises)
            }
        }
    }
}