package sim.app.episim.tissue.evaluation.tabledata;

import java.lang.reflect.Array;
import java.util.ArrayList;

public abstract class AbstractTable {

	public static <T> T[] mergeArrays(T[] array1, T[] array2, Class<?> cls) {
		int count = array1.length + array2.length;

		@SuppressWarnings("unchecked")
		T[] mergedArray = (T[]) Array.newInstance(cls, count);
		int i = 0;
		for (T val1 : array1) {
			mergedArray[i++] = val1;
		}
		for (T val2 : array2) {
			mergedArray[i++] = val2;
		}
		return mergedArray;
	}

	public abstract ArrayList<Double[]> getData();

	public abstract Column[] getColumn();
}
