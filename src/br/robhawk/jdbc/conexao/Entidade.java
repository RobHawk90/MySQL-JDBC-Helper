package br.robhawk.jdbc.conexao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Esta interface deve ser implementada nos models que representam tabelas do
 * banco de dados. A classe Dao é capaz de reconhecer esta interface,
 * possibilitando a construção de queries mais elaboradas.
 * 
 * @author Robert
 *
 * @param <T>
 */
public abstract class Entidade<T> extends Dao {

	public abstract T extrai(ResultSet rs);

	public List<T> listaResultados(String sql, Object... filtros) {
		List<T> objetos = new ArrayList<T>();

		try {
			PreparedStatement ps = prepara(sql, filtros);
			ResultSet rs = ps.executeQuery();

			while (rs.next())
				objetos.add(extrai(rs));

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return objetos;
	}

	public T getResultado(String sql, Object... filtros) {
		try {
			PreparedStatement ps = prepara(sql, filtros);
			ResultSet rs = ps.executeQuery();

			if (rs.next())
				return extrai(rs);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}
}
