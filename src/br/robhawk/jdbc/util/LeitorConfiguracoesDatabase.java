package br.robhawk.jdbc.util;

import static br.robhawk.jdbc.conexao.Conector.URL_ARQUIVO_CONFIGURACOES;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import br.robhawk.jdbc.conexao.Conector;

public class LeitorConfiguracoesDatabase {

	public static String NOME_ARQUIVO = "banco.conf";

	private LeitorConfiguracoesDatabase() {
	}

	/**
	 * É esperado um arquivo - banco.conf se seguindo a convenção - com o
	 * seguinte conteúdo:<code><br>
	 * servidor:nome.do.servidor<br>
	 * porta:3306<br>
	 * nome:nome_do_banco<br>
	 * usuario:usuario<br>
	 * senha:senha <br>
	 * </code>
	 * 
	 * @return {@link HashMap mapa} de configurações.
	 */
	public static final HashMap<String, String> getConfiguracoes() {
		HashMap<String, String> conf = new HashMap<String, String>();

		try {
			BufferedReader file = new BufferedReader(new FileReader(getUrlArquivoConfiguracoes()));
			String linha;

			while ((linha = file.readLine()) != null) {
				String[] chaveValor = linha.split(":");
				conf.put(chaveValor[0], chaveValor[1]);
			}

			file.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (!conf.containsKey("servidor"))
			System.err.println("Falta config 'servidor'");
		if (!conf.containsKey("porta"))
			System.err.println("Falta config 'porta'");
		if (!conf.containsKey("nome"))
			System.err.println("Falta config 'nome'");
		if (!conf.containsKey("usuario"))
			System.err.println("Falta config 'usuario'");
		if (!conf.containsKey("senha"))
			System.err.println("Falta config 'senha'");

		return conf;
	}

	/**
	 * Busca arquivo de configurações do banco de dados:<br>
	 * 1-Busca conforme convenção: localizacao/do/projeto/ou/jar/banco.conf<br>
	 * 2-Busca conforme url informada em Conector.URL_ARQUIVO_CONFIGURACOES<br>
	 * 
	 * @return url em String
	 */
	public static final String getUrlArquivoConfiguracoes() {
		String url = "";
		String urlAlternativa = "";

		try {
			String path = Conector.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			String[] partsPath = path.split("/"); // para remover partes desnecessárias.

			for (int i = 1; i < partsPath.length - 1; i++)
				url += partsPath[i] + "/";
			url += NOME_ARQUIVO;

			if (new File(url).exists())
				return url;

			// volta um nível e tenta encontrar o arquivo convencional
			for (int i = 1; i < partsPath.length - 2; i++)
				urlAlternativa += partsPath[i] + "/";
			urlAlternativa += NOME_ARQUIVO;

			if (new File(urlAlternativa).exists())
				return urlAlternativa;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		if (!URL_ARQUIVO_CONFIGURACOES.isEmpty())
			return URL_ARQUIVO_CONFIGURACOES;

		throw new Error("Não foi possível localizar o arquivo com as configurações do banco de dados.\nURL: " + url
				+ "\nURL Alternativa: " + urlAlternativa);
	}
}
