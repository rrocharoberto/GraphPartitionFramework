package br.edu.unifei.mestrado.commons.graph;

public interface TransactionInterface {

	public void beginTransaction();

	public void endTransaction();
	
	public void failure();
	
	/**
	 * Commits the transaction if it was marked ok. Rolls back the transaction otherwise.
	 */
	public void finish();
}
