package reyes.michelle.listatareas

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.room.Room

class MainActivity : AppCompatActivity() {
    lateinit var et_tarea:EditText
    lateinit var btn_agregar:Button
    lateinit var listView_tareas:ListView
    lateinit var list_tareas: ArrayList<String>
    lateinit var adaptador:ArrayAdapter<String>
    lateinit var db:AppDatabase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Obtén el color que deseas establecer para la barra de notificaciones
        val colorStatusBar = ContextCompat.getColor(this, R.color.colorStatusBar)

// Establece el color de la barra de notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = colorStatusBar
        }

        et_tarea=findViewById(R.id.edit_tarea)
        btn_agregar=findViewById(R.id.btn_agregar)
        listView_tareas=findViewById(R.id.listV_tareas)
        list_tareas= ArrayList()

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "tareas-db"
        ).allowMainThreadQueries().build()

        cargarTareas()

        adaptador= object: ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, list_tareas) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById(android.R.id.text1) as TextView

                // Cambiar el color y el tamaño del texto
                textView.setTextColor(resources.getColor(R.color.white))
                textView.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.my_text_size)
                )

                return view
            }
        }


        listView_tareas.adapter=adaptador

        btn_agregar.setOnClickListener {
            var tarea_str = et_tarea.text.toString()
            if(!tarea_str.isNullOrEmpty()){
                var tarea= Tarea(desc = tarea_str)
                db.TareaDao().agregarTarea(tarea)
                list_tareas.add(tarea_str)
                adaptador.notifyDataSetChanged()
                et_tarea.setText("")
            }else{
                Toast.makeText(this,"No has agregado una tarea", Toast.LENGTH_SHORT).show()
            }
        }
        listView_tareas.onItemClickListener= AdapterView.OnItemClickListener{parent,view, position,id ->
            var tarea_desc= list_tareas[position]

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirmación")
            builder.setMessage("¿Estás seguro de que deseas eliminar esta tarea?")

            builder.setPositiveButton("Sí") { dialog, _ ->
                // Eliminar la tarea
                var tarea= db.TareaDao().obtenerTarea(tarea_desc)
                db.TareaDao().eliminarTarea(tarea)
                list_tareas.removeAt(position)
                adaptador.notifyDataSetChanged()
                Toast.makeText(this, "Tarea eliminada", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()

        }

        listView_tareas.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            // Obtener el elemento seleccionado
            var tareaSeleccionada = list_tareas[position]
            var tareaId= db.TareaDao().obtenerTarea(tareaSeleccionada).id
            var tareaDesc=db.TareaDao().obtenerTarea(tareaSeleccionada).desc
            // Mostrar un diálogo de edición o realizar alguna acción de edición
                abrirDialogoEdicion(tareaId,tareaDesc)
            adaptador.notifyDataSetChanged()
            // Devolver true para indicar que el evento de clic largo ha sido manejado
            true
        }


    }
    private fun cargarTareas(){
        var lista_db= db.TareaDao().obtenerTareas()
        for(tarea in lista_db){
            list_tareas.add(tarea.desc)
        }
    }
    private fun abrirDialogoEdicion(idTarea:Int, elemento: String) {
        val dialogoBuilder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogoView = inflater.inflate(R.layout.edit_dialog, null)
        val editText = dialogoView.findViewById<EditText>(R.id.editText)
        editText.setText(elemento) // Mostrar el elemento actual en el EditText

        dialogoBuilder.setView(dialogoView)
            .setPositiveButton("Guardar") { dialog, _ ->
                // Obtener el texto editado del EditText
                val textoEditado = editText.text.toString()

                db.TareaDao().editarTarea(idTarea,textoEditado)
                // Obtener la tarea editada de la base de datos
                val tareaEditada = db.TareaDao().obtenerTarea(textoEditado)

                // Buscar la posición de la tarea en list_tareas por su descripción original
                val posicionTareaEditada = list_tareas.indexOf(elemento)

                // Actualizar la descripción de la tarea en list_tareas con la descripción editada
                list_tareas[posicionTareaEditada] = textoEditado

                dialog.dismiss() // Cerrar el diálogo
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss() // Cerrar el diálogo sin guardar cambios
            }

        val dialogo = dialogoBuilder.create()
        dialogo.show()
    }
}