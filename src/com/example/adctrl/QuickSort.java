package com.example.adctrl;

public class QuickSort {
	private int[] numbers;
 	private int number;
 	
 	private float[] f_numbers;
 	private long[] l_numbers;

 	public void sort(int[] values, int length) {
 		// Check for empty or null array
 		if (values ==null || values.length==0){
 			return;
 		}
 		this.numbers = values;
 		number = length;
 		quicksort(0, number - 1);
 	}
 	
 	public void sort(float[] values, int length) {
 		// Check for empty or null array
 		if (values ==null || values.length==0){
 			return;
 		}
 		this.f_numbers = values;
 		number = length;
 		f_quicksort(0, number - 1);
 	}
 	
 	public void sort(long[] values, int length) {
 		// Check for empty or null array
 		if (values ==null || values.length==0){
 			return;
 		}
 		this.l_numbers = values;
 		number = length;
 		l_quicksort(0, number - 1);
 	}

 	private void quicksort(int low, int high) {
 		int i = low, j = high;
 		// Get the pivot element from the middle of the list
 		float pivot = numbers[low + (high-low)/2];

 		// Divide into two lists
 		while (i <= j) {
 			// If the current value from the left list is smaller then the pivot
 			// element then get the next element from the left list
 			while (numbers[i] < pivot) {
 				i++;
 			}
 			// If the current value from the right list is larger then the pivot
 			// element then get the next element from the right list
 			while (numbers[j] > pivot) {
 				j--;
 			}

 			// If we have found a values in the left list which is larger then
 			// the pivot element and if we have found a value in the right list
 			// which is smaller then the pivot element then we exchange the
 			// values.
 			// As we are done we can increase i and j
 			if (i <= j) {
 				exchange(i, j);
 				i++;
 				j--;
 			}
 		}
 		// Recursion
 		if (low < j)
 			quicksort(low, j);
 		if (i < high)
 			quicksort(i, high);
 	}

 	private void exchange(int i, int j) {
 		int temp = numbers[i];
 		numbers[i] = numbers[j];
 		numbers[j] = temp;
 	}
 	
 	private void f_exchange(int i, int j) {
 		float temp = f_numbers[i];
 		f_numbers[i] = f_numbers[j];
 		f_numbers[j] = temp;
 	}
 	
 	private void l_exchange(int i, int j) {
 		long temp = l_numbers[i];
 		l_numbers[i] = l_numbers[j];
 		l_numbers[j] = temp;
 	}
 	
 	private void f_quicksort(int low, int high) {
 		int i = low, j = high;
 		float pivot = f_numbers[low + (high-low)/2];

 		while (i <= j) {
 			while (f_numbers[i] < pivot) {
 				i++;
 			}
 			while (f_numbers[j] > pivot) {
 				j--;
 			}

 			if (i <= j) {
 				f_exchange(i, j);
 				i++;
 				j--;
 			}
 		}
 		// Recursion
 		if (low < j)
 			f_quicksort(low, j);
 		if (i < high)
 			f_quicksort(i, high);
 	}
 	
 	private void l_quicksort(int low, int high) {
 		int i = low, j = high;
 		float pivot = l_numbers[low + (high-low)/2];

 		while (i <= j) {
 			while (l_numbers[i] < pivot) {
 				i++;
 			}
 			while (l_numbers[j] > pivot) {
 				j--;
 			}

 			if (i <= j) {
 				l_exchange(i, j);
 				i++;
 				j--;
 			}
 		}
 		// Recursion
 		if (low < j)
 			l_quicksort(low, j);
 		if (i < high)
 			l_quicksort(i, high);
 	}
}
