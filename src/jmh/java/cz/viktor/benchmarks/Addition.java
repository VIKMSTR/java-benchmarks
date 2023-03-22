package cz.viktor.benchmarks;

import jdk.incubator.vector.*;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 50, time = 1)
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgsAppend = {"--add-modules=jdk.incubator.vector"})
//@Fork(value = 1)
public class Addition {

    private int[] TEST_DATA;

    @Setup
    public void setup() {
        TEST_DATA = IntStream.range(0, 10_000_000).toArray();
    }

    @Benchmark
    public void addUsingStreams() {
       var out = Arrays.stream(TEST_DATA).map(i -> i+i).toArray();
    }

    @Benchmark
    public void addUsingStreamsSequential() {
        var out = Arrays.stream(TEST_DATA).sequential().map(i -> i+i).toArray();
    }

    @Benchmark
    public void addUsingForLoop() {
        int[] result = new int[10_000_000];
        for (int i = 0; i < TEST_DATA.length ; i++) {
            result[i] = TEST_DATA[i] + TEST_DATA[i];
        }
    }

    @Benchmark
    public void addUsingVectorAPIMasked(){
        int result[] = new int[10_000_000];
        var species = IntVector.SPECIES_PREFERRED;

        for (int i = 0; i < species.loopBound(10_000_000) ; i+= species.length()) {
            var mask = species.indexInRange(i,TEST_DATA.length);
            var v1 = IntVector.fromArray(species,TEST_DATA,i, mask);
            var v2 = IntVector.fromArray(species,TEST_DATA,i, mask);
            v1.add(v2,mask).intoArray(result,i,mask);
        }
    }

    @Benchmark
    public void addUsingVectorAPINotMasked(){
        int result[] = new int[10_000_000];
        var species = IntVector.SPECIES_PREFERRED;
        int i = 0;

        for(; i < species.loopBound(TEST_DATA.length); i += species.length()) {
            var v1 = IntVector.fromArray(species,TEST_DATA, i);
            var v2 = IntVector.fromArray(species,TEST_DATA, i);
            v1.add(v2).intoArray(result,i);
        }
        //remaining data (not fitting into vector)
        for (int j = i; j < TEST_DATA.length; j++) {
            result[j] = TEST_DATA[j] + TEST_DATA[j];
        }
    }
}
