package br.edu.unifei.mestrado.greedy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.partition.AbstractPartition;
import br.edu.unifei.mestrado.commons.partition.index.CutIndex;
import br.edu.unifei.mestrado.commons.partition.index.PartitionIndex;


public class KWayPartition extends AbstractPartition {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public KWayPartition(Integer level, int k, PartitionIndex partitionIdx, CutIndex cutIdx) {
		super(level, k, partitionIdx, cutIdx);
	}

}
