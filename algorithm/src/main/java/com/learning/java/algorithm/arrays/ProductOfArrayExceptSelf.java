package com.learning.java.algorithm.arrays;

import java.util.Arrays;

/**
 * Given an array of n integers where n > 1, nums, return an array output such that
 * output[i] is equal to the product of all the elements of nums except nums[i].
 *
 * Solve it without division and in O(n).
 *
 * For example, given [1,2,3,4], return [24,12,8,6].
 */
public class ProductOfArrayExceptSelf {

	public static void main(String[] args) {

		int[] nums = new int[] { 1, 2, 3, 4 };
		long startTime = System.nanoTime();
		int[] response = productExceptSelfON(nums);
		System.out.println("timeTaken -> " + (System.nanoTime() - startTime) + " array" + Arrays.toString(response));

	}

	private static int[] productExceptSelfON(int[] input) {
		int[] result = new int[input.length];

		result[input.length - 1] = 1;
		for (int i = input.length - 2; i >= 0; i--) {
			result[i] = result[i + 1] * input[i + 1];
		}

		int left = 1;
		for (int i = 0; i < input.length; i++) {
			result[i] = result[i] * left;
			left = left * input[i];
		}

		return result;
	}

}
