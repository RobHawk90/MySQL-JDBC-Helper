package br.robhawk.jdbc.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Scanner;

public class LeitorDeArquivos {

	private static Scanner leitor;

	private LeitorDeArquivos() {
	}

	public static final HashMap<String, String> leConfigBanco(String nome) {
		HashMap<String, String> conf = new HashMap<>();

		try {
			leitor = new Scanner(new FileReader(nome)).useDelimiter("\\n");

			while (leitor.hasNext()) {
				String[] chaveMaisValor = leitor.next().split(":");

				conf.put(chaveMaisValor[0].trim(), chaveMaisValor[1].trim());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		leitor.close();

		return conf;
	}
}
