package reyes.michelle.listatareas

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tareas")
data class Tarea (
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var desc: String)

