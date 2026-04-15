# Matriz de Foco Físico

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)

Um aplicativo Android nativo que transforma a Matriz de Eisenhower em uma experiência gestual. Tarefas viram bolhas que o usuário arrasta e solta nos quadrantes — ao pousar, cada bolha quica com animação de mola.

## Demonstração

https://github.com/user-attachments/assets/45159328-36ac-4398-a3c3-59f3fdb0dfc4

---

## Funcionalidades

### Interface
uatro áreas com cores pastel — vermelho (Fazer Agora), verde (Agendar), âmbar (Delegar) e azul (Descartar/Avaliar).
- **Bolhas arrastáveis:** cada tarefa é representada por uma bolha quadrada colorida, posicionada automaticamente em uma grade de slots dentro do seu quadrante.
- **Animação de mola:** ao soltar uma bolha, ela anima até o slot correto com física de mola (`DampingRatioMediumBouncy`). Quando uma tarefa muda de quadrante, as demais se reorganizam com a mesma animação.
- **Input flutuante:** barra de criação de tarefas com design arredondado na parte inferior da tela, que sobe automaticamente ao abrir o teclado.

### Criação de tarefas

- Digite a tarefa no campo inferior e pressione Enter ou o botão "+".
- A nova tarefa vai para o primeiro quadrante que tiver espaço disponível, na ordem: **Fazer Agora → Agendar → Delegar → Descartar/Avaliar**.
- Se todos os quadrantes estiverem cheios, um aviso customizado é exibido.

### Edição

- **Toque simples** na bolha abre um dialog estilizado com o texto atual para edição.
- **Arrastar** move a bolha entre quadrantes.

### Exclusão

- **Arrastar até a zona de exclusão** (botão vermelho "✕" que aparece centralizado durante o arraste) remove a tarefa.
- A zona de exclusão dá feedback visual ao passar por cima: cresce e fica totalmente opaca.

### Limites por quadrante

| Quadrante | Máx. de tarefas | Grade |
|---|---|---|
| Fazer Agora | 8 | 2 colunas × 4 linhas |
| Agendar | 8 | 2 colunas × 4 linhas |
| Delegar | 6 | 2 colunas × 3 linhas |
| Descartar/Avaliar | 6 | 2 colunas × 3 linhas |

### Persistência

- Tarefas e seus quadrantes são salvos em `SharedPreferences` via Gson — sobrevivem ao fechamento do app.
- Estado da sessão (posições após rotação de tela) é preservado via `onSaveInstanceState`.

---

## Estrutura do Projeto

```
App-MatrizDeFocoFisico/
└── app/src/main/
    ├── java/com/ufrn/matrizdefocofsico/
    │   ├── MainActivity.kt          # Toda a lógica de UI, gestos e animações
    │   └── data/
    │       ├── Quadrant.kt          # Enum dos 4 quadrantes com cores e rótulos
    │       ├── Task.kt              # Modelo de dados da tarefa (id, text, quadrant)
    │       └── TaskRepository.kt    # Leitura e escrita no SharedPreferences
    └── res/
        ├── layout/
        │   ├── activity_main.xml        # Grade de quadrantes, overlay de bolhas, input bar
        │   ├── item_bubble.xml          # Layout de cada bolha (TextView 80×80dp)
        │   └── dialog_editar_tarefa.xml # Dialog customizado de edição
        ├── drawable/
        │   ├── bg_bubble.xml            # Fundo retangular com cantos arredondados
        │   ├── bg_delete_zone.xml       # Fundo circular da zona de exclusão
        │   ├── bg_input_bar.xml         # Pílula arredondada da barra de input
        │   ├── bg_dialog.xml            # Fundo do dialog de edição
        │   └── bg_dialog_input.xml      # Fundo do campo de texto do dialog
        └── values/
            ├── themes.xml               # Theme.AppCompat.NoActionBar
            └── colors.xml
```

---

## Tecnologias

| Categoria | Tecnologia |
|---|---|
| Linguagem | Kotlin |
| UI | XML Layouts + AppCompatActivity |
| SDK | API 35 (mín. 24) |
| Persistência | SharedPreferences + Gson |
| Animações | `androidx.dynamicanimation` (SpringAnimation) |
| Gestos | `setOnTouchListener` (ACTION_DOWN / MOVE / UP) |
| Teclado | `WindowCompat` + `ViewCompat.setOnApplyWindowInsetsListener` |
| IDE | Android Studio |

---

## Como executar

**Pré-requisitos:** [Git](https://git-scm.com) e [Android Studio](https://developer.android.com/studio).

```bash
git clone https://github.com/AppLab-Mobile-UFRN-2026-1/App-MatrizDeFocoFisico.git
```

1. Abra o Android Studio e selecione **Open**, apontando para a pasta clonada.
2. Aguarde o Gradle sincronizar as dependências.
3. Conecte um dispositivo Android via USB ou inicie um emulador pelo **Device Manager**.
4. Clique em **Run** (▶) ou pressione **Shift + F10**.

---

## Equipe

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/MarcusAurelius33">
        <img src="https://avatars.githubusercontent.com/MarcusAurelius33" width="100px;" alt="Marcus Aurelius"/>
        <br>
        <sub><b>Marcus Aurelius</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/leonardonadson">
        <img src="https://avatars.githubusercontent.com/leonardonadson" width="100px;" alt="Leonardo Nadson"/>
        <br>
        <sub><b>Leonardo Nadson</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/luan-sampaio">
        <img src="https://avatars.githubusercontent.com/luan-sampaio" width="100px;" alt="Luan Sampaio"/>
        <br>
        <sub><b>Luan Sampaio</b></sub>
      </a>
    </td>
  </tr>
</table>
