package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

public class Day20 {

    private record Pulse(String sender, BitSet pulse) { }

    private record PulseSendResult(long lowPulses, long highPulses, Queue<String> receipients) { }

    private abstract class Module {

        private String name;
        private Queue<Pulse> receivedPulses;
        private String[] destinations;

        private Module(String name, Queue<Pulse> receivedPulses, String[] destinations) {
            this.name = name;
            this.receivedPulses = receivedPulses;
            this.destinations = destinations;
        }

        String getName() {
            return this.name;
        }

        String[] getDestinations() {
            return this.destinations;
        }

        void putPulse(Pulse pulse) {
            receivedPulses.add(pulse);
        }

        Pulse getPulse() {
            return this.receivedPulses.poll();
        }

        /**
         * Send a low or high pulse to all of the module's
         * destinations.
         *
         * @return SendReult which contains the number of low and high
         * pulses sent, as well a queue of destinations
         */
        abstract PulseSendResult sendPulse();
    }

    /**
     * Flip-flop modules (prefix %) are either on or off; they are initially off.
     * If a flip-flop module receives a high pulse, it is ignored and nothing happens.
     * However, if a flip-flop module receives a low pulse, it flips between on and off.
     * If it was off, it turns on and sends a high pulse.
     * If it was on, it turns off and sends a low pulse.
     */
    private class FlipFlop extends Module {

        private final BitSet state;
        private final Map<String, Module> modules;

        private FlipFlop(String name, String[] destinations, Map<String, Module> modules) {
            super(name, new ArrayDeque<>(), destinations);
            this.state = new BitSet(1);
            this.state.clear(0);
            this.modules = modules;
        }

        @Override
        public PulseSendResult sendPulse() {
            final Pulse message = getPulse();
            if (message.pulse().get(0)) {
                return new PulseSendResult(0, 0, new ArrayDeque<>());
            }

            final BitSet pulse = new BitSet(1);
            pulse.clear(0);

            // flip-flip ON, so turn OFF
            if(state.get(0)) {
                state.clear(0);
            } else { // flip-flop OFF, so turn ON
                state.set(0);
                pulse.set(0);
            }

            final String[] destinations = getDestinations();
            final Queue<String> sent = new ArrayDeque<>();

            for (final String moduleName : destinations) {
                final Module module = modules.get(moduleName);
                module.putPulse(new Pulse(getName(), pulse));
                sent.add(moduleName);
            }

            if (state.get(0)) {
                return new PulseSendResult(0, destinations.length, sent);
            }

            return new PulseSendResult(destinations.length, 0, sent);
        }

    }

    /**
     * Conjunction modules (prefix &) remember the type of the most recent pulse
     * received from each of their connected input modules; they initially default
     * to remembering a low pulse for each input. When a pulse is received, the
     * conjunction module first updates its memory for that input. Then, if it
     * remembers high pulses for all inputs, it sends a low pulse; otherwise, it
     * sends a high pulse.
     */
    private class Conjuction extends Module {

        private final Map<String, Module> modules;
        private List<String> arrivals;
        private Map<String, BitSet> memory;

        private Conjuction(String name, String[] destinations, Map<String, Module> modules) {
            super(name, new ArrayDeque<>(), destinations);
            this.modules = modules;
            this.memory = new HashMap<>();
        }

        private void setArrivals(List<String> receivingFrom) {
            this.arrivals = new ArrayList<>(receivingFrom);
            for (final String moduleName : arrivals) {
                memory.put(moduleName, new BitSet(1));
            }
        }

        @Override
        public PulseSendResult sendPulse() {
            Pulse pulse = getPulse();
            while (pulse != null) {
                memory.put(pulse.sender(), pulse.pulse());
                pulse = getPulse();
            }

            boolean allHigh = true;
            for (final Entry<String, BitSet> e : memory.entrySet()) {
                if (!e.getValue().get(0)) {
                    allHigh = false;
                    break;
                }
            }

            final BitSet high = new BitSet(1);
            high.set(0);

            final String[] destinations = getDestinations();
            final Pulse p = allHigh ? new Pulse(getName(), new BitSet(1)) : new Pulse(getName(), high);
            final Queue<String> sent = new ArrayDeque<>();

            for (final String moduleName : destinations) {
                Module m = modules.get(moduleName);
                // only 1 module (rx) which is only
                // receives pulses and does not send any.
                // Relevant for part 2
                if (m == null) {
                    modules.put(moduleName, new NoOp());
                    m = modules.get(moduleName);
                }
                m.putPulse(p);
                sent.add(moduleName);
            }

            if (allHigh) {
                return new PulseSendResult(destinations.length, 0, sent);
            }

            return new PulseSendResult(0, destinations.length, sent);
        }
    }

    /**
     * Broadcast module (named broadcaster). When it receives a pulse, it sends
     * the same pulse to all of its destination modules.
     */
    private class Broadcaster extends Module {

        private final Map<String, Module> modules;

        private Broadcaster(String name, String[] destinations, Map<String, Module> modules) {
            super(name, new ArrayDeque<>(), destinations);
            this.modules = modules;
        }

        @Override
        public PulseSendResult sendPulse() {
            final String[] destinations = getDestinations();
            final Queue<String> sent = new ArrayDeque<>();

            for (final String moduleName : destinations) {
                final Module module = modules.get(moduleName);
                module.putPulse(new Pulse(getName(), new BitSet(1)));
                sent.add(moduleName);
            }

            return new PulseSendResult(destinations.length, 0, sent);
        }
    }

    private class NoOp extends Module {

        private NoOp() {
            super("noop", new ArrayDeque<>(), null);
        }

        @Override
        PulseSendResult sendPulse() {
            return new PulseSendResult(0, 0, new ArrayDeque<>());
        }
        
    }

    private Map<String, Module> buildModules(List<String> data) {
        final Map<String, Module> modules = new HashMap<>();
        final Map<String, List<String>> receivingFrom = new HashMap<>();

        for (int i = 0; i < data.size(); i++) {
            final String[] parts = data.get(i).split(" -> ");
            final char type = parts[0].charAt(0);

            String name = parts[0];
            if (!"broadcaster".equals(name)) {
                name = parts[0].substring(1);
            }

            final String[] destinations =
                Arrays.stream(parts[1].split(",")).map(String::trim).toArray(String[]::new);

            for (final String dest : destinations) {
                final List<String> list;
                if (receivingFrom.containsKey(dest)) {
                    list = receivingFrom.get(dest);
                } else {
                    list = new ArrayList<>();
                    receivingFrom.put(dest, list);
                }

                list.add(name);
            }

            if ("broadcaster".equals(name)) {
                final Module broadcaster = new Broadcaster(name, destinations, modules);
                modules.put(name, broadcaster);
                continue;
            }

            switch(type) {
                case '%':
                    modules.put(name, new FlipFlop(name, destinations, modules));
                    break;
                case '&':
                    modules.put(name, new Conjuction(name, destinations, modules));
                    break;
                default:
                    throw new RuntimeException("Unknown module type: " + name.charAt(0));
            }
        }

        modules.entrySet()
            .stream()
            .filter((e) -> e.getValue() instanceof Conjuction)
            .forEach((e) -> {
                final String name = e.getKey();
                final Module m = e.getValue();
                ((Conjuction)m).setArrivals(receivingFrom.get(name));
            });

        return modules;
    }

    private long perform1000ButtonPushes(Map<String, Module> modules) {
        int buttonPushes = 1000;
        final Queue<String> workQueue = new ArrayDeque<>();
        long lowPulses = 0;
        long highPulses = 0;

        while (buttonPushes != 0) {
            workQueue.add("broadcaster");
            while (!workQueue.isEmpty()) {
                final String moduleName = workQueue.poll();
                final Module m = modules.get(moduleName);
                final PulseSendResult sendResult = m.sendPulse();
                lowPulses += sendResult.lowPulses();
                highPulses += sendResult.highPulses();
                workQueue.addAll(sendResult.receipients());
            }
            --buttonPushes;
        }

        return (lowPulses + 1000) * highPulses;
    }

    private long gcd(long x, long y) {
        if (y == 0) {
            return Math.abs(x);
        }

        return gcd(y, x % y);
    }

    private long lcm(List<Long> numbers) {
        long lcm = 1;
        for (final long n : numbers) {
            lcm = (lcm * n)/gcd(lcm, n);
        }

        return lcm;
    }

    private long performButtonPushesUntilRX(Map<String, Module> modules) {
        final Queue<String> workQueue = new ArrayDeque<>();
        long pushes = 1;

        final Map<String, List<Long>> cycleDetector = new HashMap<>();
        // input analysis
        // these are the input modules for module 'tj'
        // 'tj' is the input module for 'rx'
        cycleDetector.put("sk", new ArrayList<>());
        cycleDetector.put("kk", new ArrayList<>());
        cycleDetector.put("vt", new ArrayList<>());
        cycleDetector.put("xc", new ArrayList<>());

        while (true) {
            workQueue.add("broadcaster");
            while (!workQueue.isEmpty()) {
                final String moduleName = workQueue.poll();
                final Module m = modules.get(moduleName);
                final PulseSendResult sendResult = m.sendPulse();
                final long highPulses = sendResult.highPulses();

                if (sendResult.receipients().contains("tj") && highPulses > 0) {
                    final List<Long> pushList = cycleDetector.get(moduleName);
                    pushList.add(pushes);
                }

                if (cycleDetector.values().stream().allMatch((l) -> l.size() == 10)) {
                    final Set<Long> seen = new HashSet<>();
                    final List<Long> intervals = new ArrayList<>();

                    for (final List<Long> lz : cycleDetector.values()) {
                        for (int i = 0; i < lz.size() - 1; i++) {
                            final long diff = lz.get(i+1) - lz.get(i);
                            final boolean notExists = seen.add(diff);
                            if (!notExists) {
                                intervals.add(diff);
                                seen.clear();
                                break;
                            }
                        }
                    }

                    return lcm(intervals);
                }

                workQueue.addAll(sendResult.receipients());
            }
            ++pushes;
        }
    }

    public long findProductOfLowAndHighPulsesAfter1000ButtonPushes(List<String> data) {
        final Map<String, Module> modules = buildModules(data);
        return perform1000ButtonPushes(modules);
    }

    public long findFewestNumberOfButtonPressesToLowPulseRX(List<String> data) {
        final Map<String, Module> modules = buildModules(data);
        return performButtonPushesUntilRX(modules);
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day20.txt"));
        System.out.println("Part 1: " + new Day20().findProductOfLowAndHighPulsesAfter1000ButtonPushes(data));
        System.out.println("Part 2: " + new Day20().findFewestNumberOfButtonPressesToLowPulseRX(data));
    }
}
