package br.robhawk.jdbc.teste;

import br.robhawk.jdbc.conexao.Dao;

public class Main {

	public static void main(String[] args) {
		Dao dao = new Dao();
		/*Usuario usuario = new Usuario();
		usuario.setEmail("alsdashd@cuzao");
		usuario.setNome("robhawk");
		usuario.setSenha("1234");
		dao.insereOuAtualiza(usuario);
		System.out.println(usuario);

		Endereco endereco = new Endereco();
		endereco.setRua("Agenor de Paula");
		endereco.setNumero("158");
		endereco.setBairro("Vila Ipiranga");
		endereco.setIdUsuario(usuario.getId());

		dao.insere(endereco);
		System.out.println(endereco);

		usuario.setNome("teste rob");
		dao.insereOuAtualiza(usuario);

		// System.out.println(dao.delete(usuario));

		// System.out.println(dao.delete(Usuario.class, 10));*/
		
		System.out.println(dao.listaResultados(Endereco.class));
	}

}
