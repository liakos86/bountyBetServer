package gr.server.common;

import gr.server.common.MergeSortTest.MergeSort;

public class MergeSortTest {

	
	public static void main (String[]args) {
		MergeSort mergeSort = new MergeSort();
		int[] nums = new int[] { 4, 6, 32, 2, 8, 1, 7, 0 };
		mergeSort.ar = nums;
		
		mergeSort.sort();
		
		for (int i : mergeSort.ar) {

			System.out.print(i + ", ");
		}
		System.out.println();
	}
	
	
	
	static class MergeSort{
	
		static int[]ar;
	static	void sort() {
			mergeSort(ar, ar.length);
		}
	
	public static void mergeSort(int[] a, int n) {
		
		System.out.println("ARAY LEN " + a.length);
		
	    if (n < 2) {
	        return;
	    }
	    int mid = n / 2;
	    int[] l = new int[mid];
	    int[] r = new int[n - mid];

	    for (int i = 0; i < mid; i++) {
	        l[i] = a[i];
	    }
	    for (int i = mid; i < n; i++) {
	        r[i - mid] = a[i];
	    }
	    mergeSort(l, mid);
	    mergeSort(r, n - mid);

	    System.out.print("Merging left : ");
	    for (int i : l) {
			System.out.print(i + " ");
		}
	    System.out.print(" with  right : ");
	    for (int i : r) {
			System.out.print(i + " ");
		}
	    System.out.println();
	    
	    merge(a, l, r, mid, n - mid);
	}
	
	public static void merge(
			  int[] a, int[] l, int[] r, int left, int right) {
			 
			    int i = 0, j = 0, k = 0;
			    while (i < left && j < right) {
			        if (l[i] <= r[j]) {
			            a[k++] = l[i++];
			        }
			        else {
			            a[k++] = r[j++];
			        }
			    }
			    while (i < left) {
			        a[k++] = l[i++];
			    }
			    while (j < right) {
			        a[k++] = r[j++];
			    }
			}
	
	}
	
	
}
