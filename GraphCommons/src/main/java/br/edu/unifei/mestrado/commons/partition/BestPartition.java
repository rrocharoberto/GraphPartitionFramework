package br.edu.unifei.mestrado.commons.partition;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.partition.index.CutIndex;
import br.edu.unifei.mestrado.commons.partition.index.PartitionIndex;

/**
 * Classe utilizada para armazenar a melhor partição. Ela utiliza objetos de
 * {@link #PartitionIndex} e {@link #CutIndex}.
 */
public class BestPartition extends AbstractPartition {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public BestPartition(Integer level, int k, PartitionIndex indexPart, CutIndex indexCut) {
		super(level, k, indexPart, indexCut, true);
	}

	public void exportPartition(String fileName, String graphFileName, long timeSpent) {
		if (getLevel() != null) {
			fileName = fileName + "_level_" + getLevel();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
		StringBuffer bf = new StringBuffer();
		bf.append("\n Partition at " + sdf.format(new Date()) + "\n");
		bf.append(" Graph file: " + graphFileName + "\n");
		bf.append(" Time spent: " + timeSpent + " ms\n");
		bf.append(" Edge cut: " + getCutWeight() + "\n");
		bf.append(" k: " + getK() + "\n");
		bf.append(" Amount of nodes of each k: ");
		for (int i = 1; i <= getK(); i++) {
			bf.append(" k_" + i + ": " + getAmountOfNodesFromSet(i));
		}
		bf.append("\n Node weight of each k: ");
		for (int i = 1; i <= getK(); i++) {
			bf.append(" k_" + i + ": " + getTotalNodeWeightFromSet(i));
		}
		bf.append("\n End of partition at "+ sdf.format(new Date()) + "\n");
		
		logger.info("Saving file {} with content: {}", fileName, bf.toString());
		try {
			FileOutputStream fos = new FileOutputStream(fileName, true);
			fos.write(bf.toString().getBytes());
			fos.close();
		} catch (Exception e) {
			logger.error("Error exporting partition to file " + fileName, e);
		}
	}

	public void clear() {
		super.clearInternalIndexes();
	}
}
