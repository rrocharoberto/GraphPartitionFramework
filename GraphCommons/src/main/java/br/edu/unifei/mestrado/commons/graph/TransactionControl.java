package br.edu.unifei.mestrado.commons.graph;


public class TransactionControl {

	/**
	 * Used to commit the transaction each N passes.
	 */
	public static final int COMMIT_INTERVAL = 1000;
	
	/**
	 * Graph to delegate the actions
	 */
	private TransactionInterface transaction;
	
	/**
	 * Keeps the number of commit requests.
	 */
	private int countCommit = 1;

	public TransactionControl(TransactionInterface transaction) {
		this.transaction = transaction;
	}
	
	public void beginTransaction() {
		transaction.beginTransaction();
		countCommit = 1;
	}
	
	public void intermediateCommit() {
		if(countCommit % COMMIT_INTERVAL == 0) {
			transaction.endTransaction();
			transaction.finish();
			transaction.beginTransaction();
			//logger.info("commit selectBestCellToMove: " + countCommit);
		}
		countCommit++;
	}
	
	public void commit() {
		transaction.endTransaction();
		transaction.finish();
	}
	
	public void rollback() {
		transaction.failure();
	}
}
