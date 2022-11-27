package gr.server.common;

public class QuickSortTest {

	public static void main(String[] args) {

		int[] nums = new int[] { 4, 6, 32, 2, 8, 1, 7, 0 };
		QuickSort s = new QuickSort();
		s.arrayToSort = nums;
		for (int i : s.arrayToSort) {

			System.out.print(i + ", ");
		}
		System.out.println();
		s.sort(0, nums.length - 1);

		for (int i : s.arrayToSort) {

			System.out.print(i + ", ");
					}
	}

	static class QuickSort {

		int[] arrayToSort;

		void sort(int low, int high) {
			if (low > high) {
				throw new RuntimeException("EEEEEEEEEE");
			}

			int sortFrom = low;
			int sortTo = high;
			int pivot = arrayToSort[sortFrom];
			System.out.println("PIVOT " + pivot);

			while (sortFrom <= sortTo) {// when they 'pass' each other the pivot is in its final position
				while (arrayToSort[sortFrom] < pivot) {//find a bigger at the left
					++sortFrom;
				}

				while (arrayToSort[sortTo] > pivot) {//find a smaller at the right
					--sortTo;
				}

				if (sortFrom <= sortTo) {//if they have not already 'passed' each other
					swap(sortFrom, sortTo);
					++sortFrom;
					--sortTo;
				}
			}

			if (sortTo > low) {//sort the left side
				sort(low, sortTo);
			}

			if (sortFrom < high) {//sort the right side
				sort(sortFrom, high);
			}

		}

		private void swap(int sortFrom, int sortTo) {
			int temp = arrayToSort[sortFrom];
			System.out.println("SWAP " + temp + " AND " + arrayToSort[sortTo]);
			arrayToSort[sortFrom] = arrayToSort[sortTo];
			arrayToSort[sortTo] = temp;
		}

	}

}
