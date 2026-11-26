package Modelo;
import java.awt.Color;
import java.awt.Graphics;

/** O chão: uma linha com traços que deslizam para a esquerda e dão a sensação de velocidade. */
public class Chao extends ObjetoDoJogo {

    private static final int PERIODO = 60;   // de quanto em quanto o desenho se repete

    private int deslocamento;
    private int velocidade;

    public Chao(int x, int y, int largura, int altura, int velocidade) {
        super(x, y, largura, altura);
        this.velocidade = velocidade;
    }

    public void setVelocidade(int velocidade) {
        this.velocidade = velocidade;
    }

    // Faz os traços andarem; o resto (%) faz o desenho repetir sem fim
    @Override
    public void atualizar() {
        deslocamento = (deslocamento + velocidade) % PERIODO;
    }

    // Linha reta + traços deslocados
    @Override
    public void desenhar(Graphics g) {
        g.setColor(Color.GRAY);
        g.drawLine(getX(), getY(), getX() + getLargura(), getY());          // a linha do chão

        // traços que andam para a esquerda e voltam ao começo a cada PERIODO
        for (int px = -deslocamento; px < getLargura(); px += PERIODO) {
            g.fillRect(getX() + px, getY() + 10, 20, 2);
            g.fillRect(getX() + px + 35, getY() + 22, 8, 2);
            g.fillRect(getX() + px + 15, getY() + 34, 12, 2);
        }
    }
}
