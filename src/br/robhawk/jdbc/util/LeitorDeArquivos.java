package br.robhawk.jdbc.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Scanner;

public class LeitorDeArquivos {

	private static Scanner leitor;

	private LeitorDeArquivos() {
	}

	public static final HashMap<String, String> leConfigBanco(String nome) {
		HashMap<String, String> conf = new HashMap<String, String>();

		InputStream inputStream = LeitorDeArquivos.class.getResourceAsStream(nome);
		BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream));
		leitor = new Scanner(buffer).useDelimiter("\\n");

		while (leitor.hasNext()) {
			String[] chaveMaisValor = leitor.next().split(":");

			conf.put(chaveMaisValor[0].trim(), chaveMaisValor[1].trim());
		}

		leitor.close();

		return conf;
	}
}
