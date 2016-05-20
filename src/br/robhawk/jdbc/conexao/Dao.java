package br.robhawk.jdbc.conexao;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;

import br.robhawk.jdbc.reflection.Campo;
import br.robhawk.jdbc.reflection.Id;
import br.robhawk.jdbc.reflection.Tabela;

public class Dao {

	private ResultSet rs; // guarda as PK geradas nos inserts
	public static final boolean EXIBE_SQL = false;

	/**
	 * Executa o SQL (insert, delete, update...) com os valores, previnindo o
	 * SQL Injection
	 * 
	 * @param sql
	 *            com os parâmetros '?'
	 * @param valores
	 *            que substituem as '?' do sql
	 * @return sucesso ou falha
	 * @throws SQLException
	 */
	public boolean executa(String sql, Object... valores) {
		try {
			PreparedStatement ps = prepara(sql, valores);

			ps.execute();

			rs = ps.getGeneratedKeys();

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public ResultSet query(String sql, Object... valores) {
		try {
			PreparedStatement ps = prepara(sql, valores);

			return ps.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Executa um insert se o valor do campo anotado como Id do objeto for
	 * vazio, ou um update caso contrário.
	 * 
	 * @param objeto
	 * @return se foi possível executar a transação - true ou false.
	 */
	public <T> boolean insereOuAtualiza(T objeto) {
		Object id = getValorCampoId(objeto);

		if (id == null || id.equals(0))
			return insere(objeto);
		else
			return atualiza(objeto);

		// TODO melhorar os metodos insert e update: código repetido deve ser
		// extraído para uma função
	}

	/**
	 * Insere lista de objetos de uma só vez.
	 * 
	 * @param objetos
	 * @return
	 */
	public <T> boolean insere(List<T> objetos) {
		Class<? extends Object> classe = objetos.get(0).getClass();

		// parâmetros para a criação do insert
		String tabela = getNomeTabela(classe);
		List<Field> camposInsert = new ArrayList<Field>();
		List<String> nomesCampos = new ArrayList<String>();
		Field campoId = null;

		Field[] campos = classe.getDeclaredFields();
		Field.setAccessible(campos, true);

		for (Field campo : campos) {
			if (campo.isAnnotationPresent(Id.class))
				campoId = campo;
			else {
				camposInsert.add(campo);
				nomesCampos.add(extraiNome(campo));
			}
		}

		String sql = "INSERT INTO " + tabela + " (";
		for (String nomeCampo : nomesCampos)
			sql += nomeCampo + ", ";
		sql = sql.substring(0, sql.length() - 2) + ") VALUES ";

		String valuesClause = "(";
		for (int i = 0; i < nomesCampos.size(); i++)
			valuesClause += "?, ";
		valuesClause = valuesClause.substring(0, valuesClause.length() - 2) + ")";

		for (int i = 0; i < objetos.size(); i++)
			sql += valuesClause + ", ";
		sql = sql.substring(0, sql.length() - 2);

		int params = 0;

		try {
			PreparedStatement ps = Conector.getConexao().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

			for (T objeto : objetos)
				for (Field campo : camposInsert)
					ps.setObject(++params, extraiValor(campo, objeto));

			if (EXIBE_SQL)
				System.out.println(ps.toString().split(":", 1)[1]);

			ps.execute();
			ResultSet rs = ps.getGeneratedKeys();

			int idsGerados = 0;

			while (rs.next()) {
				T objeto = objetos.get(idsGerados++);
				campoId.set(objeto, rs.getInt("GENERATED_KEY"));
			}

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Constrói um insert automaticamente e insere no banco de dados.
	 * 
	 * @param objeto
	 *            a ser inserido no banco de dados.
	 * @return objeto inserido com id.
	 */
	public <T> boolean insere(T objeto) {
		Class<? extends Object> classe = objeto.getClass();

		// parâmetros para a criação do insert
		String tabela = getNomeTabela(classe);
		List<String> nomesCampos = new ArrayList<String>();
		List<Object> valoresCampos = new ArrayList<Object>();
		Field campoId = null;

		// obtém os metadados para extraír os parâmetros
		Field[] campos = classe.getDeclaredFields();
		Field.setAccessible(campos, true); // acessa atributos privados

		for (Field campo : campos) {
			// se for id, será o campo que irá guardar a chave primária gerada
			// após o insert,
			// senão será manipulado como um campo comum na criação do SQL
			if (campo.isAnnotationPresent(Id.class))
				campoId = campo;
			else {
				String nome = extraiNome(campo);
				Object valor = extraiValor(campo, objeto);

				nomesCampos.add(nome);
				valoresCampos.add(valor);
			}
		}

		// constrói o insert
		String sql = constroiInsertSql(tabela, nomesCampos);

		// executa o insert
		boolean sucesso = executa(sql, valoresCampos.toArray());

		// se executou o insert, atribui o id gerado no objeto
		if (sucesso)
			setId(campoId, objeto);

		return sucesso;
	}

	public <T> boolean atualiza(T objeto) {
		Class<? extends Object> classe = objeto.getClass();

		// parâmetros para a criação do insert
		String tabela = getNomeTabela(classe);
		List<String> nomesCampos = new ArrayList<String>();
		List<Object> valoresCampos = new ArrayList<Object>();

		// obtém os metadados para extraír os parâmetros
		Field[] campos = classe.getDeclaredFields();
		Field.setAccessible(campos, true); // acessa atributos privados

		for (Field campo : campos) {
			// se for id, será o campo que irá guardar a chave primária gerada
			// após o insert,
			// senão será manipulado como um campo comum na criação do SQL
			if (!campo.isAnnotationPresent(Id.class)) {
				String nome = extraiNome(campo);
				Object valor = extraiValor(campo, objeto);

				nomesCampos.add(nome);
				valoresCampos.add(valor);
			}
		}

		valoresCampos.add(getValorCampoId(objeto));

		// constrói o update
		String sql = constroiUpdateSql(tabela, nomesCampos, getNomeCampoId(classe));

		// executa o update
		return executa(sql, valoresCampos.toArray());
	}

	public <T> boolean delete(T objeto) {
		Class<? extends Object> classe = objeto.getClass();

		Object id = getValorCampoId(objeto);

		return delete(classe, id);
	}

	/**
	 * Concatena um comando SQL delete e o executa.
	 * 
	 * @param classe
	 *            que representa uma tabela do banco de dados.
	 * @param id
	 *            para o filtro da clausula where.
	 * @return
	 */
	public boolean delete(Class<?> classe, Object id) {
		String nomeTabela = getNomeTabela(classe);
		String nomeId = getNomeCampoId(classe);

		String sql = "DELETE FROM " + nomeTabela + " WHERE " + nomeId + " = ?";

		return executa(sql, id);
	}

	public <T> T getResultado(Class<T> tipoClasse, Object id) {
		Field[] campos = tipoClasse.getDeclaredFields();

		String sql = "SELECT * FROM " + getNomeTabela(tipoClasse) + " WHERE " + getNomeCampoId(tipoClasse) + " = ?";

		try {
			PreparedStatement ps = prepara(sql, id);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				T objeto = tipoClasse.newInstance();

				for (Field campo : campos) {
					if (campo.isAnnotationPresent(Id.class))
						campo.set(objeto, rs.getObject(getNomeCampoId(tipoClasse)));
					else
						campo.set(objeto, rs.getObject(campo.getName()));
				}

				return objeto;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return null;
	}

	// TODO refatorar isto
	public <T> List<T> listaResultados(Class<T> tipoClasse) {
		List<T> objetos = new ArrayList<T>();

		Field[] campos = tipoClasse.getDeclaredFields();
		Field.setAccessible(campos, true);

		String sql = "SELECT * FROM " + getNomeTabela(tipoClasse);

		try {
			PreparedStatement ps = prepara(sql);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				T objeto = tipoClasse.newInstance();

				for (Field campo : campos) {
					if (campo.isAnnotationPresent(Id.class))
						campo.set(objeto, rs.getObject(getNomeCampoId(tipoClasse)));
					else
						campo.set(objeto, rs.getObject(campo.getName()));
				}

				objetos.add(objeto);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return objetos;
	}

	/**
	 * Constrói o SQL com os valores, previnindo o SQL Injection
	 * 
	 * @param sql
	 *            com os parâmetros '?'
	 * @param valores
	 *            que subistituem as '?' do sql
	 * @return {@link PreparedStatement}
	 * @throws SQLException
	 */
	protected PreparedStatement prepara(String sql, Object... valores) throws SQLException {
		Connection conexao = Conector.getConexao();

		PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

		for (int i = 0; i < valores.length; i++)
			ps.setObject(i + 1, valores[i]);

		if (EXIBE_SQL)
			System.out.println(ps.toString().split(":", 1)[1]); // exibe SQL completo

		return ps;
	}

	/**
	 * @return id gerado na execução de um insert
	 */
	private int getIdGerado() {
		try {
			if (rs != null && rs.next())
				return rs.getInt("GENERATED_KEY");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return 0;
	}

	private <T> Object getValorCampoId(T objeto) {
		Class<? extends Object> classe = objeto.getClass();
		Field campoId = getCampoId(classe);
		return extraiValor(campoId, objeto);
	}

	/**
	 * Verifica a anotação do campo Id para obter seu nome.
	 * 
	 * @param classe
	 *            que representa uma tabela do banco de dados.
	 * @return
	 */
	private String getNomeCampoId(Class<?> classe) {
		Field campoId = getCampoId(classe);
		String nomeCampoId = campoId.getName();
		String nomeAnotado = campoId.getAnnotation(Id.class).nome();

		if (nomeAnotado.isEmpty())
			return nomeCampoId;

		return nomeAnotado;
	}

	/**
	 * Percorre todos os campos declarados na classe para obter o primeiro
	 * anotado como Id.
	 * 
	 * @param classe
	 *            que representa uma tabela do banco de dados.
	 * @return
	 */
	private Field getCampoId(Class<?> classe) {
		Field[] campos = classe.getDeclaredFields();

		// permite acesso aos campos privados.
		Field.setAccessible(campos, true);

		for (Field campo : campos)
			if (campo.isAnnotationPresent(Id.class))
				return campo;

		throw new Error("Não há um campo anotado como Id na classe " + classe.getName());
	}

	/**
	 * Obtém nome que representa uma tabela do banco de dados, verificando os
	 * metadados da classe.
	 * 
	 * @param classe
	 *            que contém o nome de uma tabela do banco de dados.
	 * @return nome da tabela do banco de dados.
	 */
	private String getNomeTabela(Class<? extends Object> classe) {
		String nomeTabela = classe.getSimpleName();

		if (classe.isAnnotationPresent(Tabela.class)) {
			Tabela tabelaAnnotation = classe.getAnnotation(Tabela.class);
			nomeTabela = tabelaAnnotation.nome();
		}

		return nomeTabela;
	}

	/**
	 * Atribui o id gerado após um insert no objeto.
	 * 
	 * @param campoId
	 * @param objeto
	 */
	private <T> void setId(Field campoId, T objeto) {
		if (campoId == null || objeto == null)
			throw new Error("Os parâmetros 'campoId' e 'objeto' não podem ser nulos");

		try {
			campoId.set(objeto, getIdGerado());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constrói um SQL de insert concatenando os dados passados por parâmetro
	 * 
	 * @param tabela
	 * @param nomesCampos
	 * @return SQL de insert
	 */
	private String constroiInsertSql(String tabela, List<String> nomesCampos) {
		String sql = "INSERT INTO " + tabela + " (";

		String valuesParam = ") VALUES (";

		// concatena campos e parâmetros que serão substituídos
		for (String nome : nomesCampos) {
			sql += nome + ",";
			valuesParam += "?,";
		}

		// retira a vírgula que sobrou da concatenação acima
		valuesParam = valuesParam.substring(0, valuesParam.length() - 1) + ")";

		sql = sql.substring(0, sql.length() - 1) + valuesParam;

		return sql;
	}

	private String constroiUpdateSql(String tabela, List<String> nomesCampos, String nomeId) {
		String sql = "UPDATE " + tabela + " SET ";

		// concatena campos e parâmetros que serão substituídos
		for (String nome : nomesCampos)
			sql += nome + " = ?,";

		String where = " WHERE " + nomeId + " = ?";

		sql = sql.substring(0, sql.length() - 1) + where;

		return sql;
	}

	/**
	 * Obtém nome que representa um campo de uma tabela do banco de dados,
	 * verificando os metadados da classe.
	 * 
	 * @param campo
	 *            que contém o nome da coluna de uma tabela do banco de dados.
	 * @return nome da coluna de uma tabela do banco de dados.
	 */
	private String extraiNome(Field campo) {
		String nome = campo.getName();

		if (campo.isAnnotationPresent(Campo.class)) {
			Campo campoAnnotation = campo.getAnnotation(Campo.class);
			nome = campoAnnotation.nome();
		}

		return nome;
	}

	/**
	 * Extrai valor de um atributo de um objeto.
	 * 
	 * @param campo
	 * @param objeto
	 * @return
	 */
	private <T> Object extraiValor(Field campo, T objeto) {
		try {
			return campo.get(objeto);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return null;
	}

}
