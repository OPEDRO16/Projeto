package com.train.app.data

import com.train.app.data.models.ExerciseDifficulty
import com.train.app.data.models.ExerciseForce
import com.train.app.data.models.ExerciseLibraryItem

object ExerciseLibraryRepository {
    val exercises: List<ExerciseLibraryItem> = listOf(
        ExerciseLibraryItem(
            id = "barbell_bench_press",
            name = "Barbell Bench Press",
            primaryMuscle = "Chest",
            secondaryMuscles = listOf("Triceps", "Front Delts"),
            equipment = "Barbell",
            category = "Strength",
            force = ExerciseForce.PUSH,
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            instructions = listOf(
                "Set your eyes under the bar and plant your feet firmly.",
                "Retract your shoulder blades and keep your chest high.",
                "Lower the bar with control to the lower chest.",
                "Press back up while keeping wrists stacked over elbows."
            ),
            tips = listOf(
                "Do not bounce the bar off the chest.",
                "Use a spotter on heavy sets."
            ),
            videoUrl = "bench_press_demo"
        ),
        ExerciseLibraryItem(
            id = "incline_dumbbell_press",
            name = "Incline Dumbbell Press",
            primaryMuscle = "Upper Chest",
            secondaryMuscles = listOf("Triceps", "Front Delts"),
            equipment = "Dumbbells",
            category = "Strength",
            force = ExerciseForce.PUSH,
            difficulty = ExerciseDifficulty.BEGINNER,
            instructions = listOf(
                "Set the bench to a low incline.",
                "Start with dumbbells over the shoulders.",
                "Lower until elbows are slightly below torso level.",
                "Press up while keeping the chest lifted."
            ),
            tips = listOf(
                "Keep forearms vertical near the bottom.",
                "Do not let shoulders roll forward."
            ),
            videoUrl = "incline_db_press_demo"
        ),
        ExerciseLibraryItem(
            id = "machine_chest_press",
            name = "Machine Chest Press",
            primaryMuscle = "Chest",
            secondaryMuscles = listOf("Triceps", "Front Delts"),
            equipment = "Machine",
            category = "Hypertrophy",
            force = ExerciseForce.PUSH,
            difficulty = ExerciseDifficulty.BEGINNER,
            instructions = listOf(
                "Adjust seat so handles line up with mid chest.",
                "Keep shoulders pinned back against the pad.",
                "Press handles forward until arms are nearly straight.",
                "Return slowly to the stretch position."
            ),
            tips = listOf(
                "Avoid shrugging as you press.",
                "Keep tempo controlled."
            ),
            videoUrl = "machine_chest_press_demo"
        ),
        ExerciseLibraryItem(
            id = "lat_pulldown",
            name = "Lat Pulldown",
            primaryMuscle = "Lats",
            secondaryMuscles = listOf("Biceps", "Upper Back"),
            equipment = "Cable",
            category = "Strength",
            force = ExerciseForce.PULL,
            difficulty = ExerciseDifficulty.BEGINNER,
            instructions = listOf(
                "Sit tall and lock legs under the pads.",
                "Grip the bar just outside shoulder width.",
                "Pull elbows down toward the ribs.",
                "Return slowly to a full stretch overhead."
            ),
            tips = listOf(
                "Lead with elbows, not hands.",
                "Do not throw torso backward."
            ),
            videoUrl = "lat_pulldown_demo"
        ),
        ExerciseLibraryItem(
            id = "seated_cable_row",
            name = "Seated Cable Row",
            primaryMuscle = "Upper Back",
            secondaryMuscles = listOf("Lats", "Biceps", "Rear Delts"),
            equipment = "Cable",
            category = "Strength",
            force = ExerciseForce.PULL,
            difficulty = ExerciseDifficulty.BEGINNER,
            instructions = listOf(
                "Sit upright with chest proud.",
                "Reach forward for a stretch without rounding hard.",
                "Pull handle toward the lower ribs.",
                "Pause briefly and return with control."
            ),
            tips = listOf(
                "Keep shoulders down.",
                "Do not turn the row into a lean-back swing."
            ),
            videoUrl = "seated_row_demo"
        ),
        ExerciseLibraryItem(
            id = "barbell_row",
            name = "Barbell Row",
            primaryMuscle = "Upper Back",
            secondaryMuscles = listOf("Lats", "Rear Delts", "Biceps"),
            equipment = "Barbell",
            category = "Strength",
            force = ExerciseForce.PULL,
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            instructions = listOf(
                "Hinge at the hips and brace the core.",
                "Hold the bar just below the knees.",
                "Row toward the lower ribs.",
                "Lower under control without losing torso position."
            ),
            tips = listOf(
                "Keep the spine neutral.",
                "Avoid jerking the weight from the floor."
            ),
            videoUrl = "barbell_row_demo"
        ),
        ExerciseLibraryItem(
            id = "back_squat",
            name = "Back Squat",
            primaryMuscle = "Quads",
            secondaryMuscles = listOf("Glutes", "Core", "Adductors"),
            equipment = "Barbell",
            category = "Strength",
            force = ExerciseForce.LEGS,
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            instructions = listOf(
                "Set the bar across the upper back and brace hard.",
                "Break at the hips and knees together.",
                "Descend until depth is solid for your mobility.",
                "Drive up through the midfoot while keeping chest and hips rising together."
            ),
            tips = listOf(
                "Keep knees tracking over toes.",
                "Do not relax at the bottom."
            ),
            videoUrl = "back_squat_demo"
        ),
        ExerciseLibraryItem(
            id = "leg_press",
            name = "Leg Press",
            primaryMuscle = "Quads",
            secondaryMuscles = listOf("Glutes", "Hamstrings"),
            equipment = "Machine",
            category = "Strength",
            force = ExerciseForce.LEGS,
            difficulty = ExerciseDifficulty.BEGINNER,
            instructions = listOf(
                "Place feet shoulder width on the platform.",
                "Unlock the sled and lower with control.",
                "Descend until knees are comfortably bent.",
                "Press back up without slamming lockout."
            ),
            tips = listOf(
                "Keep lower back pressed into the pad.",
                "Avoid letting knees cave inward."
            ),
            videoUrl = "leg_press_demo"
        ),
        ExerciseLibraryItem(
            id = "romanian_deadlift",
            name = "Romanian Deadlift",
            primaryMuscle = "Hamstrings",
            secondaryMuscles = listOf("Glutes", "Lower Back"),
            equipment = "Barbell",
            category = "Strength",
            force = ExerciseForce.PULL,
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            instructions = listOf(
                "Start standing with the bar in your hands.",
                "Push hips back while keeping the bar close to the legs.",
                "Lower until hamstrings are fully loaded.",
                "Drive hips forward to stand tall again."
            ),
            tips = listOf(
                "Keep a slight knee bend.",
                "Do not round the lower back."
            ),
            videoUrl = "rdl_demo"
        ),
        ExerciseLibraryItem(
            id = "leg_extension",
            name = "Leg Extension",
            primaryMuscle = "Quads",
            secondaryMuscles = listOf(),
            equipment = "Machine",
            category = "Hypertrophy",
            force = ExerciseForce.LEGS,
            difficulty = ExerciseDifficulty.BEGINNER,
            instructions = listOf(
                "Adjust the back pad and ankle roller.",
                "Extend knees until legs are almost straight.",
                "Pause briefly at the top.",
                "Lower slowly to the start."
            ),
            tips = listOf(
                "Use control instead of momentum.",
                "Keep hips stable on the seat."
            ),
            videoUrl = "leg_extension_demo"
        ),
        ExerciseLibraryItem(
            id = "seated_leg_curl",
            name = "Seated Leg Curl",
            primaryMuscle = "Hamstrings",
            secondaryMuscles = listOf("Calves"),
            equipment = "Machine",
            category = "Hypertrophy",
            force = ExerciseForce.LEGS,
            difficulty = ExerciseDifficulty.BEGINNER,
            instructions = listOf(
                "Set knee joint in line with the machine pivot.",
                "Curl the pad down by driving heels back.",
                "Squeeze hamstrings at the contracted position.",
                "Return with control to full stretch."
            ),
            tips = listOf(
                "Keep hips anchored.",
                "Do not bounce at the bottom."
            ),
            videoUrl = "leg_curl_demo"
        ),
        ExerciseLibraryItem(
            id = "standing_calf_raise",
            name = "Standing Calf Raise",
            primaryMuscle = "Calves",
            secondaryMuscles = listOf(),
            equipment = "Machine",
            category = "Hypertrophy",
            force = ExerciseForce.LEGS,
            difficulty = ExerciseDifficulty.BEGINNER,
            instructions = listOf(
                "Place the balls of the feet on the platform.",
                "Drop heels for a full stretch.",
                "Push through the toes and rise high.",
                "Pause at the top before lowering."
            ),
            tips = listOf(
                "Use full range of motion.",
                "Do not rush reps."
            ),
            videoUrl = "calf_raise_demo"
        ),
        ExerciseLibraryItem(
            id = "overhead_press",
            name = "Overhead Press",
            primaryMuscle = "Shoulders",
            secondaryMuscles = listOf("Triceps", "Upper Chest"),
            equipment = "Barbell",
            category = "Strength",
            force = ExerciseForce.PUSH,
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            instructions = listOf(
                "Grip the bar just outside shoulder width.",
                "Brace glutes and core.",
                "Press overhead in a straight path.",
                "Lock out overhead with the bar over midfoot."
            ),
            tips = listOf(
                "Avoid excessive lower back extension.",
                "Move your head back then through."
            ),
            videoUrl = "ohp_demo"
        ),
        ExerciseLibraryItem(
            id = "lateral_raise",
            name = "Lateral Raise",
            primaryMuscle = "Side Delts",
            secondaryMuscles = listOf("Upper Traps"),
            equipment = "Dumbbells",
            category = "Hypertrophy",
            force = ExerciseForce.PUSH,
            difficulty = ExerciseDifficulty.BEGINNER,
            instructions = listOf(
                "Hold dumbbells by the sides.",
                "Raise arms out until around shoulder height.",
                "Keep elbows softly bent.",
                "Lower slowly without swinging."
            ),
            tips = listOf(
                "Lead slightly with elbows.",
                "Avoid shrugging hard."
            ),
            videoUrl = "lateral_raise_demo"
        ),
        ExerciseLibraryItem(
            id = "rear_delt_fly",
            name = "Rear Delt Fly",
            primaryMuscle = "Rear Delts",
            secondaryMuscles = listOf("Upper Back"),
            equipment = "Dumbbells",
            category = "Hypertrophy",
            force = ExerciseForce.PULL,
            difficulty = ExerciseDifficulty.BEGINNER,
            instructions = listOf(
                "Hinge forward with a neutral spine.",
                "Let arms hang with a slight elbow bend.",
                "Open arms wide until upper arms align with torso.",
                "Lower with control."
            ),
            tips = listOf(
                "Do not shrug hard.",
                "Use manageable loads."
            ),
            videoUrl = "rear_delt_fly_demo"
        ),
        ExerciseLibraryItem(
            id = "cable_triceps_pushdown",
            name = "Cable Triceps Pushdown",
            primaryMuscle = "Triceps",
            secondaryMuscles = listOf(),
            equipment = "Cable",
            category = "Hypertrophy",
            force = ExerciseForce.PUSH,
            difficulty = ExerciseDifficulty.BEGINNER,
            instructions = listOf(
                "Stand tall and pin elbows near the torso.",
                "Push the handle down by extending the elbows.",
                "Squeeze triceps at lockout.",
                "Return with control."
            ),
            tips = listOf(
                "Keep shoulders quiet.",
                "Do not let elbows drift forward."
            ),
            videoUrl = "triceps_pushdown_demo"
        ),
        ExerciseLibraryItem(
            id = "barbell_curl",
            name = "Barbell Curl",
            primaryMuscle = "Biceps",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Barbell",
            category = "Hypertrophy",
            force = ExerciseForce.PULL,
            difficulty = ExerciseDifficulty.BEGINNER,
            instructions = listOf(
                "Stand with bar in front of thighs.",
                "Curl the bar while keeping elbows close.",
                "Squeeze biceps at the top.",
                "Lower under full control."
            ),
            tips = listOf(
                "Avoid swinging torso.",
                "Use full elbow extension at the bottom."
            ),
            videoUrl = "barbell_curl_demo"
        ),
        ExerciseLibraryItem(
            id = "hammer_curl",
            name = "Hammer Curl",
            primaryMuscle = "Biceps",
            secondaryMuscles = listOf("Brachialis", "Forearms"),
            equipment = "Dumbbells",
            category = "Hypertrophy",
            force = ExerciseForce.PULL,
            difficulty = ExerciseDifficulty.BEGINNER,
            instructions = listOf(
                "Hold dumbbells with a neutral grip.",
                "Curl while keeping upper arm stable.",
                "Pause near the top.",
                "Lower slowly to full extension."
            ),
            tips = listOf(
                "Keep wrists neutral.",
                "Do not swing dumbbells."
            ),
            videoUrl = "hammer_curl_demo"
        ),
        ExerciseLibraryItem(
            id = "cable_crunch",
            name = "Cable Crunch",
            primaryMuscle = "Abs",
            secondaryMuscles = listOf("Obliques"),
            equipment = "Cable",
            category = "Core",
            force = ExerciseForce.CORE,
            difficulty = ExerciseDifficulty.BEGINNER,
            instructions = listOf(
                "Kneel facing the cable station.",
                "Hold rope by the sides of the head.",
                "Crunch down by curling the spine.",
                "Return slowly while keeping tension."
            ),
            tips = listOf(
                "Move through the torso, not the hips.",
                "Keep abs braced throughout."
            ),
            videoUrl = "cable_crunch_demo"
        ),
        ExerciseLibraryItem(
            id = "plank",
            name = "Plank",
            primaryMuscle = "Core",
            secondaryMuscles = listOf("Glutes", "Shoulders"),
            equipment = "Bodyweight",
            category = "Core",
            force = ExerciseForce.CORE,
            difficulty = ExerciseDifficulty.BEGINNER,
            instructions = listOf(
                "Place forearms under shoulders.",
                "Extend legs and squeeze glutes.",
                "Keep ribs down and spine neutral.",
                "Hold position while breathing calmly."
            ),
            tips = listOf(
                "Do not let hips sag.",
                "Push the floor away with forearms."
            ),
            videoUrl = "plank_demo"
        ),
        ExerciseLibraryItem(
            id = "pull_up",
            name = "Pull-up",
            primaryMuscle = "Lats",
            secondaryMuscles = listOf("Biceps", "Core", "Upper Back"),
            equipment = "Bodyweight",
            category = "Calisthenics",
            force = ExerciseForce.PULL,
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            instructions = listOf(
                "Grip the bar slightly wider than shoulder width.",
                "Hang with arms fully extended and pull shoulder blades down.",
                "Pull chest up to the bar by driving elbows down.",
                "Lower with control back to a dead hang."
            ),
            tips = listOf(
                "Avoid using leg momentum.",
                "Keep core tight throughout."
            ),
            videoUrl = "pull_up_demo"
        ),
        ExerciseLibraryItem(
            id = "dip",
            name = "Parallel Bar Dip",
            primaryMuscle = "Chest",
            secondaryMuscles = listOf("Triceps", "Front Delts"),
            equipment = "Bodyweight",
            category = "Calisthenics",
            force = ExerciseForce.PUSH,
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            instructions = listOf(
                "Support body on parallel bars with arms locked.",
                "Lower body by bending elbows, leaning slightly forward.",
                "Descend until shoulders are slightly below elbow height.",
                "Push back up to the starting position."
            ),
            tips = listOf(
                "Do not let shoulders roll forward.",
                "Control the descent speed."
            ),
            videoUrl = "dip_demo"
        ),
        ExerciseLibraryItem(
            id = "push_up",
            name = "Push-up",
            primaryMuscle = "Chest",
            secondaryMuscles = listOf("Triceps", "Front Delts", "Core"),
            equipment = "Bodyweight",
            category = "Calisthenics",
            force = ExerciseForce.PUSH,
            difficulty = ExerciseDifficulty.BEGINNER,
            instructions = listOf(
                "Start in a high plank position.",
                "Lower body until chest almost touches the floor.",
                "Keep elbows at a 45-degree angle to the body.",
                "Push back up to full extension."
            ),
            tips = listOf(
                "Do not let the lower back sag.",
                "Keep neck neutral."
            ),
            videoUrl = "push_up_demo"
        ),
        ExerciseLibraryItem(
            id = "pistol_squat",
            name = "Pistol Squat",
            primaryMuscle = "Quads",
            secondaryMuscles = listOf("Glutes", "Hamstrings", "Core"),
            equipment = "Bodyweight",
            category = "Calisthenics",
            force = ExerciseForce.LEGS,
            difficulty = ExerciseDifficulty.ADVANCED,
            instructions = listOf(
                "Stand on one leg, extending the other leg forward.",
                "Hinge at the hips and descend into a deep squat.",
                "Maintain balance and upright torso.",
                "Drive through the heel to stand back up."
            ),
            tips = listOf(
                "Keep core tight for stability.",
                "Use a wall for support if needed."
            ),
            videoUrl = "pistol_squat_demo"
        ),
        ExerciseLibraryItem(
            id = "l_sit",
            name = "L-Sit",
            primaryMuscle = "Abs",
            secondaryMuscles = listOf("Core", "Shoulders", "Hip Flexors"),
            equipment = "Bodyweight",
            category = "Calisthenics",
            force = ExerciseForce.CORE,
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            instructions = listOf(
                "Place hands on parallettes or the floor.",
                "Push shoulders down and lift hips off the ground.",
                "Extend legs forward parallel to the floor.",
                "Hold the position while breathing."
            ),
            tips = listOf(
                "Depress shoulders actively.",
                "Keep legs fully locked."
            ),
            videoUrl = "l_sit_demo"
        ),
        ExerciseLibraryItem(
            id = "muscle_up",
            name = "Muscle-up",
            primaryMuscle = "Lats",
            secondaryMuscles = listOf("Triceps", "Biceps", "Shoulders", "Chest"),
            equipment = "Bodyweight",
            category = "Calisthenics",
            force = ExerciseForce.PULL,
            difficulty = ExerciseDifficulty.ADVANCED,
            instructions = listOf(
                "Hang from a bar and initiate a powerful pull-up.",
                "Pull bar toward the lower chest and transition torso forward.",
                "Press body up until arms are fully locked out.",
                "Lower with control back to a hang."
            ),
            tips = listOf(
                "Requires both pull and push strength.",
                "Ensure grip is secure."
            ),
            videoUrl = "muscle_up_demo"
        ),
        ExerciseLibraryItem(
            id = "handstand_push_up",
            name = "Handstand Push-up",
            primaryMuscle = "Shoulders",
            secondaryMuscles = listOf("Triceps", "Core"),
            equipment = "Bodyweight",
            category = "Calisthenics",
            force = ExerciseForce.PUSH,
            difficulty = ExerciseDifficulty.ADVANCED,
            instructions = listOf(
                "Kick up into a handstand against a wall.",
                "Lower body with control until head touches the ground.",
                "Keep core tight and elbows tucked.",
                "Press forcefully back to start position."
            ),
            tips = listOf(
                "Keep body straight.",
                "Ensure shoulders are warm."
            ),
            videoUrl = "hspu_demo"
        )
    )

    fun getById(id: String): ExerciseLibraryItem? = exercises.firstOrNull { it.id == id }

    fun filterExercises(
        query: String,
        primaryMuscle: String,
        equipment: String,
        difficulty: String,
        category: String
    ): List<ExerciseLibraryItem> {
        return exercises.filter { exercise ->
            val matchesQuery = query.isBlank() ||
                    exercise.name.contains(query, ignoreCase = true) ||
                    exercise.primaryMuscle.contains(query, ignoreCase = true) ||
                    exercise.secondaryMuscles.any { it.contains(query, ignoreCase = true) } ||
                    exercise.equipment.contains(query, ignoreCase = true)

            val matchesMuscle = primaryMuscle == "All" || exercise.primaryMuscle == primaryMuscle
            val matchesEquipment = equipment == "All" || exercise.equipment == equipment
            val matchesDifficulty = difficulty == "All" || exercise.difficulty.name == difficulty
            val matchesCategory = category == "All" || exercise.category == category

            matchesQuery && matchesMuscle && matchesEquipment && matchesDifficulty && matchesCategory
        }
    }
}