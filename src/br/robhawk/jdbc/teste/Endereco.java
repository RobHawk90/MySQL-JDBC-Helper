package br.robhawk.jdbc.teste;

import br.robhawk.jdbc.reflection.Id;

public class Endereco {

	@Id
	private int idEndereco;
	private String rua;
	private String bairro;
	private String numero;
	private int idUsuario;

	public int getIdEndereco() {
		return idEndereco;
	}

	public void setIdEndereco(int idEndereco) {
		this.idEndereco = idEndereco;
	}

	public String getRua() {
		return rua;
	}

	public void setRua(String rua) {
		this.rua = rua;
	}

	public String getBairro() {
		return bairro;
	}

	public void setBairro(String bairro) {
		this.bairro = bairro;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public int getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(int idUsuario) {
		this.idUsuario = idUsuario;
	}

	@Override
	public String toString() {
		return "{idEndereco: " + idEndereco + ", rua: " + rua + ", numero: " + numero + ", bairro: " + bairro
				+ ", idUsuario: " + idUsuario + '}';
	}
}
