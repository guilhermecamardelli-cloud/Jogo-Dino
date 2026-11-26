package Visao;

import Modelo.Jogo;
import Modelo.Ranking;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;

/**
 * Tela de recorde no estilo fliperama: céu estrelado, teclado de letras para escrever o nome
 * e a tabela RANK / SCORE / NAME. Tem dois modos:
 *  - digitando: o jogador entrou no ranking e escolhe as letras do nome;
 *  - só tabela: mostra o ranking (quando não entrou, ou depois de apertar END).
 * Quem decide o que cada tecla faz é o JogoController; esta classe guarda o cursor e o nome e desenha.
 */
public class TelaRanking extends JPanel {

    public static final int MAX_LETRAS = 8;
    private static final int COLUNAS = 10;
    private static final String[] TECLAS = montarTeclas();

    private static final Color LETRA = Color.WHITE;
    private static final Color LETRA_APAGADA = new Color(90, 90, 110);
    private static final Color AZUL = new Color(90, 130, 255);
    private static final Color CURSOR = new Color(120, 140, 230);
    private static final Color MAGENTA = new Color(230, 40, 230);
    private static final Color LARANJA = new Color(255, 170, 40);
    private static final Color[] CORES_LINHA = {
        new Color(255, 40, 40), new Color(255, 140, 30), new Color(255, 230, 50),
        new Color(60, 220, 90), new Color(60, 200, 255)
    };
    private static final String[] POSICOES = { "1ST", "2ND", "3RD", "4TH", "5TH" };

    private final Ranking ranking;
    private final int pontos;
    private final int posicaoNova;          // linha do jogador na tabela (-1 se não entrou)
    private boolean digitando;
    private int cursor;                     // índice em TECLAS
    private final StringBuilder nome = new StringBuilder();
    private final int[][] estrelas = new int[140][3];   // x, y, brilho

    public TelaRanking(Ranking ranking, int pontos) {
        this.ranking = ranking;
        this.pontos = pontos;
        this.digitando = ranking.entraNoRanking(pontos);
        this.posicaoNova = digitando ? ranking.posicaoDe(pontos) : -1;

        setPreferredSize(new Dimension(Jogo.LARGURA, Jogo.ALTURA));
        setBackground(Color.BLACK);
        setFocusable(true);

        Random sorteio = new Random(66);    // sempre o mesmo céu
        for (int[] e : estrelas) {
            e[0] = sorteio.nextInt(Jogo.LARGURA);
            e[1] = sorteio.nextInt(Jogo.ALTURA);
            e[2] = sorteio.nextInt(4);
        }
    }

    /** A–Z, ponto, traço, RUB (apaga) e END (termina): 3 linhas de 10. */
    private static String[] montarTeclas() {
        List<String> teclas = new ArrayList<>();
        for (char c = 'A'; c <= 'Z'; c++) {
            teclas.add(String.valueOf(c));
        }
        teclas.add(".");
        teclas.add("-");
        teclas.add("RUB");
        teclas.add("END");
        return teclas.toArray(new String[0]);
    }

    // ---------- Ações (o JogoController chama a partir do teclado) ----------

    public boolean isDigitando() {
        return digitando;
    }

    /** Setas: anda pelo teclado; passando da borda, volta do outro lado. */
    public void mover(int dx, int dy) {
        int linhas = TECLAS.length / COLUNAS;
        int coluna = (cursor % COLUNAS + dx + COLUNAS) % COLUNAS;
        int linha = (cursor / COLUNAS + dy + linhas) % linhas;
        cursor = linha * COLUNAS + coluna;
    }

    /** Enter: usa a tecla do cursor. Devolve true quando escolheu END (nome pronto). */
    public boolean escolher() {
        String tecla = TECLAS[cursor];
        if (tecla.equals("END")) {
            return true;
        }
        if (tecla.equals("RUB")) {
            apagar();
        } else {
            digitar(tecla.charAt(0));
        }
        return false;
    }

    /** Também dá para digitar direto no teclado do computador. */
    public void digitar(char letra) {
        letra = Character.toUpperCase(letra);
        boolean valida = (letra >= 'A' && letra <= 'Z') || letra == '.' || letra == '-';
        if (digitando && valida && nome.length() < MAX_LETRAS) {
            nome.append(letra);
        }
    }

    public void apagar() {
        if (nome.length() > 0) {
            nome.deleteCharAt(nome.length() - 1);
        }
    }

    public String getNome() {
        return nome.toString();
    }

    /** Depois que o nome foi salvo no Ranking: para de digitar e só mostra a tabela. */
    public void terminar() {
        digitando = false;
    }

    // ---------- Desenho ----------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        boolean piscaLigado = (System.currentTimeMillis() / 300) % 2 == 0;

        desenharEstrelas(g2);

        // título
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        g2.setColor(LARANJA);
        String titulo = digitando
                ? "NOVO RECORDE! " + String.format("%05d", pontos) + " PONTOS - ESCREVA SEU NOME"
                : posicaoNova >= 0 ? "NOME SALVO!" : "SUA PONTUAÇÃO: " + String.format("%05d", pontos);
        centralizar(g2, titulo, 28);

        desenharTeclado(g2, piscaLigado);
        desenharTabela(g2, piscaLigado);

        // dica no rodapé
        g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        g2.setColor(Color.LIGHT_GRAY);
        centralizar(g2, digitando
                ? "SETAS + ENTER escolhem (ou digite)   BACKSPACE apaga   END termina"
                : "Aperte ENTER para jogar de novo", 390);
    }

    private void desenharEstrelas(Graphics2D g2) {
        long tempo = System.currentTimeMillis() / 250;
        for (int i = 0; i < estrelas.length; i++) {
            int[] e = estrelas[i];
            int brilho = 110 + 45 * ((e[2] + (int) ((tempo + i) % 4)) % 4);    // pisca devagar
            g2.setColor(new Color(brilho, brilho, brilho));
            int tamanho = e[2] == 3 ? 2 : 1;
            g2.fillRect(e[0], e[1], tamanho, tamanho);
        }
    }

    private void desenharTeclado(Graphics2D g2, boolean piscaLigado) {
        Font grande = new Font(Font.MONOSPACED, Font.BOLD, 30);
        Font pequena = new Font(Font.MONOSPACED, Font.BOLD, 14);
        for (int i = 0; i < TECLAS.length; i++) {
            int centroX = 145 + (i % COLUNAS) * 56;
            int centroY = 62 + (i / COLUNAS) * 44;
            String tecla = TECLAS[i];
            boolean especial = tecla.length() > 1;          // RUB e END

            if (digitando && i == cursor) {
                g2.setColor(CURSOR);
                g2.drawRoundRect(centroX - 21, centroY - 20, 42, 40, 8, 8);
                g2.drawRoundRect(centroX - 20, centroY - 19, 40, 38, 8, 8);
            }
            g2.setFont(especial ? pequena : grande);
            g2.setColor(!digitando ? LETRA_APAGADA : especial ? AZUL : LETRA);
            FontMetrics m = g2.getFontMetrics();
            g2.drawString(tecla, centroX - m.stringWidth(tecla) / 2, centroY + m.getAscent() / 2 - 2);
        }
    }

    private void desenharTabela(Graphics2D g2, boolean piscaLigado) {
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        g2.setColor(MAGENTA);
        g2.drawString("RANK", 120, 222);
        g2.drawString("SCORE", 300, 222);
        g2.drawString("NAME", 520, 222);

        // a lista que aparece: a do Ranking, com a linha nova "encaixada" enquanto digita
        List<Ranking.Registro> linhas = new ArrayList<>(ranking.getRegistros());
        if (digitando) {
            String digitado = nome + (piscaLigado && nome.length() < MAX_LETRAS ? "_" : "");
            linhas.add(posicaoNova, new Ranking.Registro(digitado, pontos));
        }

        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 22));
        for (int i = 0; i < Ranking.TAMANHO && i < linhas.size(); i++) {
            if (i == posicaoNova && !digitando && !piscaLigado) {
                continue;                                    // a linha nova pisca depois de salva
            }
            int y = 252 + i * 27;
            g2.setColor(CORES_LINHA[i]);
            g2.drawString(POSICOES[i], 120, y);
            g2.drawString(String.format("%05d", linhas.get(i).getPontos()), 300, y);
            g2.drawString(linhas.get(i).getNome(), 520, y);
        }
    }

    private void centralizar(Graphics2D g2, String texto, int y) {
        g2.drawString(texto, (getWidth() - g2.getFontMetrics().stringWidth(texto)) / 2, y);
    }
}
