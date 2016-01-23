package br.robhawk.jdbc.conexao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

import br.robhawk.jdbc.util.LeitorConfiguracoesDatabase;

public class Conector {

	private static Connection CONEXAO;
	public static String URL_ARQUIVO_CONFIGURACOES = "";

	private Conector() {
	}

	public static final Connection getConexao() {
		if (CONEXAO != null)
			return CONEXAO;

		try {
			Class.forName("com.mysql.jdbc.Driver");

			HashMap<String, String> conf = LeitorConfiguracoesDatabase.getConfiguracoes();

			String url = String.format("jdbc:mysql://%s:%s/%s", conf.get("servidor"), conf.get("porta"),
					conf.get("nome"));

			CONEXAO = DriverManager.getConnection(url, conf.get("usuario"), conf.get("senha"));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return CONEXAO;
	}

}
