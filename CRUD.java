import java.io.*;
import java.util.ArrayList;

public class CRUD {

    private String nomeArquivo = "./Dados/netflix1.db";
    
    // Metodo para abrir e acessar o arquivo binario
    public CRUD() {
        
        try (RandomAccessFile arq = new RandomAccessFile(nomeArquivo, "rw")){ // Abrindo o arquivo para leitura e escrita
             

            if (arq.length() == 0) { // Se o arquivo estiver vazio, inicializa com um ID 0
                arq.seek(0);
                arq.writeInt(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void importarCSVParaBinario() {

        String csv = "./Dados/netflix1.csv"; // Caminho do arquivo CSV
        int IDaux = -1; // Pegar ultimo ID
        BufferedReader br = null;

        // Escrita
        try (RandomAccessFile arq = new RandomAccessFile(this.nomeArquivo, "rw")){

            if(!new File (this.nomeArquivo).isFile()){ // Verificando se o arquivo existe
                return;
            }

            br = new BufferedReader(new FileReader(csv));

            String linha;

            br.readLine(); // Pula a primeira linha do cabeçalho

            arq.seek(4);

            while ((linha = br.readLine()) != null) { // Le cada linha do CSV

                if (linha.trim().isEmpty()) { // Pula as linha vazias
                    continue;
                }

                String[] infos = linha.split("(,)(?=(?:[^\"]|\"[^\"]*\")*$)"); // Dividindo os campos corretamente para
                                                                               // evitar o problema com as aspas duplas

                Netflix nf = new Netflix();

                // System.out.println(linha);
                if (infos[1].equals("TV Show")) { // Ignora os registros do tipo TV Show
                    continue;
                }

                // Definindo os atributos
                nf.setIdFilme(infos[0]);
                nf.setTipo(infos[1]);
                nf.setTitulo(infos[2]);
                nf.setDiretor(infos[3]);
                nf.setPais(infos[4]);
                nf.setData(infos[5]);
                nf.setDuracao(infos[8]);
                nf.setGenero(infos[9]);

                if (IDaux < nf.getIntIdFilme()) {
                    IDaux = nf.getIntIdFilme(); // Ultimo ID utilizado
                }
                // System.out.println(nf);

                byte[] dataBytes = nf.toByteArray(); // Passando o objeto para o array de bytes
                arq.writeByte(' '); // Marca a lapide viva
                arq.writeShort(dataBytes.length); // Escreve o tamanho de cada registro
                arq.write(dataBytes); // Escreve os registro no arquivo
            }

            arq.seek(0);
            arq.writeInt(IDaux); // Atualiza o ultimo ID

        } catch (IOException e) {
            e.getMessage();
        }
    }

    // Metodo que busca 1 ID / READ
    public void buscaID(String ID) throws IOException {

        try (RandomAccessFile arq = new RandomAccessFile(nomeArquivo, "rw")){
            
            arq.seek(4); // Pula os 4 primeiros bytes (onde o ID esta armazenado)

            if (this.nomeArquivo.isEmpty()) { // Verifica se o arquivo existe
                return;
            }

            while (arq.getFilePointer() < arq.length()) { // Verificando para não ultrapassar o tamanho do arquivo

                byte lapide = arq.readByte();
                short tamanho = arq.readShort(); // Tamanho do registro

                if (lapide == ' ') { // verifica se o registro esta vivo

                    byte[] dados = new byte[tamanho]; // Array de bytes para armazenar o tamanho
                    arq.read(dados); // le o registro completo

                    Netflix nf = new Netflix();
                    nf.fromByteArray(dados); // Preenchendo os atributos com os dados lidos
                    
                    if (nf.getIdFilme().equals(ID)) { // Comparando o ID do registro com o buscado
                        System.out.println(nf);

                        return;
                    }

                } else {
                    arq.skipBytes(tamanho); // pular caso a lapide esteja morta
                }

            }
            System.out.println("Filme com ID " + ID + " não encontrado.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao ler arquivo" + e.getMessage());
        }

    }

    // metodo para buscar IDs / READ
    public void buscaVariosIDs(ArrayList<String> IDs) throws IOException {
        
        if (this.nomeArquivo.isEmpty()) {// verificando se o arquivo existe
            return;
        }

        try (RandomAccessFile arq = new RandomAccessFile(nomeArquivo, "rw")){

            arq.seek(4); // Pula os 4 primeiros bytes (onde o ID esta armazenado)

            int idsRestantes = IDs.size(); // Quantidade de IDs que ainda precisam ser buscados

            while (arq.getFilePointer() < arq.length()) {// Verificando para não ultrapassar o tamanho do arquivo

                // long pos = arq.getFilePointer();
                byte lapide = arq.readByte();
                short tamanho = arq.readShort();

                if (lapide == ' ') { // verifica se o registro esta vivo

                    byte[] dados = new byte[tamanho];
                    arq.read(dados); // le os dados do registro do array de bytes

                    Netflix nf = new Netflix();
                    nf.fromByteArray(dados);

                    for (int i = 0; i < IDs.size(); i++) { // Percorrendo os IDs que seram buscados

                        if (nf.getIdFilme().equals(IDs.get(i))) { // Comparando o ID do registro lindo com o buscado
                            System.out.println(nf);
                            idsRestantes--; // Decramenta na quantidade de IDs restantes
                            IDs.remove(i); // Remove o ID da lista, ja que ele foi encontrado

                            if (idsRestantes <= 0) { // Encerra a busca quando encontra todos os IDs
                                return;
                            }

                            continue;

                        }

                    }

                } else {
                    arq.skipBytes(tamanho); // Pula se o registro estiver morto
                }
            }
            System.out.println("Filmes com IDs " + IDs + " não encontrado.");
            
        } catch (Exception e) {
            System.out.println("Erro ao ler arquivo" + e.getMessage());
        }
    }

    // CREATE
    public void escreveNetflixBin(Netflix nf) {

        if (this.nomeArquivo.isEmpty()) { // Verifica se o arquivo existe
            return;
        }

        try (RandomAccessFile arq = new RandomAccessFile(this.nomeArquivo, "rw")){

            arq.seek(0); // Apontando para a primeira posição

            int IDant = arq.readInt(); // Le o ultimo ID utilizado

            System.out.println("\nUltimo ID utilizado: " + IDant);

            arq.seek(arq.length()); // Pular o ponteiro para o fim do arquivo

            nf.setIdFilme("s" + String.valueOf(IDant + 1)); // Incrementando o ultimo ID usado
            // int r1 = 11 + 1
            // String r2 = Int(12) -> String ("12")
            // String r3 = "s" + "12";

            // escrevendo no arquivo
            byte[] dataBytes = nf.toByteArray(); // Colocando o objeto em um array de bytes
            arq.writeByte(' '); // Lapide viva
            arq.writeShort(dataBytes.length); // Tamanho de cada registro
            arq.write(dataBytes); // Escreve os dados no arquivo

            arq.seek(0); // Move o ponteiro para o inicio do arquivo
            arq.writeInt(IDant + 1); // Atualiza o ultimo ID

            System.out.println("\nRegistro gravado!");
            System.out.println(nf);

            arq.close(); // Fecha o arquivo

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // UPDATE
    public void atualizar(Netflix nf) throws IOException {

        if (this.nomeArquivo.isEmpty()) { // verificando se o arquivo existe
            return;
        }

        try (RandomAccessFile arq = new RandomAccessFile(this.nomeArquivo, "rw")){

            arq.seek(4); // Pulando os 4 primeiros bytes

            while (arq.getFilePointer() < arq.length()) { // Verificando para não ultrapassar o tamanho do arquivo

                byte lapide = arq.readByte();
                short tamanho = arq.readShort(); // Tamanho do registro
                long pos = arq.getFilePointer(); // Guarda a posição inicial dos dados do registro

                if (lapide == ' ') { // Verifica se o registro esta vivo

                    byte[] dados = new byte[tamanho];
                    arq.read(dados); // Le registro completo

                    Netflix registroAntigo = new Netflix();
                    registroAntigo.fromByteArray(dados);

                    if (registroAntigo.getIdFilme().equals(nf.getIdFilme())) { // Verifica se o ID do registro é o mesmo
                                                                               // do buscado

                        byte[] novoRegistro = nf.toByteArray(); // Converte os novos dados para um array de bytes
                        int novoTamanho = novoRegistro.length; // Pega o tamanho do novo registro

                        if (novoTamanho <= tamanho) { // Verifica se o tamanho do registro atual e menor ou igual ao
                                                      // antigo
                            // caso o novo registro caiba no mesmo espeço do registro antigo, apenas
                            // sobrescreve
                            arq.seek(pos); // Volta para o inicio do registro
                            arq.write(novoRegistro); // Sobrescreve com os novos dados
                            System.out.println("\nRegistro atualizado com sucesso!\n");

                        } else {
                            // caso o novo registro tenha um tamanho maior que o registro anterio, escreve
                            // um novo registro ao final do arquivo
                            arq.seek(pos - 3); // Volta para o inicio do registro
                            arq.writeByte('-'); // Marca o registro como morto

                            arq.seek(arq.length()); // Vai para o final do arquivo
                            arq.writeByte(' '); // Nova lapide viva
                            arq.writeShort(novoTamanho); // Escreve o novo tamanho do registro
                            arq.write(novoRegistro); // Escreve os novos dados
                            System.out.println("\nNovo registro criado no final do arquivo!");

                        }

                        return;
                    }

                } else {
                    arq.skipBytes(tamanho); // Pular caso a lapide esteja morta
                }

            }
            System.out.println("Filme com ID " + nf.getIdFilme() + " não encontrado.");
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    // DELETE
    public void deletar(String ID) throws IOException {

        if (this.nomeArquivo.isEmpty()) { // verificando se o arquivo existe
            return;
        }

        try (RandomAccessFile arq = new RandomAccessFile(this.nomeArquivo, "rw")){
            
            arq.seek(0); // Apontando para a primeira posição

            int IDmax = arq.readInt(); // Le o ultimo ID utilizado

            while (arq.getFilePointer() < arq.length()) { // verificando para não ultrapassar o tamanho do arquivo

                byte lapide = arq.readByte();
                short tamanho = arq.readShort(); // Tamanho do registro
                // long pos = arq.getFilePointer();

                if (lapide == ' ') { // Verifica se o registro esta vivo

                    byte[] dados = new byte[tamanho];
                    arq.read(dados); // le registro completo

                    Netflix nf = new Netflix();
                    nf.fromByteArray(dados);

                    // System.out.println("ID :" + nf.getIdFilme());

                    if (nf.getIdFilme().equals(ID)) {
                        arq.seek(arq.getFilePointer() - (tamanho + 3)); // Posicao atual - (a lapide + espaco do tamanho
                        // arq.seek(pos); //Isso é feito para marcar a lapide, ou seja, marcar o
                        // registro registro como morto.

                        arq.writeByte('-'); // Escreve a lapide

                        if (nf.getIntIdFilme() == IDmax) { // Verifica se o registro a ser deletado tem o ultimo id
                                                           // utilizado
                            arq.seek(0); // Volta no inicio do arquivo (onde o ultimo id utilizado esta armazenado)
                            arq.writeInt(IDmax - 1); // Atualiza o ultimo id o decrementando, assim não se perde o
                                                     // ultimo id para o proximo registro
                        }
                        System.out.println("Arquivo deletado com sucesso !!!");
                        return;
                    }

                } else {
                    arq.skipBytes(tamanho); // Pular caso a lapide esteja morta
                }

            }
            System.out.println("Filme com ID " + ID + " não encontrado.");
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

}
