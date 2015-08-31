package br.robhawk.jdbc.conexao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

import br.robhawk.jdbc.util.LeitorDeArquivos;

public class Conector {

	private static Connection conexao;

	private Conector() {
	}

	public static final Connection getConexao() {
		if (conexao != null)
			return conexao;

		try {
			Class.forName("com.mysql.jdbc.Driver");

			HashMap<String, String> conf = LeitorDeArquivos.leConfigBanco("/config/banco.conf");

			String url = String.format("jdbc:mysql://%s:%s/%s", conf.get("servidor"), conf.get("porta"),
					conf.get("nome"));

			conexao = DriverManager.getConnection(url, conf.get("usuario"), conf.get("senha"));
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

		return conexao;
	}
}
