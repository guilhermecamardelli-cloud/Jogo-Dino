# Cola de apresentação — Jogo do Dino

Resumo rápido para falar de cada classe. Tempo sugerido: 1 a 2 minutos por pessoa.

## Em uma frase (para abrir)

> "É um jogo no estilo do dinossauro do Chrome, feito em Java com Swing e AWT, com 11 classes organizadas em 3 pacotes: **Modelo** (as regras), **Visao** (o que aparece na tela) e **Controle** (liga as duas partes)."

## O que é MVC (explique uma vez, no começo)

| Pacote | Papel | Classes |
|---|---|---|
| **Modelo** | Guarda os dados e as regras. Não desenha nada. | `ObjetoDoJogo`, `Jogador`, `Obstaculo`, `Chao`, `Meteoro`, `Ceu`, `Jogo` |
| **Visao** | Só desenha o que o Modelo guarda. Não tem regra. | `PainelJogo`, `TelaInicio` |
| **Controle** | Liga as duas: roda o loop e lê o teclado. | `JogoController`, `Main` |

Fluxo em 4 passos: **teclado → Controle → Modelo muda → Visão desenha**.

---

## Divisão sugerida (4 pessoas)

### Pessoa 1 — Base: `ObjetoDoJogo` e `Chao`

**`ObjetoDoJogo` (Modelo, abstrata)**
- É a classe mãe de tudo o que aparece no jogo.
- Guarda `x`, `y`, `largura` e `altura` como **privados** (encapsulamento); só as filhas mudam a posição, por `setX`/`setY` protegidos.
- É **abstrata**: obriga as filhas a ter `atualizar()` e `desenhar()`, mas não diz como (abstração).
- Calcula a colisão: `colideCom()` compara o retângulo (`getHitbox()`) de dois objetos.
- Mostre: o construtor lança `IllegalArgumentException` se largura ou altura for ≤ 0.

**`Chao` (Modelo)**
- A linha do chão com traços que deslizam para a esquerda.
- Usa o resto da divisão (`%`) para o desenho se repetir e parecer infinito.
- O `Jogo` chama `setVelocidade()` quando o jogo acelera.

**Pergunta provável:** *"Por que ObjetoDoJogo é abstrata?"* → Porque não existe um "objeto genérico" na tela; só existem jogador, chão, obstáculos... A classe define o que todos têm em comum.

---

### Pessoa 2 — Dinossauro: `Jogador` e o teclado no `JogoController`

**`Jogador` (Modelo, extends ObjetoDoJogo)**
- Pula com gravidade: `pular()` dá um impulso para cima (`FORCA_PULO = -16`) e `atualizar()` soma a `GRAVIDADE` (0,8) a cada passo até pousar.
- `pular()` só vale no chão; `abaixar()` também (as 4 formas se abaixam).
- Tem **formas** (enum `Forma`: DINO, ESQUELETO, AVESTRUZ, BARRIL); `evoluirPara()` troca a forma e o personagem pisca.
- Os desenhos são pixel art guardados no fim da própria classe: cada texto é uma linha e cada letra é uma cor (`corDoPixel()`).
- Sobrescreve `getHitbox()` (**polimorfismo**): a caixa muda quando abaixado e no barril.

**`JogoController` — parte do teclado (Controle)**
- `keyPressed`: espaço ou seta para cima chama `jogo.pular()`; seta para baixo chama `jogo.abaixar(true)`; Enter chama `jogo.reiniciar()` só no GAME_OVER.
- `keyReleased`: soltar a seta para baixo levanta o dino.

**Pergunta provável:** *"Como funciona o pulo?"* → A velocidade vertical começa negativa (sobe) e aumenta 0,8 por passo até ficar positiva (desce), até tocar o chão.

---

### Pessoa 3 — Obstáculos: `Obstaculo`, `Meteoro` e a geração no `Jogo`

**`Obstaculo` (Modelo)**
- Cacto (pequeno, grande ou duplo) ou pássaro, desenhados com imagens PNG. O cacto duplo usa um segundo construtor, que recebe o caminho da imagem.
- Anda para a esquerda a cada passo; `saiuDaTela()` avisa quando pode ser removido.
- O pássaro tem 2 imagens e troca a cada 10 passos: parece bater asas.

**`Meteoro` (Modelo)**
- Cai aos **6600 pontos** (cada ponto = 10 mil anos, então 66 milhões de anos: a data do meteoro de verdade).
- `atualizar()` tem 3 fases: cai, a explosão cresce, a explosão some.
- Depois dele o dinossauro vira esqueleto e o jogo continua; aos 8800 vira avestruz e aos 11000 vira barril.

**Geração de obstáculos (dentro do `Jogo`)**
- `gerarObstaculo()` sorteia cacto pequeno, cacto grande ou pássaro (pássaros só depois de 600 pontos). Cada cacto sai simples ou duplo, por sorteio (1 chance em 3 de ser duplo).
- `criarPassaro()` sorteia a altura: baixo bate em pé; alto passa por cima.

**Pergunta provável:** *"Por que o jogo continua depois do meteoro?"* → É o modo infinito: só termina quando bate num obstáculo.

---

### Pessoa 4 — Regras e telas: `Jogo`, `Ceu`, `PainelJogo`, `TelaInicio`, `JogoController` (loop) e `Main`

**`Jogo` (Modelo)**
- É o cérebro: guarda `pontuacao`, `velocidade`, `estado` e todos os objetos.
- `atualizar()` é um passo do jogo: move os objetos, soma pontos (45 por segundo), acelera (velocidade de 5 a 11), gera obstáculos e confere colisões.
- Estados: JOGANDO, METEORO e GAME_OVER; cada tecla só vale no estado certo.
- `verificarColisoes()`: se o retângulo do jogador cruza o de um obstáculo, vai para GAME_OVER.
- Dia e noite a cada 1650 pontos.

**`Ceu` (Modelo)**
- Sol, lua e nuvens numa classe só (o atributo `astro` diz qual é): todos são pixel art que muda de cor à noite.

**`PainelJogo` (Visao)**
- Só **lê** o `Jogo` e desenha: percorre a lista de `ObjetoDoJogo` chamando `desenhar()` (**polimorfismo**: não sabe quem é quem).
- Escreve pontuação, "mi anos", GAME OVER e mensagens; no modo noturno inverte as cores.

**`TelaInicio` (Visao)**
- Tela de abertura com cara de TV antiga; botões START GAME e SAIR. Quando apertam START, avisa o controle (não decide nada).

**`JogoController` — parte do loop (Controle)**
- `abrirJogo()` cria o `Jogo` e o `PainelJogo`, troca a tela e liga um `Timer` do Swing.
- O `Timer` dispara 60 vezes por segundo: `jogo.atualizar()` e `painel.repaint()`.

**`Main` (Controle)**
- Só cria o `JogoController` e chama `iniciar()`.

**Pergunta provável:** *"Por que separar em Modelo, Visao e Controle?"* → Cada parte tem uma responsabilidade só: dá para mudar o visual sem mexer nas regras, e as regras sem mexer no teclado.

---

## Onde aparece cada conceito de POO (para fechar)

| Conceito | Onde |
|---|---|
| Classes e objetos | 11 classes; o `Jogo` possui um `Jogador`, um `Chao`, vários `Obstaculo`, `Ceu`, e um `Meteoro` |
| Encapsulamento | Atributos privados em todas as classes; acesso por métodos (`getX()`, `pular()`) |
| Herança | `ObjetoDoJogo` é pai de `Jogador`, `Obstaculo`, `Chao`, `Meteoro` e `Ceu` |
| Polimorfismo | `atualizar()` e `desenhar()` sobrescritos; `PainelJogo` percorre a lista sem saber o tipo; `getHitbox()` no `Jogador` |
| Abstração | `ObjetoDoJogo` é abstrata, com métodos abstratos |
| Tratamento de entradas inválidas | Construtor lança `IllegalArgumentException`; teclas fora do estado certo são ignoradas |

## Números para decorar

- Tela 800 x 400 · 60 quadros por segundo · 11 classes · 3 pacotes
- 45 pontos por segundo · 1 ponto = 10 mil anos
- Pássaros: 600 pontos · dia/noite: 1650 · meteoro: 6600 · avestruz: 8800 · barril: 11000
- Velocidade de 5 a 11 (sobe 1 a cada 400 pontos)

## Se der problema na hora

- Compilar e rodar (dentro da pasta `src`):
  ```
  javac Controle/*.java Modelo/*.java Visao/*.java
  java -cp . Controle.Main
  ```
- Controles: **espaço** ou **↑** pula · **↓** abaixa · **Enter** reinicia depois do GAME OVER.
