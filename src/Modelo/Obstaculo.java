package Modelo;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/** Cacto (simples ou duplo) ou pássaro que vem da direita. Usa imagens PNG; o pássaro tem 2 quadros (bate as asas). */
public class Obstaculo extends ObjetoDoJogo {

    private final BufferedImage[] quadros;
    private final int velocidade;
    private int contador;

    // passaro = true usa as 2 imagens de asa; false usa o cacto
    public Obstaculo(int x, int y, int largura, int altura, int velocidade, boolean passaro) {
        super(x, y, largura, altura);
        this.velocidade = velocidade;
        if (passaro) {
            quadros = new BufferedImage[] { ler("/passaro_verde_1.png"), ler("/passaro_verde_2.png") };
        } else {
            quadros = new BufferedImage[] { ler("/Cacto.png") };
        }
    }

    // Outro obstáculo de uma imagem só (usado pelo cacto duplo): passa o caminho do PNG
    public Obstaculo(int x, int y, int largura, int altura, int velocidade, String imagem) {
        super(x, y, largura, altura);
        this.velocidade = velocidade;
        quadros = new BufferedImage[] { ler(imagem) };
    }

    private BufferedImage ler(String caminho) {
        try {
            return ImageIO.read(getClass().getResource(caminho));
        } catch (Exception e) {
            System.out.println("Imagem não encontrada: " + caminho);
            return null;
        }
    }

    // Cada passo: anda para a esquerda e conta quadros para a animação
    @Override
    public void atualizar() {
        setX(getX() - velocidade);   // anda para a esquerda
        contador++;
    }

    @Override
    public void desenhar(Graphics g) {
        // troca de quadro a cada 10 atualizações (o cacto tem só 1, então não muda)
        BufferedImage atual = quadros[(contador / 10) % quadros.length];
        if (atual != null) {
            // largura negativa espelha a imagem (o pássaro olha para a esquerda); por isso começa em x + largura
            g.drawImage(atual, getX() + getLargura(), getY(), -getLargura(), getAltura(), null);
        }
    }

    // True quando já passou da borda esquerda: o Jogo pode remover
    public boolean saiuDaTela() {
        return getX() + getLargura() < 0;
    }
}
