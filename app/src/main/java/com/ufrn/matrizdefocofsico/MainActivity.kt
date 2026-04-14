package com.ufrn.matrizdefocofsico

import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.google.gson.Gson
import com.ufrn.matrizdefocofsico.data.Quadrant
import com.ufrn.matrizdefocofsico.data.Task
import com.ufrn.matrizdefocofsico.data.TaskRepository

class MainActivity : AppCompatActivity() {

    // ─── Views ────────────────────────────────────────────────────────────────
    private lateinit var bubbleContainer: FrameLayout
    private lateinit var editTask: EditText

    // ─── Estado ───────────────────────────────────────────────────────────────
    private lateinit var repository: TaskRepository
    private val tasks = mutableListOf<Task>()
    private val quadrantViews = mutableMapOf<Quadrant, View>()
    private val gson = Gson()

    // ─── Tamanho da bolha em px ───────────────────────────────────────────────
    private val bubbleSizePx: Int
        get() = (80 * resources.displayMetrics.density).toInt()

    // ─── onCreate ─────────────────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = TaskRepository(this)

        bubbleContainer = findViewById(R.id.bubble_container)
        editTask = findViewById(R.id.edit_task)

        quadrantViews[Quadrant.FAZER_AGORA] = findViewById(R.id.quadrant_fazer_agora)
        quadrantViews[Quadrant.AGENDAR]     = findViewById(R.id.quadrant_agendar)
        quadrantViews[Quadrant.DELEGAR]     = findViewById(R.id.quadrant_delegar)
        quadrantViews[Quadrant.ELIMINAR]    = findViewById(R.id.quadrant_eliminar)

        configurarInput()

        // Aguarda o layout ser medido antes de posicionar as bolhas
        val root = findViewById<View>(R.id.root)
        root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                tasks.addAll(repository.load())
                renderAllBubbles()
            }
        })
    }

    // ─── Input ────────────────────────────────────────────────────────────────
    private fun configurarInput() {
        val btnAdd = findViewById<Button>(R.id.btn_add)

        btnAdd.setOnClickListener { adicionarTarefa() }

        editTask.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                adicionarTarefa()
                true
            } else false
        }
    }

    private fun adicionarTarefa() {
        val texto = editTask.text.toString().trim()
        if (texto.isBlank()) return

        val task = Task(text = texto, quadrant = Quadrant.FAZER_AGORA)
        tasks.add(task)
        repository.save(tasks)
        editTask.text.clear()

        addBubbleView(task)
    }

    // ─── Bolhas ───────────────────────────────────────────────────────────────
    private fun renderAllBubbles() {
        bubbleContainer.removeAllViews()
        tasks.forEach { addBubbleView(it) }
    }

    private fun addBubbleView(task: Task) {
        val bubble = layoutInflater.inflate(R.layout.item_bubble, bubbleContainer, false) as TextView
        bubble.tag = task.id
        bubble.text = task.text
        bubble.setBackgroundColor(task.quadrant.bubbleColorInt)
        bubble.background = criarFundoCircular(task.quadrant.bubbleColorInt)

        // Posiciona no centro do quadrante
        val bounds = getBoundsOf(quadrantViews[task.quadrant]!!)
        bubble.x = bounds.exactCenterX() - bubbleSizePx / 2f
        bubble.y = bounds.exactCenterY() - bubbleSizePx / 2f

        configurarArraste(bubble, task)

        bubbleContainer.addView(bubble)
    }

    private fun configurarArraste(bubble: TextView, task: Task) {
        var dX = 0f
        var dY = 0f

        bubble.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                    view.elevation = 16f
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    view.x = event.rawX + dX
                    view.y = event.rawY + dY
                    true
                }
                MotionEvent.ACTION_UP -> {
                    view.elevation = 8f
                    val centerX = view.x + bubbleSizePx / 2f
                    val centerY = view.y + bubbleSizePx / 2f
                    val quadranteAlvo = detectarQuadrante(centerX, centerY) ?: task.quadrant

                    // Atualiza quadrante na lista e persiste
                    val index = tasks.indexOfFirst { it.id == task.id }
                    if (index >= 0) {
                        tasks[index] = tasks[index].copy(quadrant = quadranteAlvo)
                        repository.save(tasks)
                        bubble.background = criarFundoCircular(quadranteAlvo.bubbleColorInt)
                    }

                    animarParaQuadrante(view, quadranteAlvo)
                    true
                }
                else -> false
            }
        }

        bubble.setOnLongClickListener {
            tasks.removeIf { it.id == task.id }
            repository.save(tasks)
            bubbleContainer.removeView(it)
            true
        }
    }

    // ─── Animação de mola ─────────────────────────────────────────────────────
    private fun animarParaQuadrante(view: View, quadrant: Quadrant) {
        val bounds = getBoundsOf(quadrantViews[quadrant]!!)
        val targetX = bounds.exactCenterX() - bubbleSizePx / 2f
        val targetY = bounds.exactCenterY() - bubbleSizePx / 2f

        SpringAnimation(view, DynamicAnimation.X, targetX).apply {
            spring.dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
            spring.stiffness = SpringForce.STIFFNESS_MEDIUM
            start()
        }
        SpringAnimation(view, DynamicAnimation.Y, targetY).apply {
            spring.dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
            spring.stiffness = SpringForce.STIFFNESS_MEDIUM
            start()
        }
    }

    // ─── Detecção de quadrante ────────────────────────────────────────────────
    private fun detectarQuadrante(x: Float, y: Float): Quadrant? {
        return quadrantViews.entries.firstOrNull { (_, view) ->
            getBoundsOf(view).contains(x.toInt(), y.toInt())
        }?.key
    }

    private fun getBoundsOf(view: View): Rect {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return Rect(
            location[0],
            location[1],
            location[0] + view.width,
            location[1] + view.height
        )
    }

    // ─── Drawable circular com cor dinâmica ───────────────────────────────────
    private fun criarFundoCircular(colorInt: Int): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(colorInt)
        }
    }

    // ─── Persistência na rotação ──────────────────────────────────────────────
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("tasks_json", gson.toJson(tasks))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val json = savedInstanceState.getString("tasks_json") ?: return
        tasks.clear()
        tasks.addAll(gson.fromJson(json, Array<Task>::class.java).toList())
        renderAllBubbles()
    }
}
