package br.edu.unifei.mestrado.commons.graph;

import br.edu.unifei.mestrado.commons.partition.index.CutIndex;
import br.edu.unifei.mestrado.commons.partition.index.PartitionIndex;

public interface PartitionKeeper {

	public void createNewCutIndex();

	public void createNewPartitionIndex();

	public CutIndex getCurrentCutIndex();

	public PartitionIndex getCurrentPartitionIndex();

}
