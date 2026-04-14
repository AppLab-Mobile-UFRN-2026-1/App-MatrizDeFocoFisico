# 🎯 Matriz de Foco Físico

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)

Um aplicativo Android nativo que transforma a Matriz de Eisenhower em uma experiência física e gestual. Tarefas viram bolhas que o usuário arrasta e solta nos quadrantes — ao pousar, cada bolha quica com animação de mola.

## 📸 Demonstração

> *(vídeo será adicionado após a gravação)*

---

## ✨ Funcionalidades

### 🎨 Design e Interface (UI/UX)
* **Grade de Quadrantes Colorida:** Tela dividida em quatro áreas com cores pastel distintas — vermelho (Fazer Agora), verde (Agendar), âmbar (Delegar) e azul (Eliminar).
* **Sem Listas de Texto:** A interação é 100% gestual; não há tabelas, checkboxes ou listas tradicionais.
* **Bolhas Físicas:** Cada tarefa é representada por uma bolha circular colorida que pode ser arrastada livremente pela tela.
* **Input Flutuante:** Barra de criação de tarefas com design arredondado, flutuando na parte inferior da tela.

### ⚙️ Lógica e Regras de Negócio
* **Criação de Tarefas:** O usuário digita a tarefa no campo inferior e pressiona Enter ou o botão "+" — a bolha aparece no centro do quadrante padrão.
* **Arrastar e Soltar (Pan Gesture):** Bolhas são movidas com `detectDragGestures`; o dedo segue o toque em tempo real sem travamentos.
* **Animação de Mola ao Soltar:** Ao liberar a bolha, ela identifica o quadrante alvo e anima até o centro com `Spring.DampingRatioMediumBouncy` — efeito de quique real.
* **Remoção por Toque Longo:** Segurar a bolha por um instante a remove da tela e da memória.
* **Persistência Local:** Todas as tarefas e seus quadrantes são salvos em `SharedPreferences` via Gson — sobrevivem ao fechamento do app.

---

## 📁 Estrutura do Projeto

```text
App-MatrizDeFocoFisico/
└── app/
    └── src/main/
        ├── java/com/ufrn/matrizdefocofsico/
        │   ├── MainActivity.kt              # Ponto de entrada, configura Compose
        │   ├── data/
        │   │   ├── Quadrant.kt              # Enum dos 4 quadrantes com cores e rótulos
        │   │   ├── Task.kt                  # Modelo de dados da tarefa
        │   │   └── TaskRepository.kt        # Leitura e escrita no SharedPreferences
        │   ├── viewmodel/
        │   │   └── MatrixViewModel.kt       # Estado global da lista de tarefas
        │   └── ui/
        │       ├── MatrixScreen.kt          # Tela principal: grade + input flutuante
        │       ├── TaskBubble.kt            # Bolha arrastável com animação de mola
        │       └── theme/
        │           ├── Color.kt             # Paleta de cores dos quadrantes
        │           ├── Theme.kt             # Tema MaterialTheme do app
        │           └── Type.kt              # Tipografia
        ├── res/values/
        │   ├── strings.xml
        │   └── themes.xml
        └── AndroidManifest.xml
```

---

## 🛠️ Tecnologias Utilizadas
* **Linguagem:** Kotlin
* **UI:** Jetpack Compose
* **SDK:** API 35 (Min: 24)
* **Persistência:** SharedPreferences + Gson
* **Animações:** `Animatable` com `spring()` (DampingRatioMediumBouncy)
* **Gestos:** `detectDragGestures` + `detectTapGestures`
* **Arquitetura:** ViewModel + State hoisting
* **IDE:** Android Studio

---

## 💻 Pré-requisitos

Antes de começar, você vai precisar ter instalado em sua máquina:
* [Git](https://git-scm.com) para clonar o repositório.
* [Android Studio](https://developer.android.com/studio) para rodar e editar o código.

## 🚀 Como executar o projeto

1. Abra o seu terminal e faça o clone deste repositório:
   ```bash
   git clone <url-do-repositorio>
   ```
2. Abra o Android Studio.

3. Na tela inicial, clique em **Open** e selecione a pasta do projeto que você acabou de clonar.

4. Aguarde o Gradle sincronizar todas as dependências.

5. Conecte o seu celular Android via cabo USB ou inicie um Emulador pelo **Device Manager**.

6. Clique no botão verde de **Run** (▶️) na barra superior ou pressione **Shift + F10** para rodar o aplicativo!

---

## 👥 Equipe de Desenvolvimento

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/MarcusAurelius33">
        <img src="https://avatars.githubusercontent.com/MarcusAurelius33" width="100px;" alt="Foto de Marcus Aurelius no GitHub"/>
        <br>
        <sub>
          <b>Marcus Aurelius</b>
        </sub>
      </a>
    </td>
  </tr>
</table>
