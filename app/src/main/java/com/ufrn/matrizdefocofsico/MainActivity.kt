package com.ufrn.matrizdefocofsico

import android.app.Dialog
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
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
    private lateinit var deleteZone: TextView
    private lateinit var inputBar: LinearLayout

    // ─── Estado ───────────────────────────────────────────────────────────────
    private lateinit var repository: TaskRepository
    private val tasks = mutableListOf<Task>()
    private var navBarHeight = 0
    private var statusBarHeight = 0
    private val quadrantViews = mutableMapOf<Quadrant, View>()
    private val gson = Gson()

    // ─── Tamanho da bolha e limites por quadrante ─────────────────────────────
    private val bubbleSizePx: Int
        get() = (80 * resources.displayMetrics.density).toInt()
    private val colunasQuadrante = 2
    private val limitePorQuadrante = mapOf(
        Quadrant.FAZER_AGORA to 8,
        Quadrant.AGENDAR     to 8,
        Quadrant.DELEGAR     to 6,
        Quadrant.ELIMINAR    to 6
    )
    private val ordemOverflow = listOf(
        Quadrant.FAZER_AGORA, Quadrant.AGENDAR, Quadrant.DELEGAR, Quadrant.ELIMINAR
    )

    // ─── onCreate ─────────────────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        repository = TaskRepository(this)

        bubbleContainer = findViewById(R.id.bubble_container)
        editTask = findViewById(R.id.edit_task)
        deleteZone = findViewById(R.id.delete_zone)

        quadrantViews[Quadrant.FAZER_AGORA] = findViewById(R.id.quadrant_fazer_agora)
        quadrantViews[Quadrant.AGENDAR]     = findViewById(R.id.quadrant_agendar)
        quadrantViews[Quadrant.DELEGAR]     = findViewById(R.id.quadrant_delegar)
        quadrantViews[Quadrant.ELIMINAR]    = findViewById(R.id.quadrant_eliminar)

        // Empurra o grid para baixo da status bar sem afetar o container de bolhas
        val statusBarResId = resources.getIdentifier("status_bar_height", "dimen", "android")
        statusBarHeight = if (statusBarResId > 0) resources.getDimensionPixelSize(statusBarResId) else 0
        findViewById<LinearLayout>(R.id.quadrant_grid).setPadding(0, statusBarHeight, 0, 0)

        // Empurra o input bar acima da barra de navegação do dispositivo
        val navBarResId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        navBarHeight = if (navBarResId > 0) resources.getDimensionPixelSize(navBarResId) else 0
        inputBar = findViewById(R.id.input_bar)
        aplicarMargemInputBar(0)

        configurarInput()

        val root = findViewById<View>(R.id.root)

        // Reposiciona o input bar quando o teclado abre/fecha (API moderna, funciona no Android 11+)
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            aplicarMargemInputBar(if (imeHeight > 0) imeHeight else 0)
            insets
        }

        // Aguarda o layout ser medido antes de posicionar as bolhas
        root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                tasks.addAll(repository.load())
                renderAllBubbles()
            }
        })
    }

    private fun aplicarMargemInputBar(keyboardHeight: Int) {
        val margemBase = (20 * resources.displayMetrics.density).toInt()
        val params = inputBar.layoutParams as android.widget.FrameLayout.LayoutParams
        params.bottomMargin = if (keyboardHeight > 0) {
            keyboardHeight + (8 * resources.displayMetrics.density).toInt()
        } else {
            margemBase + navBarHeight
        }
        inputBar.layoutParams = params
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

        val quadranteDestino = ordemOverflow.firstOrNull { q ->
            tasks.count { it.quadrant == q } < (limitePorQuadrante[q] ?: 0)
        }
        if (quadranteDestino == null) {
            mostrarAviso("Matriz cheia — remova uma tarefa para continuar")
            return
        }

        val task = Task(text = texto, quadrant = quadranteDestino)
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
        bubble.background = criarFundoRetangular(task.quadrant.bubbleColorInt)

        val slotIndex = tasks.filter { it.quadrant == task.quadrant }.indexOfFirst { it.id == task.id }
        val (x, y) = calcularPosicaoSlot(task.quadrant, slotIndex)
        bubble.x = x
        bubble.y = y

        configurarArraste(bubble, task)
        bubbleContainer.addView(bubble)
    }

    private fun configurarArraste(bubble: TextView, task: Task) {
        var dX = 0f
        var dY = 0f
        var downRawX = 0f
        var downRawY = 0f
        var isDragging = false
        var estavaSobreZona = false
        val touchSlop = 8 * resources.displayMetrics.density

        bubble.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                    downRawX = event.rawX
                    downRawY = event.rawY
                    isDragging = false
                    estavaSobreZona = false
                    view.elevation = 16f
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!isDragging) {
                        val movedX = kotlin.math.abs(event.rawX - downRawX)
                        val movedY = kotlin.math.abs(event.rawY - downRawY)
                        if (movedX > touchSlop || movedY > touchSlop) {
                            isDragging = true
                            mostrarZonaExclusao()
                        }
                    }
                    if (isDragging) {
                        view.x = event.rawX + dX
                        view.y = event.rawY + dY
                        val cx = view.x + bubbleSizePx / 2f
                        val cy = view.y + bubbleSizePx / 2f
                        val sobreZona = getBoundsOf(deleteZone).contains(cx.toInt(), cy.toInt())
                        if (sobreZona != estavaSobreZona) {
                            estavaSobreZona = sobreZona
                            deleteZone.animate().cancel()
                            deleteZone.animate()
                                .alpha(if (sobreZona) 1f else 0.7f)
                                .scaleX(if (sobreZona) 1.25f else 1f)
                                .scaleY(if (sobreZona) 1.25f else 1f)
                                .setDuration(120)
                                .start()
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    view.elevation = 8f
                    if (isDragging) {
                        ocultarZonaExclusao()
                        if (estavaSobreZona) {
                            val quadranteOrigem = tasks.firstOrNull { it.id == task.id }?.quadrant
                            tasks.removeIf { it.id == task.id }
                            repository.save(tasks)
                            bubbleContainer.removeView(view)
                            if (quadranteOrigem != null) reposicionarTarefasDoQuadrante(quadranteOrigem)
                        } else {
                            val centerX = view.x + bubbleSizePx / 2f
                            val centerY = view.y + bubbleSizePx / 2f
                            val index = tasks.indexOfFirst { it.id == task.id }
                            val tarefaAtual = tasks.getOrNull(index)
                            val quadranteOrigem = tarefaAtual?.quadrant ?: Quadrant.FAZER_AGORA
                            val quadranteAlvo = detectarQuadrante(centerX, centerY) ?: quadranteOrigem

                            val limite = limitePorQuadrante[quadranteAlvo] ?: 0
                            if (quadranteAlvo != quadranteOrigem &&
                                tasks.count { it.quadrant == quadranteAlvo } >= limite) {
                                mostrarAviso("Quadrante cheio — máx. $limite tarefas")
                                reposicionarTarefasDoQuadrante(quadranteOrigem)
                            } else {
                                if (index >= 0) {
                                    tasks[index] = tasks[index].copy(quadrant = quadranteAlvo)
                                    repository.save(tasks)
                                    bubble.background = criarFundoRetangular(quadranteAlvo.bubbleColorInt)
                                }
                                if (quadranteAlvo != quadranteOrigem) {
                                    reposicionarTarefasDoQuadrante(quadranteOrigem)
                                }
                                reposicionarTarefasDoQuadrante(quadranteAlvo)
                            }
                        }
                    } else {
                        editarTarefa(task.id, bubble)
                    }
                    true
                }
                else -> false
            }
        }
    }

    // ─── Edição de tarefa ─────────────────────────────────────────────────────
    private fun editarTarefa(taskId: String, bubble: TextView) {
        val task = tasks.firstOrNull { it.id == taskId } ?: return
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_editar_tarefa)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.88).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        val editText = dialog.findViewById<EditText>(R.id.dialog_edit_text)
        val btnCancelar = dialog.findViewById<Button>(R.id.dialog_btn_cancelar)
        val btnSalvar = dialog.findViewById<Button>(R.id.dialog_btn_salvar)

        editText.setText(task.text)
        editText.setSelection(task.text.length)

        btnCancelar.setOnClickListener { dialog.dismiss() }
        btnSalvar.setOnClickListener {
            val novoTexto = editText.text.toString().trim()
            if (novoTexto.isNotBlank()) {
                val index = tasks.indexOfFirst { it.id == task.id }
                if (index >= 0) {
                    tasks[index] = tasks[index].copy(text = novoTexto)
                    repository.save(tasks)
                    bubble.text = novoTexto
                }
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    // ─── Aviso customizado ────────────────────────────────────────────────────
    private fun mostrarAviso(mensagem: String) {
        val d = resources.displayMetrics.density
        val root = findViewById<FrameLayout>(R.id.root)

        val aviso = TextView(this).apply {
            text = mensagem
            setTextColor(android.graphics.Color.WHITE)
            textSize = 13f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
            setPadding((20 * d).toInt(), (12 * d).toInt(), (20 * d).toInt(), (12 * d).toInt())
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(0xF0212121.toInt())
                cornerRadius = 32 * d
            }
            elevation = 32f
            alpha = 0f
            translationY = -(16 * d)
        }

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL
            topMargin = statusBarHeight + (12 * d).toInt()
        }
        root.addView(aviso, params)

        aviso.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(220)
            .withEndAction {
                aviso.postDelayed({
                    aviso.animate()
                        .alpha(0f)
                        .translationY(-(16 * d))
                        .setDuration(220)
                        .withEndAction { root.removeView(aviso) }
                        .start()
                }, 2200)
            }
            .start()
    }

    // ─── Zona de exclusão ─────────────────────────────────────────────────────
    private fun mostrarZonaExclusao() {
        deleteZone.scaleX = 0.5f
        deleteZone.scaleY = 0.5f
        deleteZone.alpha = 0f
        deleteZone.visibility = View.VISIBLE
        deleteZone.animate()
            .alpha(0.7f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(200)
            .start()
    }

    private fun ocultarZonaExclusao() {
        deleteZone.animate()
            .alpha(0f)
            .scaleX(0.5f)
            .scaleY(0.5f)
            .setDuration(150)
            .withEndAction { deleteZone.visibility = View.GONE }
            .start()
    }

    // ─── Posicionamento por slots ─────────────────────────────────────────────
    private fun calcularPosicaoSlot(quadrant: Quadrant, slotIndex: Int): Pair<Float, Float> {
        val bounds = getBoundsOf(quadrantViews[quadrant]!!)
        val d = resources.displayMetrics.density
        val gap = 8 * d
        val labelReserva = 40 * d
        val col = slotIndex % colunasQuadrante
        val row = slotIndex / colunasQuadrante
        val gridWidth = colunasQuadrante * bubbleSizePx + (colunasQuadrante - 1) * gap
        val startX = bounds.left + (bounds.width() - gridWidth) / 2f
        val startY = bounds.top + labelReserva + gap
        return Pair(startX + col * (bubbleSizePx + gap), startY + row * (bubbleSizePx + gap))
    }

    private fun reposicionarTarefasDoQuadrante(quadrant: Quadrant) {
        tasks.filter { it.quadrant == quadrant }.forEachIndexed { slotIndex, t ->
            val view = bubbleContainer.findViewWithTag<View>(t.id) ?: return@forEachIndexed
            val (targetX, targetY) = calcularPosicaoSlot(quadrant, slotIndex)
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

    // ─── Drawable retangular com cor dinâmica ─────────────────────────────────
    private fun criarFundoRetangular(colorInt: Int): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius = 12 * resources.displayMetrics.density
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
