# Jogo-Dino

Jogo estilo "dinossauro do Chrome" (corredor sem fim) em Java com Swing e AWT, feito para a disciplina de POO.

**Equipe:** Guilherme Camardelli, Claudio, Stevan e Sabrina.

## Como jogar

| Tecla | Ação |
|---|---|
| Espaço ou seta para cima | Pular |
| Seta para baixo | Abaixar (todos os personagens) |
| Enter | Depois do fim de jogo: abre o ranking; no ranking: escolhe a letra / joga de novo |
| Setas, letras e Backspace | No ranking: escolher, digitar e apagar o nome |
| M (ou o botão com o alto-falante, no canto da tela) | Liga e desliga a música (o som do pulo continua) |

## Regras

- 45 pontos por segundo (o barril chega em cerca de 4 minutos); a velocidade sobe 1 a cada 400 pontos (de 5 até 11).
- Cactos desde o início (sorteados entre cacto simples e cacto duplo); pássaros verdes a partir de 600 pontos, com altura e velocidade sorteadas e, às vezes, em bando de 2. Os mais altos o dinossauro passa por baixo; os mais baixos é preciso pular.
- Dia e noite se alternam a cada 1650 pontos: de dia aparece o sol, de noite a lua (as cores se invertem). Nuvens passam o tempo todo.
- A pontuação é simbólica: 1 ponto = 10 mil anos (a tela mostra os milhões de anos).
- Aos 6600 pontos (66 milhões de anos) cai um meteoro e o dinossauro vira esqueleto, mas continua correndo.
- Ranking dos 5 melhores (estilo fliperama): depois do GAME OVER, quem entrou escreve o nome com as setas e o Enter (ou digitando) e confirma em END. Fica salvo em `ranking.txt`.
- Música de fundo em loop e som a cada pulo; a música para no fim de jogo e recomeça com o Enter.
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
src/Visao/     telas e som: PainelJogo (desenha o jogo), TelaInicio, TelaRanking e Som
src/Controle/  Main e JogoController (loop, teclado e troca de telas)
src/           imagens (Cacto.png, Cacto_duplo.png, passaro_verde_1.png, passaro_verde_2.png)
docs/          COLA-DE-APRESENTACAO.md: o que falar de cada classe
```

## Classes (POO)

**Modelo**
- `ObjetoDoJogo` (abstrata): posição, tamanho e hitbox; base de tudo o que aparece no jogo.
- `Jogador`, `Obstaculo`, `Chao`, `Meteoro` e `Ceu` (sol, lua e nuvens): herdam de `ObjetoDoJogo` e sobrescrevem `atualizar()` e `desenhar()`. Os desenhos em pixel art do `Jogador` ficam no fim da própria classe.
- `Ranking`: os 5 melhores resultados (classe interna `Registro` com nome e pontos), em ordem, lidos e salvos no arquivo `ranking.txt`.
- `Jogo`: guarda a lista de objetos, a pontuação, a velocidade e o estado, e aplica as regras (colisão, dia e noite, meteoro e evolução).

**Visao**
- `PainelJogo`: só desenha o que o `Jogo` guarda (objetos, placar e avisos).
- `Som`: gera a música e o som do pulo por código (ondas quadrada e triangular, estilo 8 bits) e toca com `javax.sound.sampled`, sem arquivos de áudio. Fica na Visão porque o som, como o desenho, é saída para quem joga.
- `IconeSom`: implementa a interface `Icon` do Swing e desenha o alto-falante do botão de música (com ondas ou com X).
- `TelaRanking`: tela de recorde com céu estrelado, teclado de letras (A–Z, `.`, `-`, RUB apaga, END termina) e a tabela RANK / SCORE / NAME.
- `TelaInicio`: tela de início (TV antiga) com os botões START GAME e SAIR.

**Controle**
- `JogoController`: troca a tela de início pelo jogo, roda o loop de 60 passos por segundo (Timer), transforma as teclas em ações do `Jogo`, manda o `Som` tocar (pulo, início, fim de jogo) e abre a `TelaRanking` depois do fim de jogo.
- `Main`: só abre o `JogoController`.
