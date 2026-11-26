package Controle;

import javax.swing.SwingUtilities;

/** Ponto de partida do programa: só entrega a execução ao JogoController. */
public class Main {

    // invokeLater: a janela é criada na thread própria do Swing, o jeito correto
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JogoController().iniciar());
    }
}
