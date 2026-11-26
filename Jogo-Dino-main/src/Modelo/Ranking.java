package Modelo;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Os 5 melhores resultados (nome e pontos), do maior para o menor.
 * Fica salvo no arquivo ranking.txt, então não se perde ao fechar o jogo.
 * Não sabe nada de tela ou teclado: só guarda e ordena.
 */
public class Ranking {

    public static final int TAMANHO = 5;
    private static final Path ARQUIVO = Paths.get("ranking.txt");

    /** Uma linha do ranking. */
    public static class Registro {
        private final String nome;
        private final int pontos;

        public Registro(String nome, int pontos) {
            this.nome = nome;
            this.pontos = pontos;
        }

        public String getNome() {
            return nome;
        }

        public int getPontos() {
            return pontos;
        }
    }

    private final List<Registro> registros = new ArrayList<>();

    public Ranking() {
        carregar();
    }

    /** Em que posição (0 = primeiro) estes pontos entrariam. Empate fica depois de quem já estava. */
    public int posicaoDe(int pontos) {
        int posicao = 0;
        while (posicao < registros.size() && registros.get(posicao).getPontos() >= pontos) {
            posicao++;
        }
        return posicao;
    }

    /** True se os pontos são bons o bastante para entrar entre os 5. */
    public boolean entraNoRanking(int pontos) {
        return pontos > 0 && posicaoDe(pontos) < TAMANHO;
    }

    /** Coloca o novo resultado no lugar certo, tira o 6º (se houver) e salva no arquivo. */
    public void adicionar(String nome, int pontos) {
        if (nome == null || nome.trim().isEmpty()) {
            nome = "???";
        }
        registros.add(posicaoDe(pontos), new Registro(nome.trim(), pontos));
        while (registros.size() > TAMANHO) {
            registros.remove(registros.size() - 1);
        }
        salvar();
    }

    /** Só leitura: quem está de fora não pode mexer na lista. */
    public List<Registro> getRegistros() {
        return Collections.unmodifiableList(registros);
    }

    // ---------- Arquivo (uma linha por registro: NOME;PONTOS) ----------

    private void carregar() {
        if (!Files.exists(ARQUIVO)) {
            return;                                   // primeira vez: ranking vazio
        }
        try {
            for (String linha : Files.readAllLines(ARQUIVO, StandardCharsets.UTF_8)) {
                String[] partes = linha.split(";");
                if (partes.length == 2) {
                    registros.add(new Registro(partes[0], Integer.parseInt(partes[1].trim())));
                }
            }
        } catch (Exception e) {
            System.out.println("Não deu para ler o ranking: " + e.getMessage());
        }
    }

    private void salvar() {
        List<String> linhas = new ArrayList<>();
        for (Registro r : registros) {
            linhas.add(r.getNome() + ";" + r.getPontos());
        }
        try {
            Files.write(ARQUIVO, linhas, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("Não deu para salvar o ranking: " + e.getMessage());
        }
    }
}
