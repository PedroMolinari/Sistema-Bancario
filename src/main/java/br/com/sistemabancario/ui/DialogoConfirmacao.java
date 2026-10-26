package br.com.sistemabancario.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicButtonUI;

/** Confirmação modal sem os ícones e botões nativos do JOptionPane. */
final class DialogoConfirmacao {
    private static final Pattern UUID_TEXTO = Pattern.compile(
            "(?i)(?<![0-9a-f])[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}(?![0-9a-f])");
    private enum TipoMensagem {
        SUCESSO("✓", new Color(37, 110, 83), new Color(231, 245, 238)),
        AVISO("!", new Color(152, 97, 22), new Color(255, 247, 230)),
        ERRO("×", new Color(174, 58, 64), new Color(255, 238, 237)),
        INFORMACAO("i", EstiloSwing.AZUL, EstiloSwing.AZUL_CLARO);

        private final String simbolo;
        private final Color cor;
        private final Color fundo;

        TipoMensagem(String simbolo, Color cor, Color fundo) {
            this.simbolo = simbolo;
            this.cor = cor;
            this.fundo = fundo;
        }
    }

    private DialogoConfirmacao() { }

    static void sucesso(Component pai, String titulo, String mensagem) {
        mostrar(pai, titulo, mensagem, TipoMensagem.SUCESSO);
    }

    static void aviso(Component pai, String titulo, String mensagem) {
        mostrar(pai, titulo, mensagem, TipoMensagem.AVISO);
    }

    static void erro(Component pai, String titulo, String mensagem) {
        mostrar(pai, titulo, mensagem, TipoMensagem.ERRO);
    }

    static void informacao(Component pai, String titulo, String mensagem) {
        mostrar(pai, titulo, mensagem, TipoMensagem.INFORMACAO);
    }

    static boolean confirmar(Component pai, String titulo, String mensagem,
                             String textoConfirmar, boolean destrutiva) {
        Window proprietaria = SwingUtilities.getWindowAncestor(pai);
        JDialog dialogo = new JDialog(proprietaria, titulo, Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setUndecorated(true);
        dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel conteudo = new JPanel(new BorderLayout(0, 18));
        conteudo.setBackground(Color.WHITE);
        conteudo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstiloSwing.BORDA),
                BorderFactory.createEmptyBorder(0, 0, 18, 0)));

        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(destrutiva ? new Color(255, 244, 243) : EstiloSwing.AZUL_CLARO);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(18, 22, 16, 22));
        JLabel tituloLabel = new JLabel(titulo);
        tituloLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        tituloLabel.setForeground(EstiloSwing.TEXTO);
        cabecalho.add(tituloLabel, BorderLayout.CENTER);
        conteudo.add(cabecalho, BorderLayout.NORTH);

        JLabel texto = new JLabel("<html><div style='width: 390px'>"
                + escapar(formatarIds(mensagem)).replace("\n", "<br>") + "</div></html>");
        texto.setFont(EstiloSwing.FONTE_CORPO);
        texto.setForeground(EstiloSwing.TEXTO);
        texto.setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 22));
        conteudo.add(texto, BorderLayout.CENTER);

        JButton cancelar = EstiloSwing.botao("Cancelar", false);
        JButton confirmar = EstiloSwing.botao(textoConfirmar, true);
        cancelar.setUI(new BasicButtonUI());
        confirmar.setUI(new BasicButtonUI());
        if (destrutiva) {
            confirmar.setBackground(EstiloSwing.ERRO);
            confirmar.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(EstiloSwing.ERRO),
                    BorderFactory.createEmptyBorder(11, 18, 11, 18)));
        }
        boolean[] confirmado = {false};
        cancelar.addActionListener(evento -> dialogo.dispose());
        confirmar.addActionListener(evento -> {
            confirmado[0] = true;
            dialogo.dispose();
        });

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botoes.setOpaque(false);
        botoes.setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 22));
        botoes.add(cancelar);
        botoes.add(confirmar);
        conteudo.add(botoes, BorderLayout.SOUTH);

        dialogo.setContentPane(conteudo);
        dialogo.getRootPane().setDefaultButton(confirmar);
        dialogo.getRootPane().registerKeyboardAction(evento -> dialogo.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialogo.setMinimumSize(new Dimension(470, 190));
        dialogo.pack();
        dialogo.setLocationRelativeTo(pai);
        dialogo.setVisible(true);
        return confirmado[0];
    }

    private static void mostrar(Component pai, String titulo, String mensagem, TipoMensagem tipo) {
        Window proprietaria = SwingUtilities.getWindowAncestor(pai);
        JDialog dialogo = new JDialog(proprietaria, titulo, Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setUndecorated(true);
        dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel conteudo = new JPanel(new BorderLayout(0, 18));
        conteudo.setBackground(Color.WHITE);
        conteudo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstiloSwing.BORDA),
                BorderFactory.createEmptyBorder(0, 0, 18, 0)));

        JPanel cabecalho = new JPanel(new BorderLayout(14, 0));
        cabecalho.setBackground(tipo.fundo);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(18, 22, 16, 22));
        JLabel simbolo = new JLabel(tipo.simbolo, JLabel.CENTER);
        simbolo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        simbolo.setForeground(Color.WHITE);
        simbolo.setOpaque(true);
        simbolo.setBackground(tipo.cor);
        simbolo.setPreferredSize(new Dimension(34, 34));
        JLabel tituloLabel = new JLabel(titulo);
        tituloLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        tituloLabel.setForeground(EstiloSwing.TEXTO);
        JPanel identidade = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        identidade.setOpaque(false);
        identidade.add(simbolo);
        identidade.add(tituloLabel);
        cabecalho.add(identidade, BorderLayout.CENTER);
        conteudo.add(cabecalho, BorderLayout.NORTH);

        JLabel texto = new JLabel("<html><div style='width: 390px'>"
                + escapar(formatarIds(mensagem)).replace("\n", "<br>") + "</div></html>");
        texto.setFont(EstiloSwing.FONTE_CORPO);
        texto.setForeground(EstiloSwing.TEXTO);
        texto.setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 22));
        conteudo.add(texto, BorderLayout.CENTER);

        JButton fechar = EstiloSwing.botao("Fechar", true);
        fechar.setUI(new BasicButtonUI());
        fechar.addActionListener(evento -> dialogo.dispose());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        botoes.setOpaque(false);
        botoes.setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 22));
        botoes.add(fechar);
        conteudo.add(botoes, BorderLayout.SOUTH);

        dialogo.setContentPane(conteudo);
        dialogo.getRootPane().setDefaultButton(fechar);
        dialogo.getRootPane().registerKeyboardAction(evento -> dialogo.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialogo.setMinimumSize(new Dimension(470, 190));
        dialogo.pack();
        dialogo.setLocationRelativeTo(pai);
        dialogo.setVisible(true);
    }

    private static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String formatarIds(String texto) {
        Matcher encontrados = UUID_TEXTO.matcher(texto);
        StringBuffer resultado = new StringBuffer();
        while (encontrados.find()) {
            String curto = br.com.sistemabancario.domain.Conta
                    .formatarIdentificador(UUID.fromString(encontrados.group()));
            encontrados.appendReplacement(resultado, Matcher.quoteReplacement(curto));
        }
        encontrados.appendTail(resultado);
        return resultado.toString();
    }
}
