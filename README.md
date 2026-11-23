# Jogo-Dino

Jogo estilo "dinossauro do Chrome" (corredor sem fim) em Java com Swing e AWT, feito para a disciplina de POO.

**Equipe:** Guilherme Camardelli, Claudio, Stevan e Sabrina.

## Como jogar

| Tecla | Ação |
|---|---|
| Espaço ou seta para cima | Pular |
| Seta para baixo | Abaixar (todos os personagens) |
| Enter | Reiniciar depois do fim de jogo |

## Regras

- 45 pontos por segundo (o barril chega em cerca de 4 minutos); a velocidade sobe 1 a cada 400 pontos (de 5 até 11).
- Cactos desde o início; pássaros verdes a partir de 600 pontos, com altura e velocidade sorteadas e, às vezes, em bando de 2. Os mais altos o dinossauro passa por baixo; os mais baixos é preciso pular.
- Dia e noite se alternam a cada 1650 pontos: de dia aparece o sol, de noite a lua (as cores se invertem). Nuvens passam o tempo todo.
- A pontuação é simbólica: 1 ponto = 10 mil anos (a tela mostra os milhões de anos).
- Aos 6600 pontos (66 milhões de anos) cai um meteoro e o dinossauro vira esqueleto, mas continua correndo.
- **Modo infinito:** o jogo só termina ao encostar em um obstáculo. Com os anos o personagem evolui: aos 8800 pontos o esqueleto vira avestruz (as aves descendem dos dinossauros) e aos 11000 vira um barril de petróleo.

## Como rodar

Com o JDK instalado, dentro da pasta `src`:

```
javac *.java
java Main
```

(ou abra a pasta no VS Code / IntelliJ e rode a classe `Main`).

## Estrutura

```
src/    código (.java) e imagens (Cacto.png, passaro_verde_1.png, passaro_verde_2.png)
docs/   PDFs do trabalho (enunciado e material da equipe)
```

## Classes (POO)

- `ObjetoDoJogo` (abstrata): posição, tamanho e hitbox; base de tudo o que aparece no jogo.
- `Jogador`, `Obstaculo`, `Chao`, `Meteoro` e `Ceu` (sol, lua e nuvens): herdam de `ObjetoDoJogo` e sobrescrevem `atualizar()` e `desenhar()`. Os desenhos em pixel art do `Jogador` ficam no fim da própria classe.
- `PainelJogo`: guarda a lista de objetos, o loop do jogo (Thread), teclado, colisão, pontuação, dia e noite, meteoro e evolução.
- `TelaInicio`: tela de início (TV antiga) com os botões START GAME e SAIR.
- `Main`: abre a janela, mostra a tela de início e depois o jogo.
