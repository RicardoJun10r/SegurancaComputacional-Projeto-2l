package client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.crypto.SecretKey;

import util.ClientSocket;
import util.Seguranca.CifrasSimetricas;
import util.Seguranca.RSA;

public class Usuarios implements Runnable {

    private final String ENDERECO_SERVER = "localhost";

    private ClientSocket clientSocket;

    private Scanner scan;

    private Boolean logado;

    private CifrasSimetricas seguranca = new CifrasSimetricas();

    private RSA rsa;

    public Usuarios() {
        this.scan = new Scanner(System.in);
        this.logado = false;
        this.rsa = new RSA();
    }

    @Override
    public void run() {
        String mensagem;
        while ((mensagem = this.clientSocket.getMessage()) != null) {
            if (mensagem.split(" ")[0].equals("rsa")) {
                this.rsa.setE_extrangeiro(Long.parseLong(mensagem.split(" ")[1]));
                this.rsa.phi(this.rsa.getP(), this.rsa.getQ());
                this.rsa.expD(this.rsa.getE_extrangeiro(), this.rsa.getPhi());
                System.out.println(
                        "Resposta do banco: " + mensagem);
            } else{
                System.out.println("MENSAGEM COM RSA: " + mensagem);
                mensagem = rsa.decifragemServer(mensagem);
                System.out.println("DECIFRANDO DO BANCO: " + mensagem);
                if (mensagem.split(" ")[0].equals("status")) {
                    System.out.println("MENSAGEM: " + mensagem);
                    logado = Boolean.parseBoolean(mensagem.split(" ")[1]);
                    if(logado){
                        this.seguranca.setChaveVernan(mensagem.split(" ")[2]);
                        this.seguranca.setChave((SecretKey) this.clientSocket.receberObjeto());
                    }
                } else {
                    System.out.println(
                            "Resposta do banco: " + mensagem);
                }
            }
        }
    }

    private void autenticar() {
        System.out.println("> 1 Entrar\n> 2 Registrar-se");
        System.out.print("> ");
        String op = scan.next();
        if (op.equals("1")) {
            System.out.println("> CPF");
            System.out.print("> ");
            String login = scan.next();
            System.out.println("> Senha");
            System.out.print("> ");
            String senha = scan.next();
            String msg_rsa = this.rsa.cifragemCliente("1;" + login + ";" + senha);
            System.out.println("RSA: " + msg_rsa);
            enviar(msg_rsa);
        } else if (op.equals("2")) {
            String senha;
            String nova_conta = "";
            System.out.println("Registrando\n> CPF");
            System.out.print("> ");
            nova_conta += scan.next() + ";";
            System.out.println("> Nome");
            System.out.print("> ");
            scan.nextLine();
            nova_conta += scan.nextLine() + ";";
            System.out.println("> Endereço");
            System.out.print("> ");
            nova_conta += scan.nextLine() + ";";
            System.out.println("> Telefone");
            System.out.print("> ");
            nova_conta += scan.nextLine() + ";";
            System.out.println("> Senha");
            System.out.print("> ");
            senha = scan.next();
            nova_conta += senha;
            String msg_rsa = this.rsa.cifragemCliente("2;" + nova_conta);
            enviar(msg_rsa);
        }
    }

    private void enviar(String mensagem) {
        this.clientSocket.sendMessage(mensagem);
    }

    private void menu() {
        System.out.println(
                "> 3 [ SAQUE ]\n> 4 [ DEPÓSITO ]\n> 5 [ Transferência ]\n> 6 [ SALDO ]\n> 7 [ INVESTIR EM POUPANÇA ]\n> 8 [ INVESTIR EM RENDA FIXA]\n> 9 [ SIMULAR POUPANÇA ]\n> 10 [ SIMULAR RENDA FIXA ]\n> sair");
    }

    private void messageLoop() {
        String mensagem = "";
        try {
            do {
                Thread.sleep(300);
                if (!logado) {
                    autenticar();
                } else {
                    System.out.println("> LOGADO");
                    menu();
                    System.out.print("> ");
                    mensagem = scan.next();
                    processOption(mensagem);
                }
            } while (!mensagem.equalsIgnoreCase("sair"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void processOption(String option) {
        String msg;
        String msg_cifrada;
        String hmac;
        String msg_rsa;
        switch (option) {
            case "3":
                msg = "3;";
                System.out.println("> CPF");
                System.out.print("> ");
                msg += this.scan.next() + ";";
                System.out.println("> Quantia");
                System.out.print("> ");
                msg += this.scan.next();
                msg_cifrada = this.seguranca.cifrar(msg);
                hmac = this.seguranca.hMac(msg);
                msg_rsa = this.rsa.cifragemCliente(msg_cifrada + ";" + hmac);
                enviar(msg_rsa);
                break;
            case "4":
                msg = "4;";
                System.out.println("> CPF");
                System.out.print("> ");
                msg += this.scan.next() + ";";
                System.out.println("> Quantia");
                System.out.print("> ");
                msg += this.scan.next();
                msg_cifrada = this.seguranca.cifrar(msg);
                hmac = this.seguranca.hMac(msg);
                msg_rsa = this.rsa.cifragemCliente(msg_cifrada + ";" + hmac);
                enviar(msg_rsa);
                break;
            case "5":
                msg = "5;";
                System.out.println("> Seu CPF");
                System.out.print("> ");
                msg += this.scan.next() + ";";
                System.out.println("> Enviar para qual CPF ?");
                System.out.print("> ");
                msg += this.scan.next() + ";";
                System.out.println("> Quantia");
                System.out.print("> ");
                msg += this.scan.next();
                msg_cifrada = this.seguranca.cifrar(msg);
                hmac = this.seguranca.hMac(msg);
                msg_rsa = this.rsa.cifragemCliente(msg_cifrada + ";" + hmac);
                enviar(msg_rsa);
                break;
            case "6":
                msg = "6;";
                System.out.println("> CPF");
                System.out.print("> ");
                msg += this.scan.next();
                msg_cifrada = this.seguranca.cifrar(msg);
                hmac = this.seguranca.hMac(msg);
                msg_rsa = this.rsa.cifragemCliente(msg_cifrada + ";" + hmac);
                enviar(msg_rsa);
                break;
            case "7":
                msg = "7;";
                System.out.println("> CPF");
                System.out.print("> ");
                msg += this.scan.next() + ";";
                System.out.println("> Quantia");
                System.out.print("> ");
                msg += this.scan.next() + ";";
                System.out.println("> Duração\n> 3 MÊSES\n> 6 MÊSES\n> 12 MÊSES");
                System.out.print("> ");
                msg += this.scan.next();
                msg_cifrada = this.seguranca.cifrar(msg);
                hmac = this.seguranca.hMac(msg);
                msg_rsa = this.rsa.cifragemCliente(msg_cifrada + ";" + hmac);
                enviar(msg_rsa);
                break;
            case "8":
                msg = "8;";
                System.out.println("> CPF");
                System.out.print("> ");
                msg += this.scan.next() + ";";
                System.out.println("> Quantia");
                System.out.print("> ");
                msg += this.scan.next() + ";";
                System.out.println("> Duração\n> 3 MÊSES\n> 6 MÊSES\n> 12 MÊSES");
                System.out.print("> ");
                msg += this.scan.next();
                msg_cifrada = this.seguranca.cifrar(msg);
                hmac = this.seguranca.hMac(msg);
                msg_rsa = this.rsa.cifragemCliente(msg_cifrada + ";" + hmac);
                enviar(msg_rsa);
                break;
            case "9":
                msg = "9;";
                System.out.println("> CPF");
                System.out.print("> ");
                msg += this.scan.next() + ";";
                System.out.println("> Quantia");
                System.out.print("> ");
                msg += this.scan.next() + ";";
                System.out.println("> Duração\n> 3 MÊSES\n> 6 MÊSES\n> 12 MÊSES");
                System.out.print("> ");
                msg += this.scan.next();
                msg_cifrada = this.seguranca.cifrar(msg);
                hmac = this.seguranca.hMac(msg);
                msg_rsa = this.rsa.cifragemCliente(msg_cifrada + ";" + hmac);
                enviar(msg_rsa);
                break;
            case "10":
                msg = "10;";
                System.out.println("> CPF");
                System.out.print("> ");
                msg += this.scan.next() + ";";
                System.out.println("> Quantia");
                System.out.print("> ");
                msg += this.scan.next() + ";";
                System.out.println("> Duração\n> 3 MÊSES\n> 6 MÊSES\n> 12 MÊSES");
                System.out.print("> ");
                msg += this.scan.next();
                msg_cifrada = this.seguranca.cifrar(msg);
                hmac = this.seguranca.hMac(msg);
                msg_rsa = this.rsa.cifragemCliente(msg_cifrada + ";" + hmac);
                enviar(msg_rsa);
                break;
            case "sair":
                System.out.println("Saindo");
                break;
            default:
                System.out.println("comando não achado");
                break;
        }
    }

    public void start() throws IOException, UnknownHostException {
        try {
            clientSocket = new ClientSocket(
                    new Socket(ENDERECO_SERVER, 1025));
            System.out
                    .println("Cliente conectado ao servidor de endereço = " + ENDERECO_SERVER + " na porta = " + 1025);
            new Thread(this).start();
            this.rsa.gerarPG();
            this.rsa.setN(this.rsa.getP()*this.rsa.getQ());
            this.rsa.gerarE();
            System.out.println("Enivando { p, q, e }");
            enviar("rsa_chaves;" + this.rsa.getP() + ";" + this.rsa.getQ() + ";" + this.rsa.getE());
            messageLoop();
        } finally {
            clientSocket.close();
        }
    }

}
