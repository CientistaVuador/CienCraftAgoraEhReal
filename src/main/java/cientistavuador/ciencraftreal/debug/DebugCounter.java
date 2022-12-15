/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <https://unlicense.org>
 */
package cientistavuador.ciencraftreal.debug;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Cien
 */
public class DebugCounter {

    private final String name;
    
    private static class Counter {
        private final String name;
        
        private long start;
        private long end;
        private long[] measurements = new long[4];
        private int measurementsIndex = 0;
        
        public Counter(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
        
        public void markStart() {
            this.start = System.nanoTime();
        }
        
        public void markEnd() {
            this.end = System.nanoTime();
        }
        
        public void pushMeasurement() {
            if (measurementsIndex >= this.measurements.length) {
                this.measurements = Arrays.copyOf(this.measurements, this.measurements.length * 2);
            }
            
            this.measurements[measurementsIndex] = this.end - this.start;
            measurementsIndex++;
        }
        
        
        public int numberOfMeasurements() {
            return this.measurementsIndex;
        }
        
        public long calculateAverage() {
            if (this.measurementsIndex == 0) {
                return 0;
            }
            
            return sumMeasurements() / this.measurementsIndex;
        }
        
        public long sumMeasurements() {
            long sum = 0;
            for (int i = 0; i < this.measurementsIndex; i++) {
                sum += this.measurements[i];
            }
            return sum;
        }
    }
    
    private final List<Counter> countersList = new ArrayList<>();
    private final HashMap<String, Counter> counters = new HashMap<>();
    
    public DebugCounter(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public void clear() {
        this.countersList.clear();
        this.counters.clear();
    }
    
    public void markStart(String counterName) {
        Counter counter = counters.get(counterName);
        if (counter == null) {
            counter = new Counter(counterName);
            this.counters.put(counterName, counter);
            this.countersList.add(counter);
        }
        counter.markStart();
    }
    
    public void markEnd(String counterName) {
        Counter counter = counters.get(counterName);
        if (counter == null) {
            throw new IllegalArgumentException("No counter with name '"+counterName+"' found.");
        }
        counter.markEnd();
        counter.pushMeasurement();
    }
    
    private float nsToMs(long ns) {
        return ns / 1E6f;
    }
    
    private String format(float f) {
        return String.format("%.3f", f);
    }
    
    public void print(PrintStream out) {
        out.println("===Debug Counter ("+this.name+")===");
        try {
            if (this.countersList.isEmpty()) {
                out.println("This counter is empty.");
                return;
            }
            
            long total = 0;
            for (Counter counter:countersList) {
                total += counter.sumMeasurements();
            }
            
            for (Counter counter:countersList) {
                out.println(
                        new StringBuilder()
                                .append(counter.getName())
                                .append(" - ")
                                .append(counter.numberOfMeasurements())
                                .append(" measurements in ")
                                .append(format(nsToMs(counter.sumMeasurements())))
                                .append("ms ")
                                .append("(average: ")
                                .append(format(nsToMs(counter.calculateAverage())))
                                .append("ms; ")
                                .append(format((nsToMs(counter.sumMeasurements())/nsToMs(total)) * 100f))
                                .append("...% of total)")
                                .toString()
                );
            }
            out.println("=======");
            out.println(this.countersList.size()+" Counter(s), total time: "+format(nsToMs(total))+"ms");
        } finally {
            out.println("=======");
        }
    }
    
    public void print() {
        print(System.out);
    }
    
}

