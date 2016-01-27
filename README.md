# MySQL-JDBC-Helper
Crie CRUDs de maneira simples.

Este projeto é um utilitário para a comunicação entre projetos java com bancos de dados MySQL. Para usá-lo, basta realizar os seguintes passos: <br>
1 - Criar o arquivo "banco.conf" na pasta do seu projeto ou na mesma pasta do seu .jar com as seguintes informações:
<p>
<code>servidor:nome.do.servidor</code><br>
<code>porta:3306</code><br>
<code>nome:nome_do_banco</code><br>
<code>usuario:usuario</code><br>
<code>senha:senha</code><br>
</p>
<br>
2 - Criar o seu model e mapea-lo como a seguir:
<div>
  <pre>
    @Tabela(nome = "pessoas") // ou somente @Tabela se o nome da classe for o mesmo no BD.
    public class Pessoa {
    
        @Id(nome = "idPessoa") // ou somente @Id se o nome do atributo for o mesmo no BD.
        public int id;
        public String nome;
    
        public Pessoa() {} // É obrigatório ter um construtor sem argumentos.
    
        public Pessoa(String nome) {
            this.nome = nome;
        }
    
    }
  </pre>
</div>
3 - Usar a classe Dao:
<pre>
    Pessoa pessoa = new Pessoa("Exemplo");
    Dao dao = new Dao();
    dao.insere(pessoa);
    System.out.println(pessoa.id); // Trás a chave criada no banco de dados!
</pre>
<br>

E muito mais (:
