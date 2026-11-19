import java.awt.Color;
import java.awt.Graphics;

/**
 * O chão: uma linha com pequenos traços que deslizam para a esquerda,
 * dando a sensação de velocidade. O PainelJogo cria o Chao e chama
 * setVelocidade() sempre que o jogo acelera.
 */
public class Chao extends ObjetoDoJogo {

    private static final int PERIODO = 60;   // de quanto em quanto o desenho se repete

    private int deslocamento;
    private int velocidade;

    public Chao(int x, int y, int largura, int altura, int velocidade) {
        super(x, y, largura, altura);
        this.velocidade = velocidade;
        this.deslocamento = 0;
    }

    public void setVelocidade(int velocidade) {
        this.velocidade = velocidade;
    }

    @Override
    public void atualizar() {
        deslocamento = (deslocamento + velocidade) % PERIODO;
    }

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
