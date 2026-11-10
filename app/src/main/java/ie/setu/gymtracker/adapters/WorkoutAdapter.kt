package ie.setu.gymtracker.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ie.setu.gymtracker.R
import ie.setu.gymtracker.helpers.PhotoHelpers
import ie.setu.gymtracker.models.ExerciseModel
import ie.setu.gymtracker.models.WorkoutModel
import timber.log.Timber

// Adapter for the workout list: makes one row (item_workout.xml) for each workout
// and fills it in. The listener is WorkoutListActivity - the adapter just tells it
// when a workout should be edited or deleted
class WorkoutAdapter(
    private var workouts: List<WorkoutModel>,
    private val listener: WorkoutListener
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    // Holds the views inside one row, so they only need to be found once
    class WorkoutViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val gymNameText: TextView =
            itemView.findViewById(R.id.gymNameText)

        val dateText: TextView =
            itemView.findViewById(R.id.dateText)

        val exercisesText: TextView =
            itemView.findViewById(R.id.exercisesText)

        val photosRow: LinearLayout =
            itemView.findViewById(R.id.photosRow)

        val beforeThumb: ImageView =
            itemView.findViewById(R.id.beforeThumb)

        val afterThumb: ImageView =
            itemView.findViewById(R.id.afterThumb)

        val editButton: Button =
            itemView.findViewById(R.id.editButton)

        val deleteButton: Button =
            itemView.findViewById(R.id.deleteButton)
    }

    // Called when the list needs a new empty row - builds it from item_workout.xml
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

    // Called to fill a row with the workout at this position
    // (also used when a row is recycled for a different workout)
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

        showThumbs(holder, workout)

        holder.editButton.setOnClickListener {
            listener.onEditWorkout(workout)
        }

        holder.deleteButton.setOnClickListener {
            listener.onDeleteWorkout(workout)
        }

        // tap anywhere on the row = edit, long-press = delete
        holder.itemView.setOnClickListener {
            Timber.d("Row tapped, workout id=${workout.id}")
            listener.onEditWorkout(workout)
        }

        holder.itemView.setOnLongClickListener {
            Timber.d("Row long-pressed, workout id=${workout.id}")
            listener.onDeleteWorkout(workout)
            true   // true = "I handled the long-press", so a normal tap isn't fired too
        }
    }

    // Tells the list how many rows there are
    override fun getItemCount(): Int {
        return workouts.size
    }

    // Swaps in a new list (e.g. after a search or delete) and tells the list to redraw
    fun updateWorkouts(newWorkouts: List<WorkoutModel>) {
        workouts = newWorkouts
        notifyDataSetChanged()
    }

    // Turns one exercise into text like "• Squat: 5 x 5 @ 100.0kg"
    // (or "(bodyweight)" if there is no weight)
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

    // Shows the before / after photos as small thumbnails, and hides the row if there are none.
    // Rows are recycled, so every view is set every time (otherwise an old photo could show)
    private fun showThumbs(holder: WorkoutViewHolder, workout: WorkoutModel) {
        val before = workout.beforePhotoUri
        val after = workout.afterPhotoUri

        holder.photosRow.visibility =
            if (before == null && after == null) View.GONE else View.VISIBLE

        holder.beforeThumb.visibility = if (before == null) View.GONE else View.VISIBLE
        if (before != null) PhotoHelpers.showPhoto(holder.beforeThumb, before, maxSizePx = 240)

        holder.afterThumb.visibility = if (after == null) View.GONE else View.VISIBLE
        if (after != null) PhotoHelpers.showPhoto(holder.afterThumb, after, maxSizePx = 240)
    }
}
