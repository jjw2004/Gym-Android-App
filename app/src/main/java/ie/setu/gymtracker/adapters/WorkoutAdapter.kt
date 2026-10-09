package ie.setu.gymtracker.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ie.setu.gymtracker.R
import ie.setu.gymtracker.models.ExerciseModel
import ie.setu.gymtracker.models.WorkoutModel

class WorkoutAdapter(
    private var workouts: List<WorkoutModel>,
    private val onEdit: (WorkoutModel) -> Unit,
    private val onDelete: (WorkoutModel) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    class WorkoutViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val gymNameText: TextView =
            itemView.findViewById(R.id.gymNameText)

        val dateText: TextView =
            itemView.findViewById(R.id.dateText)

        val exercisesText: TextView =
            itemView.findViewById(R.id.exercisesText)

        val editButton: Button =
            itemView.findViewById(R.id.editButton)

        val deleteButton: Button =
            itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WorkoutViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_workout,
                parent,
                false
            )

        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: WorkoutViewHolder,
        position: Int
    ) {

        val workout = workouts[position]

        holder.gymNameText.text =
            workout.gymName

        holder.dateText.text =
            workout.date.toString()

        val context = holder.itemView.context

        holder.exercisesText.text =
            if (workout.exercises.isEmpty()) {
                context.getString(R.string.no_exercises)
            } else {
                workout.exercises.joinToString("\n") { describe(context, it) }
            }

        holder.editButton.setOnClickListener {
            onEdit(workout)
        }

        holder.deleteButton.setOnClickListener {
            onDelete(workout)
        }
    }

    override fun getItemCount(): Int {
        return workouts.size
    }

    fun updateWorkouts(newWorkouts: List<WorkoutModel>) {
        workouts = newWorkouts
        notifyDataSetChanged()
    }

    private fun describe(context: Context, exercise: ExerciseModel): String {
        val weight =
            if (exercise.weightKg > 0) {
                context.getString(R.string.exercise_weight, exercise.weightKg.toString())
            } else {
                context.getString(R.string.exercise_bodyweight)
            }
        return context.getString(
            R.string.exercise_summary,
            exercise.name,
            exercise.sets,
            exercise.reps,
            weight
        )
    }
}
