package reyes.michelle.listatareas

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TareasDAO {
    @Query("SELECT * FROM tareas")
    fun obtenerTareas(): List<Tarea>

    @Insert
    fun agregarTarea(tarea:Tarea)

    @Query("SELECT * FROM tareas WHERE `desc` =:descripcion ")
    fun obtenerTarea(descripcion:String):Tarea

    @Query("UPDATE tareas SET `desc` = :nuevoTexto WHERE id = :idTarea")
    fun editarTarea(idTarea: Int, nuevoTexto: String)

    @Delete
    fun eliminarTarea(tarea:Tarea)
}