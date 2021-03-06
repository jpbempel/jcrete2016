package mtm.demo.benchmark;

import demo.matrix.Matrix;
import demo.matrix.MatrixOp;
import demo.mtm.MatMultiThread;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class MatrixBenchmark {
 
    @State(Scope.Thread)
    public static class MyState {
        int size = 2000;
        Matrix a = new Matrix(size, size);
        Matrix b = new Matrix(size, size);
        Matrix result = new Matrix(size, size);
                      
       @Setup(Level.Trial)
        public void doSetup() {
            MatrixOp.randomize(a);
            MatrixOp.randomize(b);
        }        
        
    }

       
    @Benchmark @BenchmarkMode(Mode.Throughput)
    public void testSingleThreadedMultiplication(MyState state) {
        MatrixOp.multiply(state.a, state.b, state.result);
    }

    @Benchmark @BenchmarkMode(Mode.Throughput)
    public void testMultiThreadedMultiplication(MyState state) {
        MatMultiThread evenTask = new MatMultiThread(state.a, state.b, state.result, 0, 2);
        MatMultiThread oddTask = new MatMultiThread(state.a, state.b, state.result, 1, 2);
        
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        
        try {
            Matrix r = threadPool.submit(oddTask).get();
        } catch (InterruptedException ex) {
            Logger.getLogger(MatrixBenchmark.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(MatrixBenchmark.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        
        threadPool.shutdown();
        
    }    
    
    
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MatrixBenchmark.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .jvmArgs("-server")
                .resultFormat(ResultFormatType.CSV)
                .forks(1)                
                .build();

        new Runner(opt).run();
    }     
    
}
