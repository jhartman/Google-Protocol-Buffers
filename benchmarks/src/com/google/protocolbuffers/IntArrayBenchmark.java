package com.google.protocolbuffers;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import benchmarks.PrimitiveTypes.IntList;
import benchmarks.PrimitiveTypes.IntList.Builder;

public class IntArrayBenchmark {
	private static final Random RANDOM = new Random(123456);
	
	private static int[] generateRandomArray(int length) {
		final int[] array = new int[length];
		for(int i = 0; i < array.length; i++) array[i] = RANDOM.nextInt(1000000);
		return array;
	}
	
	private static List<Integer> toList(int[] array) {
		final List<Integer> list = new ArrayList<Integer>(array.length);
		for(int i = 0; i < array.length; i++) list.add(array[i]);
		return list;
	}
	
	private void print(PrintStream stream, IntListSerializer serializer, String methodName, long result) {
		if(stream != null) {
			String name = serializer.getClass().getSimpleName() + "." + methodName;
			int size = 75 - name.length();
			stream.printf("%s %" + size + "d\n", name, result);
		}
	}
	
	public void test(final int [] array, final int numIterations, final PrintStream writer) {
		final List<Integer> list = toList(array);
		final Set<Integer> set = new HashSet<Integer>(list);
		
		List<IntListSerializer> serializers = new ArrayList<IntListSerializer> ();
		serializers.add(new DefaultSerializer());
		serializers.add(new PreAllocationSerializer());
		
		
		if(writer != null) writer.println("Size: " + array.length + "\t\t Iterations: " + numIterations);
		
		for(final IntListSerializer serializer : serializers) {
			
			print(writer, serializer, "serializeArray", new BenchmarkClosure(numIterations) {
				protected void run0() {
					serializer.serializeArray(array);
				}
			}.run());
			
			print(writer, serializer, "serializeCollection1", new BenchmarkClosure(numIterations) {
				protected void run0() {
					serializer.serializeCollection1(set);
				}
			}.run());
			
			print(writer, serializer, "serializeCollection2", new BenchmarkClosure(numIterations) {
				protected void run0() {
					serializer.serializeCollection2(set);
				}
			}.run());
			
			print(writer, serializer, "serializeList", new BenchmarkClosure(numIterations) {
				protected void run0() {
					serializer.serializeList(list);
				}
			}.run());

		}
	}
	
	public static void main(String[] args) {
		final int LENGTH = Integer.parseInt(args[0]);
		final int NUM_ITERATIONS = Integer.parseInt(args[1]);
		
		final int [] array = generateRandomArray(LENGTH);
		IntArrayBenchmark benchmark = new IntArrayBenchmark();
		benchmark.test(array, NUM_ITERATIONS, System.out);
	}	
}
abstract class BenchmarkClosure {
	private final int numIterations;
	private final int numWarmups;
	
	public BenchmarkClosure(int numIterations) {
		this.numIterations = numIterations;
		this.numWarmups = 100;
	}
	
	private long findMedian(long[] nums) {
		Arrays.sort(nums);
		return nums[nums.length / 2];
	}
	
	private long findTotal(long[] nums) {
		long sum = 0;
		for(long num : nums) sum += num;
		return sum;
	}
	
	private long findAverage(long[] nums) {
		return (int)(findTotal(nums) / nums.length);
	}
	
	public long run() {
		long[] times = new long[numIterations];
		
		for(int warmup = 0; warmup < numWarmups; warmup++) run0();
//		System.gc();
//		try {
//			Thread.currentThread().sleep(2000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		long beforeAll = System.currentTimeMillis();
		for(int iteration = 0; iteration < numIterations; iteration++) {
			long beforeTest = System.nanoTime();
			run0();
			times[iteration] = (System.nanoTime() - beforeTest);
		}
//		long total = System.currentTimeMillis() - beforeAll;
		return findMedian(times);
//		return total;
	}
	
	protected abstract void run0();
}

interface IntListSerializer {
	public IntList serializeArray(int[] array);
	public IntList serializeList(List<Integer> list);
	public IntList serializeCollection1(Collection<Integer> collection);
	public IntList serializeCollection2(Collection<Integer> collection);
}

class DefaultSerializer implements IntListSerializer {
	public IntList serializeArray(int[] array) {
		final Builder builder = IntList.newBuilder();
		for(int i = 0; i < array.length; i++)
			builder.addItems(array[i]);
		return builder.build();
	}
	
	public IntList serializeList(List<Integer> list) {
		final Builder builder = IntList.newBuilder();
		builder.addAllItems(list);
		return builder.build();
	}
	
	public IntList serializeCollection1(Collection<Integer> collection) {
		final Builder builder = IntList.newBuilder();
		builder.addAllItems(collection);
		return builder.build();
	}

	public IntList serializeCollection2(Collection<Integer> collection) {
		final Builder builder = IntList.newBuilder();
		for(Integer item : collection) builder.addItems(item);
		return builder.build();
	}
	
}

class PreAllocationSerializer implements IntListSerializer {
	public IntList serializeArray(int[] array) {
		final Builder builder = IntList.newBuilder();
		builder.ensureItemsCapacity(array.length);
		for(int i = 0; i < array.length; i++)
			builder.addItems(array[i]);
		return builder.build();
	}
	
	public IntList serializeList(List<Integer> list) {
		final Builder builder = IntList.newBuilder();
		builder.ensureItemsCapacity(list.size());
		builder.addAllItems(list);
		return builder.build();
	}
	
	public IntList serializeCollection1(Collection<Integer> collection) {
		final Builder builder = IntList.newBuilder();
		builder.ensureItemsCapacity(collection.size());
		for(Integer item : collection) builder.addItems(item);
		return builder.build();
	}
	
	public IntList serializeCollection2(Collection<Integer> collection) {
		final Builder builder = IntList.newBuilder();
		builder.ensureItemsCapacity(collection.size());
		builder.addAllItems(collection);
		return builder.build();
	}

}
