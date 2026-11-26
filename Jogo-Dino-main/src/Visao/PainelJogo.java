package Visao;

import Modelo.Jogo;
import Modelo.ObjetoDoJogo;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * A visão do jogo: só desenha o que o Jogo guarda (objetos, placar e avisos).
 * Não tem regra nenhuma: quem decide o que acontece é o Jogo.
 */
public class PainelJogo extends JPanel {

    private static final int LARGURA = Jogo.LARGURA;
    private static final int ALTURA = Jogo.ALTURA;

    private final Jogo jogo;
    private final JButton botaoMusica = new JButton();   // liga/desliga a música (o Controle diz o que fazer)

    // Recebe o Jogo só para LER os dados; nunca altera as regras
    public PainelJogo(Jogo jogo) {
        this.jogo = jogo;
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setBackground(Color.WHITE);
        setFocusable(true);          // sem isso o painel não recebe as teclas

        // botão no canto de cima, à esquerda
        setLayout(null);
        botaoMusica.setBounds(10, 8, 32, 32);
        botaoMusica.setBackground(new Color(235, 235, 235));
        botaoMusica.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        botaoMusica.setFocusPainted(false);
        botaoMusica.setFocusable(false);   // clicar não tira o teclado do jogo (o espaço continua pulando)
        add(botaoMusica);
        mostrarMusica(true);
    }

    /** O Controle entrega o que fazer quando clicarem no botão. */
    public void aoClicarMusica(Runnable acao) {
        botaoMusica.addActionListener(e -> acao.run());
    }

    /** Troca o ícone do botão: alto-falante com ondas (ligada) ou com X (desligada). */
    public void mostrarMusica(boolean ligada) {
        botaoMusica.setIcon(new IconeSom(ligada));
        botaoMusica.setToolTipText(ligada ? "Música ligada (M)" : "Música desligada (M)");   // aparece ao parar o mouse em cima
    }

    // Chamado a cada repaint(): desenha objetos, placar, mensagens e modo noturno
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);     // limpa o fundo

        // Polimorfismo: o painel não sabe se é chão, jogador ou obstáculo
        for (ObjetoDoJogo objeto : jogo.getObjetos()) {
            objeto.desenhar(g);
        }

        int pontuacao = jogo.getPontuacao();
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
        g.drawString(String.format("%05d", pontuacao), LARGURA - 80, 30);
        // 1 ponto = 10 mil anos, então 100 pontos = 1 milhão de anos
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        g.drawString(String.format("%.1f", pontuacao / 100.0).replace('.', ',') + " mi anos", LARGURA - 112, 48);

        if (jogo.getEstado() == Jogo.Estado.GAME_OVER) {
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
            g.drawString("GAME OVER", LARGURA / 2 - 75, ALTURA / 2 - 20);
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
            g.drawString("Aperte ENTER para ver o ranking", LARGURA / 2 - 145, ALTURA / 2 + 10);
        }

        String mensagem = jogo.getMensagem();
        if (mensagem != null) {
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
            FontMetrics medidas = g.getFontMetrics();
            g.drawString(mensagem, (LARGURA - medidas.stringWidth(mensagem)) / 2, ALTURA / 2 - 80);
        }

        if (jogo.isNoite()) {
            // inverte todas as cores já desenhadas: fundo preto, desenhos claros
            g.setXORMode(Color.WHITE);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setPaintMode();
        }
    }
}
