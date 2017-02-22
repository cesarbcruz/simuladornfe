package cesar.com.simulador;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cesar
 */
public class SimuladorNfe {

    private boolean sucesso;
    private Properties prop;
    private static final String DANFE = "danfe";
    private static final String EMAIL = "email";
    private static final String NOTA = "nota";
    private static final String RESP = "resp-";
    private Thread processo;

    public SimuladorNfe(Properties prop) {
        this.sucesso = Boolean.getBoolean(prop.getProperty("sucesso"));
        this.prop = prop;
    }

    public void iniciarProcesso() {
        validarParametros();
        printParametros();
        Logger.getLogger(SimuladorNfe.class.getName()).log(Level.INFO, "Simulador NFe iniciado");
        processo = new Thread(new Runnable() {
            @Override
            public void run() {
                verificarArquivos();
            }
        });
        processo.start();
    }

    private void verificarArquivos() {
        try {
            File diretorio = new File(prop.getProperty("diretorioFill"));

            while (true) {
                File[] arquivos = diretorio.listFiles();
                if (arquivos != null) {
                    for (File arquivo : arquivos) {
                        if (arquivo.isFile() && arquivo.getName().startsWith(NOTA)) {
                            gerarArquivoResposta(criarRespostaEmissaoNFe(), NOTA, arquivo);
                            Logger.getLogger(SimuladorNfe.class.getName()).log(Level.INFO, "Resposta gerada para a nota: {0}", arquivo.getName());
                            arquivo.delete();
                        } else if (arquivo.isFile() && arquivo.getName().startsWith(EMAIL)) {
                            gerarArquivoResposta(criarRespostaEnvioEmail(), EMAIL, arquivo);
                            Logger.getLogger(SimuladorNfe.class.getName()).log(Level.INFO, "Resposta gerada para o email: {0}", arquivo.getName());
                            arquivo.delete();
                        } else if (arquivo.isFile() && arquivo.getName().startsWith(DANFE)) {
                            gerarArquivoResposta(criarRespostaGerarDanfe(), DANFE, arquivo);
                            Logger.getLogger(SimuladorNfe.class.getName()).log(Level.INFO, "Resposta gerada para o danfe: {0}", arquivo.getName());
                            arquivo.delete();
                            gerarPDF();
                        }
                        Thread.sleep(1000);
                    }
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(SimuladorNfe.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void finalizarProcesso() {
        if (processo != null) {
            processo.interrupt();
        }
        excluirArquivos();
        Logger.getLogger(SimuladorNfe.class.getName()).log(Level.INFO, "Simulador NFe finalizado");
    }

    private void gerarArquivoResposta(StringBuilder resposta, String operacao, File arquivo) throws IOException {
        FileWriter arquivoResposta;
        String nomeArquivo = arquivo.getName().replace(operacao, "").replace(".txt", "");
        arquivoResposta = new FileWriter(prop.getProperty("diretorioResp") + RESP + operacao + nomeArquivo + ".txt");
        arquivoResposta.write(resposta.toString());
        arquivoResposta.close();
    }

    private void gerarPDF() throws IOException {
        FileWriter arquivo;
        arquivo = new FileWriter(prop.getProperty("diretorioPDF") + formatarCodigoDanfe() + ".pdf");
        arquivo.write("Simulador NFe");
        arquivo.close();

    }

    private void excluirArquivos() {
        File diretorio = new File(prop.getProperty("diretorioFill"));
        File[] arquivos = diretorio.listFiles();
        for (File arquivo : arquivos) {
            if (arquivo.isFile()) {
                arquivo.delete();
            }
        }
    }

    private StringBuilder criarRespostaGerarDanfe() {
        StringBuilder resposta = new StringBuilder();
        if (sucesso) {
            resposta.append("\n Resultado=4012");
            resposta.append("\n Mensagem=DANFE enviado com sucesso para a impressora.");
        } else {
            resposta.append("\n Resultado=4009");
            resposta.append("\n Mensagem=Erro na impressão do DANFE erro: (ERRO)");
        }
        return resposta;
    }

    private StringBuilder criarRespostaEmissaoNFe() {
        incrementarCodigoDanfe();
        StringBuilder resposta = new StringBuilder();
        if (sucesso) {
            resposta.append("Resultado=100");
            resposta.append("\nMensagem=Autorizado o uso da NF-e");
            resposta.append("\nRecibo=351000102644812");
            resposta.append("\nChaveNFe=").append(formatarCodigoDanfe());
            resposta.append("\nNumeroLote=000000000009049");
            resposta.append("\nNprotocolo=135160005758731");
            resposta.append("\nDataAutCanc=2016-08-25");
            resposta.append("\nHoraAutCanc=16:11:08");
        } else {
            resposta.append("Resultado=201");
            resposta.append("\nMensagem=Rejeição: O número máximo de numeração de NF-e a inutilizar ultrapassou o limite.");
            resposta.append("\nRecibo=351000102532782");
            resposta.append("\nChaveNFe=").append(formatarCodigoDanfe());
            resposta.append("\nNumeroLote=000000000009045");
        }
        return resposta;
    }

    private StringBuilder criarRespostaEnvioEmail() {
        StringBuilder resposta = new StringBuilder();
        if (sucesso) {
            resposta.append("Resultado=4014\n");
            resposta.append("Mensagem= E-mail enviado.\n");
        } else {
            resposta.append("Resultado=4013\n");
            resposta.append("Mensagem= Erro na hora de enviar o e-mail.\n");
        }
        return resposta;
    }

    private void incrementarCodigoDanfe() {
        long codigoDanfe = Long.parseLong(prop.getProperty("ultimoCodigoDanfe"));
        codigoDanfe++;
        prop.setProperty("ultimoCodigoDanfe", String.valueOf(codigoDanfe));
    }

    private String formatarCodigoDanfe() {
        return String.format("%05d", Long.parseLong(prop.getProperty("ultimoCodigoDanfe")));
    }

    private void validarParametros() {
        validaDiretorio(prop.getProperty("diretorioFill"));
        validaDiretorio(prop.getProperty("diretorioResp"));
        validaDiretorio(prop.getProperty("diretorioPDF"));
        validaDiretorio(prop.getProperty("diretorioDanfe"));
        validaDiretorio(prop.getProperty("diretorioEntrada"));

        try {
            Long.parseLong(prop.getProperty("ultimoCodigoDanfe"));
        } catch (Exception ex) {
            throw new IllegalArgumentException("O parametro 'ultimoCodigoDanfe' deve possuir um valor numérico positivo. (SimuladorNFe.properties)");
        }

        if (prop.getProperty("sucesso") == null || (!"True".equals(prop.getProperty("sucesso")) && !"False".equals(prop.getProperty("sucesso")))) {
            throw new IllegalArgumentException("O parametro 'sucesso' deve possuir o valor 'True ou False' (SimuladorNFe.properties)");
        }
    }

    private void validaDiretorio(String diretorio) throws IllegalArgumentException {
        File dir = new File(diretorio);
        if (!dir.exists()) {
            throw new IllegalArgumentException("O diretório " + diretorio + " não existe");
        } else if (dir.isFile()) {
            throw new IllegalArgumentException("O caminho " + diretorio + " deve ser um diretório e não um arquivo");
        }
    }

    private void printParametros() {
        Set<Object> chaves = prop.keySet();
        System.out.println("Configurações: (SimuladorNFe.properties)");
        System.out.println("-----------------------");
        for (Object chave : chaves) {
            System.out.println(String.format("%s=%s ", chave.toString(), prop.get(chave).toString()));
        }
        System.out.println("-----------------------");
    }
}
