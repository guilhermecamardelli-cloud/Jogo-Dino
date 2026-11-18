import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class Obstaculo extends ObjetoDoJogo {
    private final List<BufferedImage> quadros = new ArrayList<>();
    private final int velocidade;
    private int contador = 0;

    public Obstaculo(int x, int y, int largura, int altura, int velocidade, boolean passaro) {
        super(x, y, largura, altura);
        this.velocidade = velocidade;

        if (passaro) {
            carregar("/passaro_verde_1.png");
            carregar("/passaro_verde_2.png");
        } else {
            carregar("/Cacto.png");
        }
    }

    // Carrega uma imagem e, se der certo, adiciona na lista de quadros
    private void carregar(String caminho) {
        try (InputStream in = getClass().getResourceAsStream(caminho)) {
            if (in == null) {
                System.out.println("Imagem não encontrada: " + caminho);
                return;
            }
            BufferedImage img = ImageIO.read(in);
            if (img != null) {
                quadros.add(img);
            }
        } catch (IOException e) {
            System.out.println("Erro ao carregar " + caminho + ": " + e.getMessage());
        }
    }

    @Override
    public void atualizar() {
        x -= velocidade;   // anda para a esquerda
        contador++;
    }

    @Override
    public void desenhar(Graphics g) {
        if (quadros.isEmpty()) {
            return;
        }
        // troca de quadro a cada 10 atualizações (o cacto tem só 1, então não muda)
        BufferedImage atual = quadros.get((contador / 10) % quadros.size());
        g.drawImage(atual, x, y, -largura, altura, null);
    }

    public boolean saiuDaTela() {
        return x + largura < 0;
    }
}
