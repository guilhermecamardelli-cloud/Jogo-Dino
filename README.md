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
- Cactos desde o início (a partir de 1000 pontos, às vezes surge o cacto duplo, maior); pássaros verdes a partir de 600 pontos, com altura e velocidade sorteadas e, às vezes, em bando de 2. Os mais altos o dinossauro passa por baixo; os mais baixos é preciso pular.
- Dia e noite se alternam a cada 1650 pontos: de dia aparece o sol, de noite a lua (as cores se invertem). Nuvens passam o tempo todo.
- A pontuação é simbólica: 1 ponto = 10 mil anos (a tela mostra os milhões de anos).
- Aos 6600 pontos (66 milhões de anos) cai um meteoro e o dinossauro vira esqueleto, mas continua correndo.
- **Modo infinito:** o jogo só termina ao encostar em um obstáculo. Com os anos o personagem evolui: aos 8800 pontos o esqueleto vira avestruz (as aves descendem dos dinossauros) e aos 11000 vira um barril de petróleo.

## Como rodar

Com o JDK instalado, dentro da pasta `src`:

```
javac Controle/*.java Modelo/*.java Visao/*.java
java -cp . Controle.Main
```

(ou abra a pasta no VS Code / IntelliJ e rode a classe `Main`, no pacote `Controle`).

## Estrutura (MVC)

```
src/Modelo/    regras e objetos do jogo (não conhecem janela nem teclado)
src/Visao/     telas: PainelJogo (desenha o jogo) e TelaInicio
src/Controle/  Main e JogoController (loop, teclado e troca de telas)
src/           imagens (Cacto.png, Cacto_duplo.png, passaro_verde_1.png, passaro_verde_2.png)
docs/          COLA-DE-APRESENTACAO.md: o que falar de cada classe
```

## Classes (POO)

**Modelo**
- `ObjetoDoJogo` (abstrata): posição, tamanho e hitbox; base de tudo o que aparece no jogo.
- `Jogador`, `Obstaculo`, `Chao`, `Meteoro` e `Ceu` (sol, lua e nuvens): herdam de `ObjetoDoJogo` e sobrescrevem `atualizar()` e `desenhar()`. Os desenhos em pixel art do `Jogador` ficam no fim da própria classe.
- `Jogo`: guarda a lista de objetos, a pontuação, a velocidade e o estado, e aplica as regras (colisão, dia e noite, meteoro e evolução).

**Visao**
- `PainelJogo`: só desenha o que o `Jogo` guarda (objetos, placar e avisos).
- `TelaInicio`: tela de início (TV antiga) com os botões START GAME e SAIR.

**Controle**
- `JogoController`: troca a tela de início pelo jogo, roda o loop de 60 passos por segundo (Timer) e transforma as teclas em ações do `Jogo`.
- `Main`: só abre o `JogoController`.
