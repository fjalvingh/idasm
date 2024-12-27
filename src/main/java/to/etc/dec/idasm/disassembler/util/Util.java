package to.etc.dec.idasm.disassembler.util;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

final public class Util {
	private Util() {
	}

	public static <T, K> int binarySearch(List<? extends T> l, K key, Function<T, K> keyExtractor, Comparator<? super K> c) {
		int low = 0;
		int high = l.size()-1;

		while (low <= high) {
			int mid = (low + high) >>> 1;
			T midVal = l.get(mid);
			K kv = keyExtractor.apply(midVal);
			int cmp = c.compare(kv, key);

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1);  // key not found
	}
}
